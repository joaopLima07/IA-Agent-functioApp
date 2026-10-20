<p align="center">
  <img src="docs/assets/logo-wordmark.png" alt="AI Nexus" width="320" />
</p>

# AI-Saas

Plataforma SaaS B2B em que empresas configuram e conversam com agentes de IA especializados. O repositório reúne duas aplicações:

| Pasta              | O que é                                                    | Estado                                              |
|--------------------|------------------------------------------------------------|-----------------------------------------------------|
| `backend`          | API em Java 21 + Spring Boot                               | **Autenticação** (refresh token e recuperação de senha por e-mail), **agentes** (listar, ver e editar) e **conversas** (listar, abrir, carregar mensagens antigas e criar) prontos. O envio de mensagens (roteador de agentes + Gemini) existe no serviço, mas ainda não tem rota. |
| `frontend`         | Frontend em React + TypeScript + Vite                      | Interface completa. **Autenticação, recuperação de senha, agentes e conversas (listar, abrir, criar)** integrados ao backend; o resto roda sobre dados mockados. |

> O frontend usa o backend de verdade para `register`, `login`, `me`, `refresh`, `logout`, a recuperação de senha, `/api/agents` e as rotas de leitura e criação de `/api/conversations`. O envio de mensagens (chat em streaming), renomear, arquivar e excluir conversas, base de conhecimento, dashboard e organização continuam mockados no front, e ferramentas, fontes de conhecimento e permissões dos agentes ainda não existem no backend.

## Visão geral do que existe

**Backend**
- Cadastro de usuários, login com JWT (HS256) e consulta do usuário autenticado.
- Refresh token com rotação, guardado em cookie `HttpOnly`, e logout que revoga o token.
- Recuperação de senha em três etapas: pedido do código, verificação do código de 6 números enviado por e-mail e definição da nova senha.
- Listagem, detalhe e edição de agentes, com controle otimista de concorrência.
- Conversas por usuário: listagem, detalhe com as últimas 30 mensagens, paginação de mensagens antigas e criação. O usuário vem sempre do JWT, e uma conversa de outro usuário responde como inexistente.
- Orquestração de chat no `ChatService`: salva a mensagem do usuário, um agente roteador (Gemini) escolhe qual agente responde, o agente escolhido gera a resposta, e ela é salva com tokens de entrada, tokens de saída e latência. Ainda não exposta por rota.
- Política de senha, validação de telefone (`libphonenumber`) e senha com BCrypt.
- Documentação OpenAPI (Swagger UI).
- Spring AI com Google GenAI (Gemini) para o roteador e a geração de respostas.

**Frontend**
- Telas de login, cadastro e recuperação de senha (código e nova senha em duas etapas), com rotas protegidas e renovação automática do access token.
- Chat com agentes: respostas em streaming, markdown com realce de código e cards de chamada de ferramenta.
- Histórico de conversas, listagem, detalhe e edição de agentes.
- Base de conhecimento com upload de documentos.
- Dashboard com gráficos (conversas, uso de agentes, tokens, tempo de resposta, resultado de ferramentas).
- Configurações, tema claro/escuro e layout responsivo.

## Stack

| Camada   | Tecnologias                                                                                           |
|----------|-------------------------------------------------------------------------------------------------------|
| Backend  | Java 21, Spring Boot 4.1.1 (Web MVC, Data JPA, Validation, OAuth2 Resource Server), Nimbus JOSE, PostgreSQL, Spring Mail, springdoc-openapi, H2 (testes) |
| Frontend | React 19, TypeScript, Vite, Tailwind CSS 4, Radix UI (estilo shadcn/ui), TanStack Query, React Router 7, Zod, Recharts, react-markdown |

## Como rodar

### Backend

Pré-requisitos: JDK 21 e um PostgreSQL acessível (a conexão usa `sslmode=require`).

O perfil ativo por padrão é `dev`. Defina as variáveis de ambiente:

| Variável        | Descrição                                                                 |
|-----------------|---------------------------------------------------------------------------|
| `HOST`          | Host do PostgreSQL.                                                       |
| `PORT`          | Porta do PostgreSQL.                                                      |
| `DATABASE`      | Nome do banco.                                                            |
| `USER`          | Usuário do banco.                                                         |
| `PASSWORD`      | Senha do banco.                                                           |
| `MAIL_USERNAME` | Conta Gmail que envia o código de recuperação de senha.                   |
| `MAIL_PASSWORD` | Senha de app dessa conta (não a senha normal).                            |
| `GEMINI_API_KEY` | Chave da API do Google Gemini, usada pelo roteador e pelos agentes.      |

O segredo do JWT fica em `jwt.secret` (mínimo de 32 bytes, senão a aplicação não sobe). O SMTP usa `smtp.gmail.com:587` com STARTTLS; troque `spring.mail.host` e `spring.mail.port` para outro provedor.

```powershell
cd backend
$env:HOST = "localhost"; $env:PORT = "5432"; $env:DATABASE = "ai_saas"
$env:USER = "usuario"; $env:PASSWORD = "sua-senha"
$env:MAIL_USERNAME = "conta@gmail.com"; $env:MAIL_PASSWORD = "senha-de-app"
$env:GEMINI_API_KEY = "sua-chave"
./mvnw spring-boot:run

# Testes
./mvnw test
```

O teste `AiSaasApplicationTests.contextLoads` sobe a aplicação inteira e só passa com essas variáveis apontando para um banco acessível.

O schema é criado e atualizado automaticamente (`ddl-auto=update`). A validade do access token é configurada em `jwt.expiration-seconds` (padrão `900`).

Propriedades do refresh token (prefixo `refresh-token.`):

| Propriedade     | Padrão      | Descrição                                                        |
|-----------------|-------------|------------------------------------------------------------------|
| `ttl`           | `7d`        | Validade do refresh token e do cookie.                           |
| `cookie-secure` | `true`      | Envia o cookie só por HTTPS. O perfil `dev` define `false`.      |
| `cookie-same-site` | `Lax`    | Atributo `SameSite` do cookie.                                   |
| `cookie-path`   | `/api/auth` | Caminho em que o cookie é enviado.                               |

> O `application-dev.properties` está no `.gitignore` e não vai para o repositório. Crie-o em `backend/src/main/resources/` com `spring.jpa.hibernate.ddl-auto=update`, `refresh-token.ttl` e `refresh-token.cookie-secure=false`.

### Frontend

Pré-requisito: Node.js.

```bash
cd frontend
npm install
npm run dev       # servidor de desenvolvimento
npm run build     # checagem de tipos + build de produção
npm run preview   # prévia do build
npm run lint      # oxlint
```

Suba o backend antes (porta `8080`; o Swagger UI fica em `http://localhost:8080/swagger-ui.html`). Em desenvolvimento, o Vite faz proxy de `/api` para `http://localhost:8080`, então não é preciso CORS. Para apontar para outro endereço, defina `VITE_PROXY_TARGET`. O cadastro exige telefone em formato internacional (ex.: `+5511999999999`) e senha com letra e número.

## API do backend

| Método | Rota                 | Autenticação | Descrição                      |
|--------|----------------------|--------------|--------------------------------|
| POST   | `/api/auth/register` | Não          | Cadastra um usuário.           |
| POST   | `/api/auth/login`    | Não          | Autentica, retorna o access token e define o cookie de refresh. |
| POST   | `/api/auth/refresh`  | Cookie       | Troca o refresh token por um novo par (rotação). |
| POST   | `/api/auth/logout`   | Cookie       | Revoga o refresh token e limpa o cookie. |
| POST   | `/api/auth/forgotPassword`   | Não  | Envia por e-mail um código de 6 números. |
| POST   | `/api/auth/verifyResetCode`  | Não  | Confere o código e devolve o `resetToken`. |
| POST   | `/api/auth/resetPassword`    | Não  | Define a nova senha com o `resetToken`. |
| GET    | `/api/auth/me`       | Bearer JWT   | Retorna o usuário autenticado. |
| GET    | `/api/agents`        | Bearer JWT   | Lista os agentes (resumo).     |
| GET    | `/api/agents/{id}`   | Bearer JWT   | Detalhe de um agente.          |
| PUT    | `/api/agents/{id}`   | Bearer JWT   | Atualiza um agente.            |
| GET    | `/api/conversations` | Bearer JWT   | Lista as conversas do usuário. |
| GET    | `/api/conversations/{id}` | Bearer JWT | Detalhe da conversa com as últimas 30 mensagens. |
| GET    | `/api/conversations/{id}/messages?before={messageId}` | Bearer JWT | Até 30 mensagens anteriores a `messageId`. |
| POST   | `/api/conversations` | Bearer JWT   | Cria uma conversa.             |

Qualquer outra rota exige JWT.

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

Header: `Authorization: Bearer <accessToken>`

Resposta `200`:

```json
{ "id": 1, "fullName": "Maria Silva", "email": "maria@exemplo.com", "phone": "+5511999999999", "role": "MEMBER" }
```

### GET /api/agents

Header: `Authorization: Bearer <accessToken>`

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

Header: `Authorization: Bearer <accessToken>`

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

Todas as rotas exigem `Authorization: Bearer <accessToken>`. O dono da conversa é o usuário do JWT (`subject`); o corpo da requisição nunca informa o `userId`. Uma conversa de outro usuário é tratada como inexistente.

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

### Erros

Os erros seguem o formato `{ "code": "...", "message": "..." }`.

| Status | `code`                     | Quando                                             |
|--------|----------------------------|----------------------------------------------------|
| 400    | `VALIDATION_ERROR`         | Corpo da requisição inválido.                      |
| 401    | `INVALID_CREDENTIALS`      | E-mail ou senha incorretos, ou usuário não existe. |
| 403    | `USER_NOT_ACTIVE`          | Usuário com status diferente de `ACTIVE`.          |
| 401    | `INVALID_REFRESH_TOKEN`    | Refresh token ausente, inválido, expirado ou revogado. |
| 409    | `EMAIL_ALREADY_REGISTERED` | E-mail já cadastrado.                              |
| 422    | `WEAK_PASSWORD`            | Senha fora da política.                            |
| 422    | `INVALID_PHONE`            | Telefone inválido.                                 |
| 400    | `INVALID_RESET_TOKEN`      | Código ou token de redefinição de senha inválido.  |
| 404    | `NOT_FOUND`                | Agente não encontrado.                             |
| 409    | `CONFLICT`                 | Agente alterado por outra pessoa; recarregue.      |
| 422    | `INVALID_CONFIG`           | Configuração do agente inválida (ex.: temperatura). |

Requisições sem token, ou com token inválido ou expirado, recebem `401` do Spring Security.

> As rotas de conversas ainda não têm handler de erros próprio: conversa não encontrada (`ConversationNotFoundException`), falha do Gemini (`GeminiAPINotRespondException`) e resposta inválida do roteador (`InvalidRouterResponseException`) respondem `500` por enquanto. O plano é `404`, `502` e `502`.

### Modelo de usuário

- **Papéis (`UserRole`)**: `OWNER`, `ADMIN`, `MEMBER` (padrão).
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
- A mensagem guarda `conversationId`, `role`, `agentId` (nulo nas mensagens do usuário), `content`, `inputToken`, `outputToken`, `latencyMs` e `createdAt`. Os três últimos só são preenchidos nas respostas do agente.

## Estrutura do repositório

```
.
├── backend/
│   └── src/main/java/br/com/aisaas
│       ├── auth/
│       │   ├── api/          controller, handler de erros, cookie de refresh e DTOs
│       │   ├── application/  serviços (cadastro, login, JWT, refresh token, recuperação de senha, envio de e-mail, política de senha)
│       │   ├── domain/       User, RefreshToken, PasswordResetToken, enums e exceções
│       │   └── infra/        repositórios
│       ├── agent/
│       │   ├── api/          AgentController, handler de erros e DTOs
│       │   ├── application/  AgentQueryService
│       │   ├── domain/       entidade Agent, enums e exceções
│       │   └── infra/        AgentRepository
│       ├── conversation/
│       │   ├── api/          ConversationController e DTOs
│       │   ├── application/  ChatService (orquestração), AgentRouter, ReplyGeneretor, serviços de consulta e comando
│       │   ├── domain/       entidades Conversation e Message, enums e exceções
│       │   └── infra/        ConversationRepository e MessageRepository
│       └── shared/           SecurityConfig, configuração assíncrona e ErrorResponse
└── frontend/
    └── src
        ├── features/     módulos por domínio: agents, auth, chat, conversations, dashboard, knowledge
        ├── pages/        telas roteadas (carregadas sob demanda)
        ├── components/   ui (base), layout e common
        ├── layouts/      AppLayout e AuthLayout
        ├── routes/       ProtectedRoute e PublicOnlyRoute
        ├── mocks/        dados de exemplo
        └── hooks/, lib/  utilitários compartilhados
```

Cada módulo do frontend em `features/` segue o padrão `components/`, `hooks/`, `services/` e `types/`. Para ligar o app ao backend, basta trocar o corpo dos `services/` por chamadas HTTP.

## Próximos passos

- Criação e exclusão de agentes (hoje só é possível listar, ver e editar).
- Ferramentas, fontes de conhecimento e permissões dos agentes no backend.
- Endpoint de organização, que ainda é mockado no frontend.
- Rota de envio de mensagem (`POST /api/conversations/{id}/messages`) ligada ao `ChatService`, com resposta em streaming.
- Renomear, arquivar, desarquivar e excluir conversas, que já têm serviço mas ainda não têm rota.
- Handler de erros das conversas (`404`, `502`) e `updatedAt` e prévia da última mensagem na listagem.
- Base de conhecimento e dashboard no backend, usando o Spring AI.
