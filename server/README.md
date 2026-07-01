# Mock API Server

This is a dependency-free Node.js server used by the Android demo client.

## Run

```powershell
node server.js
```

## Endpoints

- `GET /api-docs`: Swagger UI
- `GET /openapi.json`: OpenAPI JSON
- `POST /login`: mock login API

Example request:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:3000/login `
  -ContentType 'application/json' `
  -Body '{"username":"admin","password":"123456"}'
```

