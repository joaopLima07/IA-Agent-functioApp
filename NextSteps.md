# Agentes com Tools — Plano de Implementação

Resumo do que será feito em cada agente. Os `INSERT`s dos agentes e o SQL da tabela `nota_fiscal` já estão prontos (conversa anterior).

## Visão geral

| # | Agente | Tipo | Tools | Modelo |
|---|---|---|---|---|
| 1 | Agente Fiscal | Banco + confirmação | `consultarNota`, `proporAlteracao`, `confirmarAlteracao` | gemini-3.8-flash |
| 2 | Agente de Cadastro | API externa | `consultarCnpj`, `consultarCep` | gemini-3.5-flash-lite |
| 3 | Agente de Câmbio | API externa | `consultarCotacao`, `converterValor` | gemini-3.5-flash-lite |
| 4 | Agente de Cálculos Financeiros | Lógica | `dataDeHoje`, `calcularAtraso`, `simularParcelamento`, `calcularDesconto`, `ratearValor` | gemini-3.8-flash |
| 5 | Agente de Prazos | Lógica | `dataDeHoje`, `verificarDia`, `proximoDiaUtil`, `somarDiasUteis`, `contarDiasUteis` | gemini-3.5-flash-lite |
| 6 | Agente de Conferência | Lógica + banco | `validarCnpj`, `validarCpf`, `conferirImposto`, `conferirNota` | gemini-3.8-flash |

Se um modelo lite errar chamadas de tool, troque para `gemini-3.8-flash` com um `UPDATE` no `model_name`.

---

## Peças compartilhadas

- **Registro de tools por agente:** `Map<String, Object> toolsByAgent` (nome do agente → objeto com os `@Tool`). No `ChatService`: `var tools = toolsByAgent.get(agent.getName()); if (tools != null) prompt = prompt.tools(tools);`
- **`userId`:** só via `.toolContext(Map.of("userId", userId))`. Nunca como parâmetro da tool, nunca vem do front nem do modelo.
- **`dataDeHoje()`:** `LocalDate.now(ZoneId.of("America/Sao_Paulo"))` em `yyyy-MM-dd`. O modelo não sabe a data atual.
- **Dinheiro e datas:** `BigDecimal` (escala 2, `RoundingMode.HALF_UP`) e `LocalDate`. Nunca `double`.
- **Erros das tools:** devolver texto curto e claro (ex.: "CNPJ inválido: informe 14 dígitos"), não lançar exceção até o modelo.
- **Parâmetros fechados:** use `enum` para moedas e campos editáveis. O modelo não escolhe valores fora da lista.
- **Chamadas HTTP:** `RestClient` com timeout curto (~3 s). O modelo só passa o dado (CNPJ, CEP, moeda). A URL é montada no código.

---

## 1. Agente Fiscal

**Objetivo:** consultar notas e alterar campos com confirmação do usuário.

**Tabela:** `nota_fiscal`, com as colunas de pendência (`campo_pendente`, `valor_pendente`, `pendente_expira_em`) e de última alteração (`ultima_alteracao_*`).

**Enum:** `CampoNota { DESCRICAO, EMAIL_DESTINATARIO, OBSERVACAO }`. Só esses campos são editáveis.

| Tool | O que faz |
|---|---|
| `consultarNota(numero)` | Busca por `findByNumeroAndUserId`. Devolve os dados da nota. Nota de outro usuário = "não encontrada". |
| `proporAlteracao(numero, campo, novoValor)` | Valida nota do usuário, não `CANCELADA`, campo no enum e tamanho do valor. Grava a pendência com `pendente_expira_em = agora + 10 min`. **Não altera o campo ainda.** |
| `confirmarAlteracao(numero)` | Exige pendência não expirada. Aplica o `valor_pendente` **do banco**, preenche `ultima_alteracao_*` e limpa a pendência, na mesma transação. |

**Cuidados**
- `confirmarAlteracao` não recebe o valor do modelo; usa o que está no banco.
- Uma pendência por nota; nova proposta sobrescreve a anterior.
- Nunca editar número, valor, impostos, datas, status.

---

## 2. Agente de Cadastro (API)

**API:** BrasilAPI (CNPJ e CEP). Conferir a documentação atual dos endpoints e limites.

| Tool | O que faz |
|---|---|
| `consultarCnpj(cnpj)` | Remove pontuação, exige 14 dígitos, consulta a API e devolve: razão social, nome fantasia, situação cadastral, atividade principal, município/UF. |
| `consultarCep(cep)` | Exige 8 dígitos e devolve logradouro, bairro, cidade, UF. |

**Cuidados:** validar o formato antes de chamar, timeout curto, devolver só os campos úteis (economiza tokens), mensagem simples quando a API estiver fora.

---

## 3. Agente de Câmbio (API)

**API:** AwesomeAPI de cotações.

**Enum de moedas:** `USD, EUR, GBP, ARS, CAD, JPY` (e `BRL` na conversão).

| Tool | O que faz |
|---|---|
| `consultarCotacao(moeda)` | Busca a cotação em reais (campo `bid`) e devolve valor + data/hora da cotação. |
| `converterValor(valor, moedaOrigem, moedaDestino)` | Busca as cotações necessárias (BRL = 1) e faz a conta em `BigDecimal` dentro da tool. |

**Cuidados:** o modelo nunca converte; cotação é de referência; se a API cair, não estimar valor.

---

## 4. Agente de Cálculos Financeiros (lógica)

**Taxas em `application.yml`** (valores de exemplo, usar a política da empresa):
`financeiro.multa-percentual`, `financeiro.juros-mensal-percentual`.

| Tool | Lógica |
|---|---|
| `calcularAtraso(valor, dataVencimento, dataPagamento)` | `dias = pagamento − vencimento`. Se ≤ 0, sem encargos. `multa = valor × multa%`. `juros = valor × (juros mensal% ÷ 30) × dias`. Devolve multa, juros, total e as taxas usadas. |
| `simularParcelamento(valor, numeroParcelas, taxaMensalPercentual)` | Taxa 0: `valor ÷ n`. Senão, Price: `parcela = valor × i ÷ (1 − (1+i)^−n)`, `i = taxa ÷ 100`. Devolve parcela, total e juros. |
| `calcularDesconto(valor, percentual)` | `desconto = valor × percentual ÷ 100`. Percentual entre 0 e 100. |
| `ratearValor(valor, partes)` | Divide em centavos inteiros e distribui o resto nas primeiras partes. A soma sempre fecha. |

**Testes sugeridos (JUnit)**
- Atraso: R$ 8.500,00, 10 dias, multa 2%, juros 1% a.m. → multa 170,00; juros 28,33; total 8.698,33.
- Rateio: R$ 100,00 em 3 partes → 33,34 / 33,33 / 33,33.
- Desconto 0% e 100%; valor negativo deve dar erro.

---

## 5. Agente de Prazos (lógica)

**Feriados nacionais**
- Fixos: 01/01, 21/04, 01/05, 07/09, 12/10, 02/11, 15/11, 20/11, 25/12.
- Móveis (a partir da Páscoa, algoritmo de Meeus/Jones/Butcher): Carnaval (−48 e −47 dias), Sexta-feira Santa (−2), Corpus Christi (+60).
- Estaduais e municipais **não** entram.

| Tool | Lógica |
|---|---|
| `verificarDia(data)` | Fim de semana, feriado (com nome) ou dia útil. |
| `proximoDiaUtil(data)` | Avança dia a dia (inclusive a própria data) até achar dia útil. |
| `somarDiasUteis(data, dias)` | Soma `dias` dias úteis, pulando fins de semana e feriados. |
| `contarDiasUteis(dataInicial, dataFinal)` | Conta os dias úteis do intervalo. Definir e documentar se as pontas entram. |

**Testes sugeridos:** sábado e domingo; 07/09; sexta-feira santa do ano; virada de ano; intervalo invertido (início > fim) deve dar erro claro.

---

## 6. Agente de Conferência (lógica + banco)

| Tool | Lógica |
|---|---|
| `validarCnpj(cnpj)` | 14 dígitos, rejeita sequências repetidas, calcula os 2 dígitos verificadores (pesos). |
| `validarCpf(cpf)` | 11 dígitos, rejeita sequências repetidas, calcula os 2 dígitos verificadores. |
| `conferirImposto(valorTotal, valorImpostos, aliquotaEsperadaPercentual)` | `esperado = total × alíquota ÷ 100`. Compara com tolerância (ex.: ± R$ 0,05). |
| `conferirNota(numero)` | Usa `ToolContext`. Busca a nota do usuário e lista problemas: e-mail ausente, CNPJ inválido, imposto/total fora do padrão, nota cancelada. **Só lê, não altera.** |

**Testes sugeridos:** CNPJ e CPF válidos e inválidos; `11111111111`; imposto exato, dentro e fora da tolerância; nota de outro usuário = "não encontrada".

---

## Ordem sugerida de implementação

1. Peças compartilhadas (`dataDeHoje`, mapa `toolsByAgent`, `ToolContext` no `ChatService`).
2. Agente de Cálculos (só lógica, ótimo para ganhar prática com tools e JUnit).
3. Agente de Prazos.
4. Agente de Conferência (`validarCnpj`/`validarCpf`/`conferirImposto` primeiro, `conferirNota` depois da entity).
5. Agente Fiscal (entity + 3 tools + fluxo de confirmação).
6. Agente de Cadastro e Agente de Câmbio (APIs externas, por último, com tratamento de erro e timeout).

## Checklist por agente

- [ ] `INSERT` do agente rodado e conferido (`SELECT id, name, status FROM agent`)
- [ ] Classe de tools criada com `@Tool` e `@ToolParam` descritivos
- [ ] Registrada no mapa `toolsByAgent` com o **nome exato** do agente
- [ ] Testes JUnit das regras de cálculo/validação
- [ ] Teste pela conversa: o Roteador escolhe o agente certo pela descrição?
- [ ] Erros das tools testados (dado inválido, API fora, nota de outro usuário)