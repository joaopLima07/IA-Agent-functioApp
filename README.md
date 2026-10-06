# AI-Saas

Backend em Spring Boot do AI-Saas. No momento, o projeto implementa o módulo de **autenticação**: cadastro de usuários, login com JWT e consulta do usuário autenticado.

## Stack

- Java 21
- Spring Boot 4.1.1 (Web MVC, Data JPA, Validation, Security OAuth2 Resource Server)
- SQL Server (perfil `dev`) e H2 (testes)
- JWT assinado com HS256 (Nimbus JOSE)
- `libphonenumber` para validação de telefone
- Spring AI (Anthropic e Google GenAI) já nas dependências, ainda sem uso

## Configuração

O perfil ativo por padrão é `dev`. Defina estas variáveis de ambiente antes de rodar:

| Variável       | Descrição                                                   |
|----------------|-------------------------------------------------------------|
| `JWT_SECRET`   | Segredo do JWT. Mínimo de 32 bytes, senão a aplicação não sobe. |
| `DB_PASSWORD`  | Senha do usuário `ai_saas_test` no SQL Server.              |

Outras propriedades:

| Propriedade              | Padrão | Descrição                         |
|--------------------------|--------|-----------------------------------|
| `jwt.expiration-seconds` | `900`  | Validade do token, em segundos.   |

O banco esperado no perfil `dev` é `AI-SAAS` em `localhost:1433`. O schema é criado e atualizado automaticamente (`ddl-auto=update`).

## Como rodar

```bash
# Windows (PowerShell)
$env:JWT_SECRET = "troque-por-um-segredo-com-32-bytes-ou-mais"
$env:DB_PASSWORD = "sua-senha"
./mvnw spring-boot:run

# Testes
./mvnw test
```

## Endpoints

| Método | Rota                 | Autenticação | Descrição                       |
|--------|----------------------|--------------|---------------------------------|
| POST   | `/api/auth/register` | Não          | Cadastra um usuário.            |
| POST   | `/api/auth/login`    | Não          | Autentica e retorna o token.    |
| GET    | `/api/auth/me`       | Bearer JWT   | Retorna o usuário autenticado.  |

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

Regras da senha: mínimo de 8 caracteres, com ao menos uma letra, um número e um caractere especial. O telefone é validado com `libphonenumber`.

### POST /api/auth/login

```json
{ "email": "maria@exemplo.com", "password": "Senha@123" }
```

Resposta `200`:

```json
{ "accessToken": "<jwt>", "expiresIn": 900 }
```

O token tem como `subject` o id do usuário e traz os claims `role` e, quando existir, `organizationId`. O login gasta o mesmo tempo para e-mails desconhecidos, para não revelar se um e-mail está cadastrado.

### GET /api/auth/me

Header: `Authorization: Bearer <accessToken>`

Resposta `200`:

```json
{ "id": 1, "fullName": "Maria Silva", "email": "maria@exemplo.com", "phone": "+5511999999999", "role": "MEMBER" }
```

## Erros

Todos os erros de autenticação seguem o formato `{ "code": "...", "message": "..." }`.

| Status | `code`                     | Quando                                              |
|--------|----------------------------|-----------------------------------------------------|
| 400    | `VALIDATION_ERROR`         | Corpo da requisição inválido.                       |
| 401    | `INVALID_CREDENTIALS`      | E-mail ou senha incorretos, ou usuário não existe.  |
| 403    | `USER_NOT_ACTIVE`          | Usuário com status diferente de `ACTIVE`.           |
| 409    | `EMAIL_ALREADY_REGISTERED` | E-mail já cadastrado.                               |
| 422    | `WEAK_PASSWORD`            | Senha fora da política.                             |
| 422    | `INVALID_PHONE`            | Telefone inválido.                                  |

Requisições sem token, ou com token inválido ou expirado, recebem `401` do Spring Security.

## Modelo de usuário

- **Papéis (`UserRole`)**: `OWNER`, `ADMIN`, `MEMBER` (padrão).
- **Status (`UserStatus`)**: `ACTIVE` (padrão), `INACTIVE`, `SUSPENDED`.
- O e-mail é único e a senha é guardada com BCrypt.

## Estrutura

```
src/main/java/br/com/aisaas/auth
├── api/          controller, handler de erros e DTOs
├── application/  serviços (cadastro, login, JWT, política de senha)
├── domain/       entidade User, enums e exceções
└── infra/        SecurityConfig e UserRepository
```
