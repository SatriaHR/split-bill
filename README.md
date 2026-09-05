# SplitBill API

A small Spring Boot 3 REST API for splitting bills. It uses Maven, Spring Data JPA, Bean Validation, stateless bearer tokens, and H2 for local development.

## Run

Requires Java 17+ and Maven.

To Build:

```powershell
mvn clean install
```

To Run

```powershell
mvn spring-boot:run
```

The API is available at `http://localhost:4110`. The local H2 console is at `/h2-console` with JDBC URL `jdbc:h2:mem:splitbill`, username `sa`, and an empty password.

## Response format

Every response with a body uses the same envelope:

```json
{
	"timestamp": "2026-09-05T12:00:00Z",
	"status": 201,
	"message": "User signed up successfully",
	"data": { "id": 1, "username": "alice", "balance": 0.00 }
}
```

The `status` value matches the HTTP status code. The `data` field contains the relevant DTO record or list of records. Successful deletes remain `204 No Content`. Validation, domain, and unexpected errors use the same envelope; error responses put the request path in `data` and provide the explanation in `message`.

## General request and response flow

Requests follow the same controller-service-repository pattern:

1. Spring MVC routes the request to a controller in `com.splitbill.web` based on its path and HTTP method. JSON request bodies are deserialized into records in `web/dto/Requests.java`.
2. Parameters annotated with `@Valid` are checked before the controller method runs. A validation failure is handled by `GlobalExceptionHandler.validation(...)`.
3. For protected routes, `SecurityConfig.filterChain(...)` requires an authenticated request. `TokenFilter.doFilterInternal(...)` examines the bearer token before the controller is called and places the matching `AppUser` in Spring Security's context.
4. The controller calls the relevant service, such as `UserService`, `BillHeadService`, `BillDetailService`, or `PaymentService`. Services apply business rules and use repository interfaces for persistence.
5. On success, the controller converts domain entities to response DTOs with `Responses.user(...)`, `Responses.head(...)`, `Responses.detail(...)`, or `Responses.payment(...)`, then calls `ResponseHandler.response(...)`. That method creates the `Responses.ApiResponse<T>` envelope with `ResponseHandler.body(...)` and the requested HTTP status.
6. Delete methods intentionally return `204 No Content` and do not create a response body.

The exception path is centralized in `GlobalExceptionHandler`:

- A service or controller can throw `ApiException`, which carries both an HTTP status and a message. `GlobalExceptionHandler.api(...)` uses those values.
- Bean Validation failures are handled by `validation(...)` and return `400 Bad Request` with the message `Request validation failed`.
- Any other uncaught exception is handled by `unexpected(...)` and returns `500 Internal Server Error`.
- All three handlers call the private `GlobalExceptionHandler.response(...)`, which puts `{"path":"..."}` in `data` and delegates to `ResponseHandler.response(...)`. This preserves the normal timestamp, status, message, and data envelope.

Authentication failures caused by a missing, malformed, expired, or unknown token happen in the Spring Security filter chain before a controller is reached. They are therefore rejected by Spring Security as unauthorized requests rather than being raised by the application services. Authorization checks that are part of application behavior, such as a user attempting to modify another user's account, throw `ApiException` and follow the normal application error path.

## Token and login authentication flow

Authentication is stateless and uses an application-generated bearer token rather than a server session or JWT. The involved files and phases are:

### Sign-up

1. `AuthController.signUp(...)` receives a `Requests.SignUp` request at `POST /api/auth/sign-up`.
2. `UserService.signUp(...)` checks `AppUserRepository.findByUsername(...)`, hashes the password with the `PasswordEncoder` bean from `SecurityConfig`, and saves a new `AppUser`.
3. `Responses.user(...)` removes the password hash and maps the entity to the public user DTO.
4. `ResponseHandler.response(...)` returns the `201 Created` response envelope. A duplicate username raises `ApiException(CONFLICT, ...)` and is formatted by `GlobalExceptionHandler`.

### Sign-in and token creation

1. `AuthController.signIn(...)` receives `Requests.SignIn` at `POST /api/auth/sign-in`. The `/api/auth/**` route is explicitly permitted by `SecurityConfig` so a token is not required yet.
2. `UserService.signIn(...)` loads the user through `AppUserRepository.findByUsername(...)` and verifies the submitted password with `PasswordEncoder.matches(...)`. Missing users and incorrect passwords both produce `401 Unauthorized` through `ApiException`.
3. On success, `UserService.signIn(...)` generates a UUID token, stores it in `AppUser.loginToken`, and sets `loginTokenExpiration` to 24 hours from the current time. The transaction persists these changes.
4. `AuthController.signIn(...)` returns the token in the `X-Auth-Token` response header and also includes the expiration time in the `Responses.Auth` body. Clients should send the token as `Authorization: Bearer <token>` on later protected requests.

### Token verification on each protected request

1. `SecurityConfig.filterChain(...)` disables server sessions with `SessionCreationPolicy.STATELESS`, permits `/api/auth/**` and `/h2-console/**`, and requires authentication for every other route. It registers `TokenFilter` before Spring Security's username/password filter.
2. `TokenFilter.doFilterInternal(...)` reads the `Authorization` header. For a `Bearer ` value, it looks up the token through `AppUserRepository.findByLoginTokenForUpdate(...)` and accepts it only when `loginTokenExpiration` is still after the current time.
3. For a valid token, `TokenFilter.authenticate(...)` creates a `UsernamePasswordAuthenticationToken` whose principal is the `AppUser`, then stores it in `SecurityContextHolder`. Controllers can receive that user through `@AuthenticationPrincipal AppUser`.
4. The filter always continues the chain. Spring Security allows the request only when authentication is present; otherwise it rejects the protected request before the controller or service is called.

### Authorization and authenticated identity

Authentication proves which user owns the token; controllers still enforce resource-specific authorization. For example, `UserController.requireSelf(...)` prevents a user from changing another user's account, while `PaymentController` passes the authenticated user's ID to `PaymentService`. `AppUser`, `AppUserRepository`, `TokenFilter`, `SecurityConfig`, `AuthController`, and `UserService` are the primary files involved in this flow.

## Endpoint examples

The examples use PowerShell's native executable name `curl.exe`. Replace `$token`, `$userId`, `$headId`, `$detailId`, and `$paymentId` with values from earlier responses. The sign-in token is returned in the `X-Auth-Token` response header and in the response metadata. Endpoint comments abbreviate the `data` contents; the actual JSON response includes the full envelope above.

### Authentication

```powershell
curl.exe -i -X POST http://localhost:4110/api/auth/sign-up -H "Content-Type: application/json" -d '{"username":"alice","password":"password123"}'
# 201: {"status":201,"message":"User signed up successfully","data":{"id":1,"username":"alice","balance":0.00}}

curl.exe -i -X POST http://localhost:4110/api/auth/sign-in -H "Content-Type: application/json" -d '{"username":"alice","password":"password123"}'
# 200, X-Auth-Token: <token>
# {"status":200,"message":"Signed in successfully","data":{"user":{"id":1,"username":"alice","balance":0.00},"tokenExpiration":"2026-09-06T...Z"}}
$token = "<token>"
```

### Users

```powershell
curl.exe -H "Authorization: Bearer $token" http://localhost:4110/api/users
# [{"id":1,"username":"alice","balance":0.00}]

curl.exe -H "Authorization: Bearer $token" http://localhost:4110/api/users/$userId
# {"id":1,"username":"alice","balance":100.00}

curl.exe -X POST http://localhost:4110/api/users/$userId/top-up -H "Authorization: Bearer $token" -H "Content-Type: application/json" -d '{"amount":100.00}'
# {"id":1,"username":"alice","balance":100.00}

curl.exe -X POST http://localhost:4110/api/users/$userId/withdraw -H "Authorization: Bearer $token" -H "Content-Type: application/json" -d '{"amount":10.00}'
# {"id":1,"username":"alice","balance":90.00}

curl.exe -H "Authorization: Bearer $token" http://localhost:4110/api/users/$userId/bill-details/unpaid
# [{"id":2,"billHeadId":1,"userId":1,"amountToPay":25.50,"amountPaid":0.00,"outstanding":25.50}]

curl.exe -H "Authorization: Bearer $token" http://localhost:4110/api/users/$userId/payments
# [{"id":1,"payerId":1,"billDetailId":2,"amountPaid":25.50,"serviceChargeAmount":0.50,"amountTransferred":25.00,...}]

curl.exe -i -X DELETE http://localhost:4110/api/users/$userId -H "Authorization: Bearer $token"
# 204 No Content
```

The top-up, withdrawal, unpaid bill-detail, and payment-history endpoints only accept the authenticated user's own ID. A request for another user's ID returns `403 Forbidden`.

### Bill heads

Bill heads are assigned to the authenticated user who creates them and cannot exist without that user. They can be created before any details exist; attach one or more details afterward. Amounts belong on bill details.

```powershell
curl.exe -X POST http://localhost:4110/api/bill-heads -H "Authorization: Bearer $token" -H "Content-Type: application/json" -d '{"description":"Dinner"}'
# 201: {"id":1,"description":"Dinner","billDetailsSum":0.00,"serviceChargePercentage":2,"createdById":1,"details":[]}

curl.exe -H "Authorization: Bearer $token" http://localhost:4110/api/bill-heads
# [{"id":1,"description":"Dinner",...}]

curl.exe -H "Authorization: Bearer $token" http://localhost:4110/api/bill-heads/$headId
# {"id":1,"description":"Dinner",...}

curl.exe -X PUT http://localhost:4110/api/bill-heads/$headId -H "Authorization: Bearer $token" -H "Content-Type: application/json" -d '{"description":"Team dinner"}'
# {"id":1,"description":"Team dinner",...}

curl.exe -i -X DELETE http://localhost:4110/api/bill-heads/$headId -H "Authorization: Bearer $token"
# 204 No Content, or 409 if a related detail has amountPaid > 0
```

### Bill details

```powershell
curl.exe -X POST http://localhost:4110/api/bill-details/for/$headId -H "Authorization: Bearer $token" -H "Content-Type: application/json" -d '{"amountToPay":25.00,"userId":2}'
# 201: {"id":2,"billHeadId":1,"userId":2,"amountToPay":25.50,"amountPaid":0.00,"outstanding":25.50}

curl.exe -H "Authorization: Bearer $token" http://localhost:4110/api/bill-details
# [{"id":2,"billHeadId":1,"amountToPay":25.50,...}]

curl.exe -H "Authorization: Bearer $token" http://localhost:4110/api/bill-details/$detailId
# {"id":2,"billHeadId":1,"amountToPay":25.50,"amountPaid":0.00,"outstanding":25.50}

curl.exe -X PUT http://localhost:4110/api/bill-details/$detailId -H "Authorization: Bearer $token" -H "Content-Type: application/json" -d '{"amountToPay":30.00,"userId":2}'
# {"id":2,"billHeadId":1,"userId":2,"amountToPay":30.60,"amountPaid":0.00,"outstanding":30.60}

curl.exe -i -X DELETE http://localhost:4110/api/bill-details/$detailId -H "Authorization: Bearer $token"
# 204 No Content, or 409 if amountPaid > 0
```

### Payments

Payments can only be made by the user assigned to the bill detail, and that user must have enough balance to cover the full outstanding amount. The payer is debited by the full amount including service charge. The bill-head creator receives the base amount, while the service charge is reported separately.

```powershell
curl.exe -X POST http://localhost:4110/api/payments/bill-details/$detailId -H "Authorization: Bearer $token"
# {"id":1,"payerId":2,"billDetailId":2,"amountPaid":30.60,"serviceChargeAmount":0.60,"amountTransferred":30.00,"paidAt":"2026-09-05T...Z"}

curl.exe -H "Authorization: Bearer $token" http://localhost:4110/api/payments
# [{"id":1,"payerId":2,"billDetailId":2,"amountPaid":30.60,"serviceChargeAmount":0.60,"amountTransferred":30.00,...}]

curl.exe -H "Authorization: Bearer $token" http://localhost:4110/api/payments/$paymentId
# {"id":1,"payerId":2,"billDetailId":2,"amountPaid":30.60,"serviceChargeAmount":0.60,"amountTransferred":30.00,...}
```

Payment reads return only the authenticated payer's own history. Payment is atomic and uses database row locks. A payment is rejected when the assigned user is not the payer, when the detail is already paid, or when the payer's balance is less than the full outstanding amount. Withdrawals reject amounts greater than the current balance. API errors use this shape: `{"timestamp":"...","status":409,"message":"Insufficient balance","data":{"path":"/api/payments/bill-details/2"}}`.

## Tests

Run all unit and integration tests with:

```powershell
mvn test
```

Run only the balance transaction tests with:

```powershell
mvn -Dtest=BalanceServiceTest test
```

The default database is in-memory for development. You can replace the datasource settings and use migrations such as Flyway or Liquibase before production deployment.

## Bill calculation

The bill-head total is calculated by `getBillDetailsSum()` every time it is called. It sums the current `amountToPay` of every related bill detail, so changes are reflected immediately.

The service charge percentage is derived from the ASCII character-code sum of `satriahr` (the GitHub username `SatriaHR` in lowercase) modulo 10:

```text
(115 + 97 + 116 + 114 + 105 + 97 + 104 + 114) % 10 = 862 % 10 = 2%
```

It is not accepted from BillHead creator. Every new or updated bill detail has this percentage applied to its requested amount before it contributes to the bill total whenever payment is made.

## Docker

Build and run the multi-stage image:

```powershell
docker build -t split-bill-api .
docker run --rm -p 4110:4110 split-bill-api
```

The first stage builds the executable JAR with Maven; the final image runs it with the smaller Java 17 runtime image as a non-root user.

## Submission question

Q: What was the hardest design decision you made while building this, and what trade-off did you accept?

A: The hardest design decision I made was mostly related to the security token and best practices. I very rarely deal with authorizations and login tokens along with keeping up with best practice standards. I end up asking for help by looking up for examples and AI help on the internet. However, as long the code performance and reliability is on top and the authorization progresses smoothly as intended, I think such thing is acceptable to prevent future problems or simplify the code, as it's unnecessary to reinvent the wheel every time.