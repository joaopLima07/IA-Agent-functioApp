<p align="center">
  <img src="docs/assets/logo-wordmark.png" alt="AI Nexus" width="320" />
</p>

# AI Nexus — Backend

API em Java 21 + Spring Boot da plataforma SaaS B2B AI Nexus, em que empresas configuram e conversam com agentes de IA especializados. Este repositório contém apenas o backend; o frontend fica em outro repositório, e o repositório central reúne a visão geral do projeto.

**Estado:** autenticação (refresh token e recuperação de senha por e-mail), agentes (listar, ver e editar), conversas (listar, abrir, carregar mensagens antigas, criar, renomear, arquivar, desarquivar e excluir), envio de mensagens com resposta em streaming (roteador de agentes + Gemini) e painel de métricas prontos. Ferramentas, fontes de conhecimento e permissões dos agentes ainda não existem.

## O que existe

- Cadastro de usuários, login com JWT (HS256) e consulta do usuário autenticado.
- Refresh token com rotação, guardado em cookie `HttpOnly`, e logout que revoga o token.
- Recuperação de senha em três etapas: pedido do código, verificação do código de 6 números enviado por e-mail e definição da nova senha.
- Listagem, detalhe e edição de agentes, com controle otimista de concorrência.
- Conversas por usuário: listagem, detalhe com as últimas 30 mensagens, paginação de mensagens antigas, criação, renomeação, arquivamento e exclusão. O usuário vem sempre do JWT, e uma conversa de outro usuário responde como inexistente.
- Envio de mensagem com resposta em streaming (SSE). O `ChatService` salva a mensagem do usuário, um agente roteador (Gemini) escolhe qual agente responde, o agente escolhido gera a resposta aos poucos, e ela é salva com tokens de entrada, tokens de saída e latência.
- Painel de métricas globais (todas as conversas): totais, conversas criadas por dia, tokens por dia, uso por agente e tempo de resposta.
- Política de senha, validação de telefone (`libphonenumber`) e senha com BCrypt.
- Documentação OpenAPI (Swagger UI).
- Spring AI com Google GenAI (Gemini) para o roteador e a geração de respostas.

## Stack

Java 21, Spring Boot 4.1.1 (Web MVC, Data JPA, Validation, OAuth2 Resource Server), Spring AI (Google GenAI), Nimbus JOSE, PostgreSQL, Spring Mail, springdoc-openapi e H2 (testes).

## Como rodar

Pré-requisitos: JDK 21 e um PostgreSQL acessível (a conexão usa `sslmode=require`).

O perfil ativo por padrão é `dev`. Defina as variáveis de ambiente:

| Variável         | Descrição                                                                                  |
|------------------|--------------------------------------------------------------------------------------------|
| `HOST`           | Host do PostgreSQL.                                                                        |
| `PORT`           | Porta do PostgreSQL.                                                                       |
| `DATABASE`       | Nome do banco.                                                                             |
| `USER`           | Usuário do banco.                                                                          |
| `PASSWORD`       | Senha do banco.                                                                            |
| `JWT`            | Segredo usado para assinar os tokens (HS256). Mínimo de 32 bytes, senão a aplicação não sobe. |
| `MAIL_USERNAME`  | Conta Gmail que envia o código de recuperação de senha.                                    |
| `MAIL_PASSWORD`  | Senha de app dessa conta (não a senha normal).                                             |
| `GEMINI_API_KEY` | Chave da API do Google Gemini, usada pelo roteador e pelos agentes.                        |

Todas são obrigatórias: se uma faltar, a aplicação não sobe e o log aponta o placeholder não resolvido (por exemplo, `Could not resolve placeholder 'JWT'`). O SMTP usa `smtp.gmail.com:587` com STARTTLS; troque `spring.mail.host` e `spring.mail.port` para outro provedor.

```powershell
$env:HOST = "localhost"; $env:PORT = "5432"; $env:DATABASE = "ai_saas"
$env:USER = "usuario"; $env:PASSWORD = "sua-senha"
$env:JWT = "um-segredo-com-pelo-menos-32-caracteres"
$env:MAIL_USERNAME = "conta@gmail.com"; $env:MAIL_PASSWORD = "senha-de-app"
$env:GEMINI_API_KEY = "sua-chave"
./mvnw spring-boot:run
```

Na IDE, defina as mesmas variáveis na configuração de execução (no IntelliJ: *Run > Edit Configurations > Environment variables*).

O schema é criado e atualizado automaticamente no perfil `dev` (`ddl-auto=update`), que também imprime o SQL no log. A validade do access token é configurada em `jwt.expiration-seconds` (padrão `900`).

Propriedades do refresh token (prefixo `refresh-token.`):

| Propriedade        | Padrão      | Descrição                                                        |
|--------------------|-------------|------------------------------------------------------------------|
| `ttl`              | `7d`        | Validade do refresh token e do cookie.                           |
| `cookie-secure`    | `true`      | Envia o cookie só por HTTPS. O perfil `dev` define `false`.      |
| `cookie-same-site` | `Lax`       | Atributo `SameSite` do cookie.                                   |
| `cookie-path`      | `/api/auth` | Caminho em que o cookie é enviado.                               |

A API sobe na porta `8080` e o Swagger UI fica em `http://localhost:8080/swagger-ui.html`. O backend não configura CORS: o frontend em desenvolvimento acessa a API por um proxy de `/api` (Vite), e em produção `/api` precisa ser encaminhado para esta aplicação. O cadastro exige telefone em formato internacional (ex.: `+5511999999999`) e senha com letra e número.

### Dados que o chat espera no banco

- **Agente roteador:** é o agente de `id = 10` (`AgentRepository.findAgentDecision`). Ele recebe as últimas 5 mensagens e a lista de agentes e deve responder só com o `id` do agente que atende. Sem esse agente, nenhuma mensagem é respondida.
- **Agentes candidatos:** só os agentes com visibilidade `PUBLIC` entram na escolha do roteador.

## Testes

```bash
./mvnw test
```

- Testes de serviço e de controller usam Mockito e `MockMvc` (standalone, com o handler de erros do módulo).
- Testes de repositório (`@DataJpaTest`) rodam em H2, sem precisar do PostgreSQL. Eles também validam as queries do painel e das conversas.
- `AiSaasApplicationTests.contextLoads` sobe a aplicação inteira e só passa com as variáveis de ambiente apontando para um banco acessível.

## Deploy

O workflow `.github/workflows/main_ai-agent.yml` compila o projeto com Java 21 e publica o `.jar` no Azure Web App `AI-Agent-App` a cada push na branch `main` (ou manualmente, por `workflow_dispatch`).

No Azure, as variáveis da tabela acima são configuradas como *Application settings* do Web App, e o Spring as lê como variáveis de ambiente.

> O perfil `dev` é o padrão (`spring.profiles.active=dev` em `application.properties`), inclusive em produção. Ele define `refresh-token.cookie-secure=false`, `ddl-auto=update` e `show-sql=true`. Para trocar, defina `SPRING_PROFILES_ACTIVE` no Azure e crie o `application-prod.properties` correspondente (ainda não existe).

## API

Todas as rotas abaixo exigem `Authorization: Bearer <accessToken>`, exceto as de autenticação marcadas como públicas. Qualquer outra rota exige JWT.

| Método | Rota                                   | Autenticação | Descrição                      |
|--------|----------------------------------------|--------------|--------------------------------|
| POST   | `/api/auth/register`                   | Não          | Cadastra um usuário.           |
| POST   | `/api/auth/login`                      | Não          | Autentica, retorna o access token e define o cookie de refresh. |
| POST   | `/api/auth/refresh`                    | Cookie       | Troca o refresh token por um novo par (rotação). |
| POST   | `/api/auth/logout`                     | Cookie       | Revoga o refresh token e limpa o cookie. |
| POST   | `/api/auth/forgotPassword`             | Não          | Envia por e-mail um código de 6 números. |
| POST   | `/api/auth/verifyResetCode`            | Não          | Confere o código e devolve o `resetToken`. |
| POST   | `/api/auth/resetPassword`              | Não          | Define a nova senha com o `resetToken`. |
| GET    | `/api/auth/me`                         | Bearer JWT   | Retorna o usuário autenticado. |
| GET    | `/api/agents`                          | Bearer JWT   | Lista os agentes (resumo).     |
| GET    | `/api/agents/{id}`                     | Bearer JWT   | Detalhe de um agente.          |
| PUT    | `/api/agents/{id}`                     | Bearer JWT   | Atualiza um agente.            |
| GET    | `/api/conversations`                   | Bearer JWT   | Lista as conversas do usuário. |
| GET    | `/api/conversations/{id}`              | Bearer JWT   | Detalhe da conversa com as últimas 30 mensagens. |
| GET    | `/api/conversations/{id}/messages?before={messageId}` | Bearer JWT | Até 30 mensagens anteriores a `messageId`. |
| POST   | `/api/conversations`                   | Bearer JWT   | Cria uma conversa.             |
| PATCH  | `/api/conversations/{id}`              | Bearer JWT   | Renomeia a conversa.           |
| POST   | `/api/conversations/{id}/messages`     | Bearer JWT   | Envia uma mensagem; a resposta chega em streaming (SSE). |
| POST   | `/api/conversations/{id}/archive`      | Bearer JWT   | Arquiva a conversa.            |
| POST   | `/api/conversations/{id}/unarchive`    | Bearer JWT   | Desarquiva a conversa.         |
| DELETE | `/api/conversations/{id}`              | Bearer JWT   | Exclui a conversa.             |
| GET    | `/api/panel/cards`                     | Bearer JWT   | Totais de conversas, mensagens e tokens. |
| GET    | `/api/panel/conversationsCreated`      | Bearer JWT   | Conversas criadas por dia (últimos 14 dias). |
| GET    | `/api/panel/dailyTokens`               | Bearer JWT   | Tokens consumidos por dia.     |
| GET    | `/api/panel/agentUsage`                | Bearer JWT   | Respostas por agente e por dia. |
| GET    | `/api/panel/responseTime`              | Bearer JWT   | Tempo médio de resposta por dia. |

### POST /api/auth/register

```json
{
  "fullName": "Maria Silva",
  "email": "maria@exemplo.com",
  "password": "Senha@123",
  "phone": "+5511999999999"
}
```

Resposta `201`:

```json
{ "id": 1, "fullName": "Maria Silva", "email": "maria@exemplo.com" }
```

Regras da senha: mínimo de 8 caracteres, com ao menos uma letra e um número (caractere especial não é exigido). O telefone é validado com `libphonenumber`.

### POST /api/auth/login

```json
{ "email": "maria@exemplo.com", "password": "Senha@123" }
```

Resposta `200`:

```json
{ "accessToken": "<jwt>", "expiresIn": 900 }
```

O refresh token vai no cookie `refresh_token` (`HttpOnly`, `SameSite=Lax`, caminho `/api/auth`), nunca no corpo. O token de acesso tem como `subject` o id do usuário e traz o claim `role`. O login gasta o mesmo tempo para e-mails desconhecidos, para não revelar se um e-mail está cadastrado.

### POST /api/auth/refresh e /api/auth/logout

Sem corpo. Ambos leem o cookie `refresh_token`.

- `refresh`: responde `200` com `{ "accessToken": "...", "expiresIn": 900 }` e um novo cookie. Sem cookie, ou com token inválido, responde `401 INVALID_REFRESH_TOKEN`.
- `logout`: responde `204`, revoga o token e expira o cookie.

### Recuperação de senha

Três etapas, todas públicas:

1. `POST /api/auth/forgotPassword` com `{ "email": "maria@exemplo.com" }`. Responde sempre `204`, exista conta ou não, para não revelar quais e-mails estão cadastrados. Se houver conta, envia por e-mail um código de 6 números, válido por 10 minutos, e invalida os códigos anteriores do usuário.
2. `POST /api/auth/verifyResetCode` com `{ "email": "...", "token": "483920" }`. O `token` precisa ter exatamente 6 números. Um código só pode ser verificado uma vez, e erros de código contam como tentativa (máximo de 5). Resposta `200`:

   ```json
   { "resetToken": "<token opaco>", "expiresIn": 600 }
   ```

3. `POST /api/auth/resetPassword` com `{ "email": "...", "resetToken": "<token opaco>", "newPassword": "NovaSenha1" }`. Responde `204`. Se a senha for recusada pela política, o `resetToken` não é consumido e vale até expirar. Em caso de sucesso ele é invalidado.

Qualquer falha de código ou token (inexistente, expirado, usado, errado ou e-mail desconhecido) responde `400 INVALID_RESET_TOKEN` com a mesma mensagem. Nem o código nem o `resetToken` são guardados em texto: só o hash BCrypt vai para o banco.

### GET /api/auth/me

Resposta `200`:

```json
{ "id": 1, "fullName": "Maria Silva", "email": "maria@exemplo.com", "phone": "+5511999999999", "role": "MEMBER" }
```

### GET /api/agents

Resposta `200`:

```json
[
  { "id": 1, "name": "Suporte", "description": "Atende dúvidas", "status": "ACTIVE", "modelName": "claude-sonnet-5-5", "updatedAt": "2026-10-09T21:00:00" }
]
```

### GET /api/agents/{id}

Resposta `200`:

```json
{
  "id": 1,
  "createdAt": "2026-10-09T20:00:00",
  "updatedAt": "2026-10-09T21:00:00",
  "version": 0,
  "information": { "name": "Suporte", "description": "Atende dúvidas", "status": "ACTIVE" },
  "model": { "modelProvider": "anthropic", "modelName": "claude-sonnet-5-5", "temperature": 0.7, "maxTokens": 1024 },
  "systemPrompt": "Você é um agente de suporte...",
  "visibility": "PRIVATE"
}
```

Se o agente não existir, responde `404`.

### PUT /api/agents/{id}

Corpo (o `version` precisa ser o mesmo lido no detalhe):

```json
{
  "version": 0,
  "information": { "name": "Suporte", "description": "Atende dúvidas", "status": "ACTIVE" },
  "model": { "modelProvider": "anthropic", "modelName": "claude-sonnet-5-5", "temperature": 0.7, "maxTokens": 1024 },
  "systemPrompt": "Você é um agente de suporte...",
  "visibility": "PRIVATE"
}
```

Resposta `200` com o detalhe atualizado. Temperatura acima de `2` responde `422 INVALID_CONFIG`; edição sobre uma versão antiga (alguém salvou antes) responde `409 CONFLICT`.

### Conversas

O dono da conversa é o usuário do JWT (`subject`); o corpo da requisição nunca informa o `userId`. Uma conversa de outro usuário é tratada como inexistente (`404 NOT_FOUND`).

**GET /api/conversations** responde `200` com a lista, sem as mensagens:

```json
[
  { "id": 1, "userId": 7, "title": "Dúvida sobre fatura", "status": "ACTIVE" }
]
```

**GET /api/conversations/{id}** responde `200` com a conversa e as últimas 30 mensagens, da mais antiga para a mais nova:

```json
{
  "id": 1,
  "userId": 7,
  "title": "Dúvida sobre fatura",
  "status": "ACTIVE",
  "messages": [
    { "id": 10, "conversationId": 1, "role": "USER", "agentId": null, "content": "Olá", "createdAt": "2026-10-19T10:00:00" },
    { "id": 11, "conversationId": 1, "role": "ASSISTANT", "agentId": 2, "content": "Como posso ajudar?", "createdAt": "2026-10-19T10:00:03" }
  ]
}
```

**GET /api/conversations/{id}/messages?before={messageId}** tem o mesmo formato, com até 30 mensagens de `id` menor que `before`, para carregar o histórico mais antigo. O parâmetro `before` é obrigatório; a primeira carga usa o detalhe.

**POST /api/conversations** cria a conversa e responde `201`:

```json
{ "title": "Nova conversa", "status": "ACTIVE" }
```

```json
{ "id": 1, "title": "Nova conversa", "status": "ACTIVE", "createdAt": "2026-10-19T10:00:00" }
```

O `title` é obrigatório, com até 100 caracteres. O agente não é escolhido na criação: o roteador decide a cada mensagem.

**PATCH /api/conversations/{id}** renomeia a conversa. Corpo `{ "newTitle": "Fatura de outubro" }` (o `newTitle` não pode ser vazio). Responde `200` com o resumo da conversa, no mesmo formato da listagem.

**POST /api/conversations/{id}/archive** e **POST /api/conversations/{id}/unarchive** não têm corpo e respondem `204`.

**DELETE /api/conversations/{id}** exclui a conversa e responde `204`.

### Envio de mensagem (SSE)

`POST /api/conversations/{id}/messages` responde com `Content-Type: text/event-stream`. Corpo:

```json
{ "content": "Quero a segunda via da fatura" }
```

O `content` é obrigatório, com até 10.000 caracteres. O fluxo é:

1. Valida a conversa e salva a mensagem do usuário.
2. O roteador escolhe o agente que responde, com base nas últimas 5 mensagens.
3. O agente escolhido gera a resposta, enviada aos poucos.
4. Ao fim, a mensagem do assistente é salva com `agentId`, tokens (os do roteador somados aos da resposta) e `latencyMs`. Se o cliente desconectar no meio, o texto recebido até ali também é salvo.

Cada pedaço da resposta chega como um evento `data:` com texto puro (não é JSON). O stream fecha quando a resposta termina. Se algo falhar **durante** o stream, o servidor envia um evento nomeado antes de fechar:

```
event: error
data: {"code":"TECHNICAL_ERROR","message":"..."}
```

Falhas **antes** do stream começar (conversa inexistente, roteador sem resposta válida, Gemini fora do ar) respondem JSON normal, com os códigos da tabela de erros. O cliente precisa usar `fetch` com leitura do corpo, porque `EventSource` não faz `POST` nem envia o header `Authorization`. A conexão tem timeout de 120 segundos.

### Painel

Métricas globais, somando **todas** as conversas de todos os usuários. Qualquer usuário autenticado pode consultar. As datas vêm no formato `yyyy-MM-dd`, e dias sem dados não aparecem na lista.

| Rota | Resposta `200` |
|------|----------------|
| `GET /api/panel/cards` | `{ "totalConversations": 10, "totalMessages": 30, "totalTokens": 13499 }` |
| `GET /api/panel/conversationsCreated` | `[ { "date": "2026-10-20", "quantityConversations": 10 } ]` (últimos 14 dias) |
| `GET /api/panel/dailyTokens` | `[ { "date": "2026-10-20", "totalTokens": 13499 } ]` |
| `GET /api/panel/agentUsage` | `[ { "date": "2026-10-20", "agentId": 2, "totalUsage": 8 } ]` (respostas do assistente por agente) |
| `GET /api/panel/responseTime` | `[ { "date": "2026-10-20", "latencyMs": 2700.0 } ]` (média das respostas do assistente) |

Os dias são calculados no fuso do servidor de banco.

### Erros

Os erros seguem o formato `{ "code": "...", "message": "..." }`.

| Status | `code`                     | Quando                                             |
|--------|----------------------------|----------------------------------------------------|
| 400    | `VALIDATION_ERROR`         | Corpo da requisição inválido (autenticação).       |
| 401    | `INVALID_CREDENTIALS`      | E-mail ou senha incorretos, ou usuário não existe. |
| 403    | `USER_NOT_ACTIVE`          | Usuário com status diferente de `ACTIVE`.          |
| 401    | `INVALID_REFRESH_TOKEN`    | Refresh token ausente, inválido, expirado ou revogado. |
| 409    | `EMAIL_ALREADY_REGISTERED` | E-mail já cadastrado.                              |
| 422    | `WEAK_PASSWORD`            | Senha fora da política.                            |
| 422    | `INVALID_PHONE`            | Telefone inválido.                                 |
| 400    | `INVALID_RESET_TOKEN`      | Código ou token de redefinição de senha inválido.  |
| 404    | `NOT_FOUND`                | Agente ou conversa não encontrados.                |
| 409    | `CONFLICT`                 | Agente alterado por outra pessoa; recarregue.      |
| 422    | `INVALID_CONFIG`           | Configuração do agente inválida (ex.: temperatura). |
| 409    | `NO_AGENT`                 | O roteador não devolveu um agente válido.          |
| 422    | `INVALID`                  | Mensagem inválida (a exceção existe, mas nenhuma regra a dispara ainda). |
| 500    | `TECHNICAL_ERROR`          | O Gemini não respondeu.                            |

Requisições sem token, ou com token inválido ou expirado, recebem `401` do Spring Security. Nas rotas de conversas, um corpo que não passa na validação (título ausente ou maior que 100 caracteres, `content` ausente ou maior que 10.000, `newTitle` vazio) responde `400` sem corpo de erro. Se o roteador devolver o id de um agente que não existe, a resposta é `404` do módulo de agentes, que o módulo de conversas ainda não traduz.

### Modelo de usuário

- **Papéis (`UserRole`)**: `OWNER`, `ADMIN`, `MEMBER` (padrão). O papel vai no JWT, mas nenhuma rota do backend o verifica ainda.
- **Status (`UserStatus`)**: `ACTIVE` (padrão), `INACTIVE`, `SUSPENDED`.
- O e-mail é único e a senha é guardada com BCrypt.

### Modelo de agente

- **Status (`AgentStatus`)**: `DRAFT` (padrão), `ACTIVE`, `PAUSED`.
- **Visibilidade (`AgentVisibility`)**: `PRIVATE` (padrão), `PUBLIC`.
- Temperatura entre `0.0` e `2.0` (padrão `0.7`) e `maxTokens` positivo, até `2048`.
- Usa controle otimista de concorrência (`version`).

### Modelo de conversa e mensagem

- **Status da conversa (`ConversationStatus`)**: `ACTIVE` (padrão), `ARCHIVED`.
- **Papel da mensagem (`MessageRole`)**: `USER`, `ASSISTANT`.
- A mensagem guarda `conversationId`, `role`, `agentId` (nulo nas mensagens do usuário), `content`, `inputToken`, `outputToken`, `totalToken`, `latencyMs` e `createdAt`. Os campos de tokens e latência só são preenchidos nas respostas do agente.

## Estrutura

```
src/main/java/br/com/aisaas
├── auth/
│   ├── api/          controller, handler de erros, cookie de refresh e DTOs
│   ├── application/  serviços (cadastro, login, JWT, refresh token, recuperação de senha, envio de e-mail, política de senha)
│   ├── domain/       User, RefreshToken, PasswordResetToken, enums e exceções
│   └── infra/        repositórios
├── agent/
│   ├── api/          AgentController, handler de erros e DTOs
│   ├── application/  AgentQueryService
│   ├── domain/       entidade Agent, enums e exceções
│   └── infra/        AgentRepository
├── conversation/
│   ├── api/          ConversationController, handler de erros e DTOs
│   ├── application/  ChatService (orquestração e SSE), AgentRouter, ReplyGeneretor, serviços de consulta e comando
│   ├── domain/       entidades Conversation e Message, enums e exceções
│   └── infra/        ConversationRepository e MessageRepository
├── panel/
│   ├── api/          PanelController e DTOs das métricas
│   ├── application/  PanelService
│   └── infra/        PanelRepository (queries de agregação)
└── shared/           SecurityConfig, configuração assíncrona e ErrorResponse
```

## Próximos passos

- Criação e exclusão de agentes (hoje só é possível listar, ver e editar).
- Ferramentas, fontes de conhecimento e permissões dos agentes.
- Endpoint de organização.
- Perfil `prod` (cookie seguro, `ddl-auto=validate`, sem SQL no log) e restrição do painel a `OWNER`/`ADMIN`.
- Regra de validação de mensagem (`InvalidMessageException`) e tradução de `AgentNotFoundException` no módulo de conversas.
- `updatedAt` e prévia da última mensagem na listagem de conversas.
- Painel: taxa de resolução e resultados de chamadas de ferramentas (dependem de definir o que é "resolvida" e de existir tool calling).
- Base de conhecimento, usando o Spring AI.
