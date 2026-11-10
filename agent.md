# agent.md — guia para agentes de código

Backend do **AI Nexus** (SaaS B2B em que empresas configuram e conversam com agentes de IA). Java 21, Spring Boot 4.1.1, Spring AI 2.0.1 (Google GenAI / Gemini), PostgreSQL (Supabase). O frontend fica em outro repositório; aqui só existe a API. Visão geral, rotas e variáveis de ambiente: `README.md`. Plano das tools dos agentes: `NextSteps.md`.

## Comandos

```bash
./mvnw spring-boot:run            # sobe a API (perfil dev + variáveis de ambiente, ver README)
./mvnw -q -DskipTests compile     # só compila
./mvnw test                       # testes (H2 em memória)
```

- Sem perfil ativo, `application.properties` é a configuração de **produção**. Localmente use `SPRING_PROFILES_ACTIVE=dev`.
- Variáveis obrigatórias: `DATABASE_URL` (URL **JDBC** completa, com usuário e senha), `JWT` (mínimo 32 bytes), `MAIL_USERNAME`, `MAIL_PASSWORD`, `GEMINI_API_KEY`.
- Nunca commitar segredos nem colocar valores reais em `application*.properties`, README ou neste arquivo.
- Deploy: Azure Function App (`.github/workflows/main_ai-agent.yml`, `azure-function/`). A `DATABASE_URL` também precisa existir nas *Application settings* do Function App.

## Banco de dados (Supabase)

- Conexão pelo **Session pooler** (`aws-N-<região>.pooler.supabase.com:5432`, usuário `postgres.<ref-do-projeto>`). A conexão direta (`db.<ref>.supabase.co`) é só IPv6 e falha em redes IPv4 com `UnknownHostException`. O host do pooler vem do botão *Connect* do dashboard; não dá para montar de cabeça.
- O formato do Supabase (`postgresql://...`) **não** serve no Spring: use `jdbc:postgresql://host:5432/postgres?user=...&password=...&sslmode=require`. Caracteres especiais da senha precisam de percent-encoding.
- O session pooler aceita no máximo **15 clientes** no total. Por isso o pool do Hikari é pequeno (`maximum-pool-size=5`, `minimum-idle=1`). Se aparecer `EMAXCONNSESSION`, há outra instância do app ou cliente SQL usando as conexões.
- **Produção não altera schema** (`ddl-auto` só existe no perfil `dev`). Mudou uma entidade? Escreva o `ALTER TABLE` e rode no Supabase. Já aconteceu: a entidade `Invoice` ganhou `valor_icms`, `valor_ipi`, `valor_pis`, `valor_cofins`, `valor_iss`, `valor_irrf`, `valor_csll`, `valor_inss`, `valor_ibs`, `valor_cbs`, `valor_is` (`numeric(12,2) NOT NULL DEFAULT 0`) e a tabela `nota_fiscal` do banco não tinha → `column ... does not exist`.
- As skills `supabase` e `supabase-postgres-best-practices` (pastas `.agents/skills` e `.claude/skills`, `skills-lock.json`) orientam mudanças de schema, RLS e índices no Postgres.

## Estrutura (`src/main/java/br/com/aisaas`)

Pacotes por módulo, cada um com as camadas `api` → `application` → `domain` → `infra`:

| Módulo | Responsabilidade |
|---|---|
| `auth` | Cadastro, login JWT (HS256), refresh token com rotação em cookie `HttpOnly`, recuperação de senha por e-mail |
| `agent` | Listar, ver e editar agentes (controle otimista de concorrência) |
| `conversation` | Conversas, mensagens, chat em streaming (SSE), envio de arquivo, roteador de agentes, tools e notas fiscais |
| `panel` | Métricas globais (totais, tokens por dia, uso por agente, tempo de resposta) |
| `shared` | `SecurityConfig`, `AsyncConfig` e `ErrorResponse` |

## Fluxo do chat (`conversation`)

`ConversationController` (`POST /api/conversations/{id}/messages`, JSON ou `multipart/form-data` com `request` + `file`) → `ChatService.orquestratorMessage`:

1. `verifyFileExtension`: só `pdf`, `png`, `jpg`, `jpeg`, `pptx`, `docx` (senão `MediaTypeNotSuportedException` → `415 UNSUPPORTED_MEDIA`).
2. Valida a conversa do usuário e salva a mensagem `USER`.
3. `AgentRouter.choseAgent` (Gemini) recebe as últimas 5 mensagens e a lista de agentes `PUBLIC` + `ACTIVE`, e devolve só o `id` do agente. O roteador é o agente de **`id = 10`** (`AgentRepository.findAgentDecision`).
4. `ChatService.searchTool(agentId)` liga o agente à classe de tools **pelo id fixo**: `17` Fiscal, `18` Cadastro, `19` Câmbio, `20` Cálculos financeiros, `21` Prazos, `22` Conferência, `11` Lista de agentes. Outro id = sem tools. Mudou o id de um agente no banco? Ajuste esse `switch`.
5. `ReplyGeneretor.finalMessage` chama o Gemini em streaming com `.tools(agentTool)` e `.toolContext(Map.of("userId", userId))`. O arquivo enviado vai como `Media` na última mensagem do usuário.
6. A resposta sai por `SseEmitter` e é salva com tokens (roteador + resposta) e latência.

## Tools dos agentes (`conversation/tools`)

| Classe | Estado |
|---|---|
| `FiscalAgentTool` | Implementada: `releasesStatement` (lança nota extraída de um documento), `searchInvoice` (por número), `searchPerStatus` (por situação). Usa `FiscalAgentRepository`. |
| `ListAgentTool` | Implementada: `listAllAgentsDescriptions`. |
| `RegisterAgentTool`, `TermAgentTool`, `FinancialCalculationsAgentTool`, `ExchangeAgentTool`, `ConferenceAgentTool` | **Vazias** (só `@Component`). O plano está em `NextSteps.md`. |

`domain/Invoice` (tabela `nota_fiscal`) tem o fluxo de alteração com confirmação (`proposeChange` → `confirmChange`, expira em `pendingExpiresAt`; só `InvoiceField` = DESCRICAO, EMAIL_DESTINATARIO, OBSERVACAO é editável). As **tools** `proporAlteracao`/`confirmarAlteracao` ainda não existem.

### Regras do `releasesStatement`

Ordem das validações: valor total > 0 → CNPJ (normaliza, 14 caracteres numéricos ou alfanuméricos, rejeita sequências repetidas, confere dígitos verificadores módulo 11) → datas (`LocalDate.parse`; vencimento opcional e não pode ser anterior à emissão) → status `CANCELADA` é recusado → impostos totais ≤ valor total → soma dos impostos detalhados (`FiscalTax`) tem de ser igual ao total de impostos → número duplicado. Depois grava com o `userId` do `toolContext`.

## Convenções

- Exceções de domínio em `domain/exceptions`, estendem `RuntimeException` e são traduzidas em `*ControllerAdvice` para `ErrorResponse`. O advice de `conversation` tem um `RuntimeException` genérico (`500 RUNTIME_ERROR`) como último recurso.
- DTOs em `api/dto`; entidades nunca saem direto pela API.
- O `userId` vem **sempre** do JWT (e, nas tools, do `toolContext`). Nunca como parâmetro vindo do front ou do modelo. Conversa de outro usuário responde como inexistente.
- Dinheiro: `BigDecimal` (escala 2, `RoundingMode.HALF_UP`), comparação com `compareTo`/`signum`, nunca `equals` nem `double`. Datas: `LocalDate`, fuso `America/Sao_Paulo`.
- Colunas do banco em português (`nota_fiscal`, `valor_total`), campos Java em inglês. **Em `@Query` (JPQL) use o nome da entidade e dos atributos** (`select n from Invoice n where n.number = :numero`), não os da tabela. Em query derivada (`findByNumber`) também vale o atributo Java.
- Tools: parâmetros fechados com `enum`, validação no código (o modelo não é confiável), `@Tool(description)` e `@ToolParam(description)` em português, claras e com exemplo. Chamadas HTTP externas com `RestClient` e timeout curto (~3 s).
- Estilo: seguir o código ao redor (nomes, densidade de comentários). Commits em português ou inglês curtos, prefixo `fix:`/`feat:`.

## Pendências conhecidas

- `FiscalAgentTool.searchInvoice`, `searchPerStatus` e `checkForDuplicates` **não filtram por `userId`**: leem notas de todos os usuários. A checagem de duplicidade também é global (o número de um usuário bloqueia o de outro). Falta `findByUserIdAndNumber` / `findByUserIdAndStatus`, usando o `userId` do `toolContext`.
- `searchInvoice` faz `Optional.of(optional).orElseThrow(...)`, que nunca lança `NoteNotFoundException`. Deveria ser `repository.findByNumber(...).orElseThrow(...)`.
- Essas tools devolvem a entidade `Invoice` inteira ao modelo (inclui `userId`, `version` e campos internos). Preferir um resumo (DTO).
- `FiscalAgentTool` tem imports sobrando (`javax.swing...Option`, `MethodInvocationUtils`, `OutputFormat`, `JsonClassDescription` etc.). `ReplyGeneretor` recebe um `ListAgentTool` que não usa.
- Exceções lançadas pelas tools (`DuplicateInvoiceException`, `InvalidCNPJException`...) chegam ao Spring AI como erro da tool; o ideal é devolver texto curto ao modelo (ver `NextSteps.md`).
- Fontes de conhecimento e permissões dos agentes ainda não existem.

## Ao mexer no código

1. Rode `./mvnw -q -DskipTests compile` e os testes do módulo afetado antes de concluir.
2. Atualize `README.md`/`NextSteps.md` se mudar comportamento visível ou o plano das tools.
3. Não altere nomes de colunas nem `ddl-auto=update` sem combinar: o banco de produção é compartilhado e o schema é mantido à mão.
4. Mudou entidade? Entregue também o SQL de migração.
