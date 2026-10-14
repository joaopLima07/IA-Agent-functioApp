# AI-Saas

Plataforma SaaS B2B em que empresas configuram e conversam com agentes de IA especializados. O repositório reúne duas aplicações:

| Pasta              | O que é                                                    | Estado                                              |
|--------------------|------------------------------------------------------------|-----------------------------------------------------|
| `backend`          | API em Java 21 + Spring Boot                               | **Autenticação** (com refresh token) e **consulta de agentes** prontas. |
| `frontend`         | Frontend em React + TypeScript + Vite                      | Interface completa. **Cadastro, login e usuário atual** integrados ao backend; o resto roda sobre dados mockados. |

> O frontend usa o backend de verdade apenas para `register`, `login` e `me`. Ainda não consome `refresh`, `logout` nem `/api/agents`. Agentes, conversas, base de conhecimento, dashboard e recuperação de senha continuam mockados no front.

## Visão geral do que existe

**Backend**
- Cadastro de usuários, login com JWT (HS256) e consulta do usuário autenticado.
- Refresh token com rotação, guardado em cookie `HttpOnly`, e logout que revoga o token.
- Listagem e detalhe de agentes (somente leitura por enquanto).
- Política de senha, validação de telefone (`libphonenumber`) e senha com BCrypt.
- Spring AI (Anthropic e Google GenAI) já nas dependências, ainda sem uso.

**Frontend**
- Telas de login, cadastro e recuperação de senha, com rotas protegidas.
- Chat com agentes: respostas em streaming, markdown com realce de código e cards de chamada de ferramenta.
- Histórico de conversas, listagem e detalhe de agentes.
- Base de conhecimento com upload de documentos.
- Dashboard com gráficos (conversas, uso de agentes, tokens, tempo de resposta, resultado de ferramentas).
- Configurações, tema claro/escuro e layout responsivo.

## Stack

| Camada   | Tecnologias                                                                                           |
|----------|-------------------------------------------------------------------------------------------------------|
| Backend  | Java 21, Spring Boot 4.1.1 (Web MVC, Data JPA, Validation, OAuth2 Resource Server), Nimbus JOSE, SQL Server (dev), H2 (testes) |
| Frontend | React 19, TypeScript, Vite, Tailwind CSS 4, Radix UI (estilo shadcn/ui), TanStack Query, React Router 7, Zod, Recharts, react-markdown |

## Como rodar

### Backend

Pré-requisitos: JDK 21 e um SQL Server em `localhost:1433` com o banco `AI-SAAS`.

O perfil ativo por padrão é `dev`. Defina as variáveis de ambiente:

| Variável      | Descrição                                                       |
|---------------|-----------------------------------------------------------------|
| `JWT_SECRET`  | Segredo do JWT. Mínimo de 32 bytes, senão a aplicação não sobe. |
| `DB_PASSWORD` | Senha do usuário `ai_saas_test` no SQL Server.                  |

```powershell
cd backend
$env:JWT_SECRET = "troque-por-um-segredo-com-32-bytes-ou-mais"
$env:DB_PASSWORD = "sua-senha"
./mvnw spring-boot:run

# Testes
./mvnw test
```

O schema é criado e atualizado automaticamente (`ddl-auto=update`). A validade do access token é configurada em `jwt.expiration-seconds` (padrão `900`).

Propriedades do refresh token (prefixo `refresh-token.`):

| Propriedade     | Padrão      | Descrição                                                        |
|-----------------|-------------|------------------------------------------------------------------|
| `ttl`           | `7d`        | Validade do refresh token e do cookie.                           |
| `cookie-secure` | `true`      | Envia o cookie só por HTTPS. O perfil `dev` define `false`.      |
| `cookie-same-site` | `Lax`    | Atributo `SameSite` do cookie.                                   |
| `cookie-path`   | `/api/auth` | Caminho em que o cookie é enviado.                               |

> O `application-dev.properties` está no `.gitignore` e não vai para o repositório. Crie-o em `backend/src/main/resources/` com a URL, usuário e senha do banco, `spring.jpa.hibernate.ddl-auto=update`, `refresh-token.ttl` e `refresh-token.cookie-secure=false`.

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

Suba o backend antes (porta `8080`). Em desenvolvimento, o Vite faz proxy de `/api` para `http://localhost:8080`, então não é preciso CORS. Para apontar para outro endereço, defina `VITE_PROXY_TARGET`. O cadastro exige telefone em formato internacional (ex.: `+5511999999999`) e senha com letra e número.

## API do backend

| Método | Rota                 | Autenticação | Descrição                      |
|--------|----------------------|--------------|--------------------------------|
| POST   | `/api/auth/register` | Não          | Cadastra um usuário.           |
| POST   | `/api/auth/login`    | Não          | Autentica, retorna o access token e define o cookie de refresh. |
| POST   | `/api/auth/refresh`  | Cookie       | Troca o refresh token por um novo par (rotação). |
| POST   | `/api/auth/logout`   | Cookie       | Revoga o refresh token e limpa o cookie. |
| GET    | `/api/auth/me`       | Bearer JWT   | Retorna o usuário autenticado. |
| GET    | `/api/agents`        | Bearer JWT   | Lista os agentes (resumo).     |
| GET    | `/api/agents/{id}`   | Bearer JWT   | Detalhe de um agente.          |

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

Regras da senha: mínimo de 8 caracteres, com ao menos uma letra e um número. O telefone é validado com `libphonenumber`.

### POST /api/auth/login

```json
{ "email": "maria@exemplo.com", "password": "Senha@123" }
```

Resposta `200`:

```json
{ "accessToken": "<jwt>", "expiresIn": 900 }
```

O refresh token vai no cookie `refresh_token` (`HttpOnly`, `SameSite=Lax`, caminho `/api/auth`), nunca no corpo. O token de acesso tem como `subject` o id do usuário e traz os claims `role` e, quando existir, `organizationId`. O login gasta o mesmo tempo para e-mails desconhecidos, para não revelar se um e-mail está cadastrado.

### POST /api/auth/refresh e /api/auth/logout

Sem corpo. Ambos leem o cookie `refresh_token`.

- `refresh`: responde `200` com `{ "accessToken": "...", "expiresIn": 900 }` e um novo cookie. Sem cookie, ou com token inválido, responde `401 INVALID_REFRESH_TOKEN`.
- `logout`: responde `204`, revoga o token e expira o cookie.

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

### Erros

Todos os erros de autenticação seguem o formato `{ "code": "...", "message": "..." }`.

| Status | `code`                     | Quando                                             |
|--------|----------------------------|----------------------------------------------------|
| 400    | `VALIDATION_ERROR`         | Corpo da requisição inválido.                      |
| 401    | `INVALID_CREDENTIALS`      | E-mail ou senha incorretos, ou usuário não existe. |
| 403    | `USER_NOT_ACTIVE`          | Usuário com status diferente de `ACTIVE`.          |
| 401    | `INVALID_REFRESH_TOKEN`    | Refresh token ausente, inválido, expirado ou revogado. |
| 409    | `EMAIL_ALREADY_REGISTERED` | E-mail já cadastrado.                              |
| 422    | `WEAK_PASSWORD`            | Senha fora da política.                            |
| 422    | `INVALID_PHONE`            | Telefone inválido.                                 |

Requisições sem token, ou com token inválido ou expirado, recebem `401` do Spring Security.

### Modelo de usuário

- **Papéis (`UserRole`)**: `OWNER`, `ADMIN`, `MEMBER` (padrão).
- **Status (`UserStatus`)**: `ACTIVE` (padrão), `INACTIVE`, `SUSPENDED`.
- O e-mail é único e a senha é guardada com BCrypt.

### Modelo de agente

- **Status (`AgentStatus`)**: `DRAFT` (padrão), `ACTIVE`, `PAUSED`.
- **Visibilidade (`AgentVisibility`)**: `PRIVATE` (padrão), `PUBLIC`.
- Temperatura entre `0.0` e `2.0` (padrão `0.7`) e `maxTokens` até `2048`.
- Usa controle otimista de concorrência (`version`).

## Estrutura do repositório

```
.
├── backend/
│   └── src/main/java/br/com/aisaas
│       ├── auth/
│       │   ├── api/          controller, handler de erros, cookie de refresh e DTOs
│       │   ├── application/  serviços (cadastro, login, JWT, refresh token, política de senha)
│       │   ├── domain/       User, RefreshToken, enums e exceções
│       │   └── infra/        SecurityConfig e repositórios
│       └── agent/
│           ├── controller/   AgentController, handler de erros e DTOs
│           ├── application/  AgentQueryService
│           ├── domain/       entidade Agent, enums e exceções
│           └── infra/        AgentRepository
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

- Integrar o frontend a `refresh`, `logout` e `/api/agents`. Hoje o access token expira em 15 min e o usuário volta ao login.
- Endpoint de recuperação de senha.
- Criação, edição e exclusão de agentes (`UpdateAgentRequest` ainda está vazio).
- Implementar no backend os módulos de conversas, base de conhecimento e dashboard, usando o Spring AI.
