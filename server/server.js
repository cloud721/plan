const http = require("http");

const PORT = 3000;

const swaggerSpec = {
  openapi: "3.0.0",
  info: {
    title: "Mock App API",
    version: "1.0.0",
    description: "Local mock API service for Android client login testing."
  },
  servers: [
    {
      url: "http://localhost:3000",
      description: "Local development server"
    }
  ],
  paths: {
    "/login": {
      post: {
        summary: "User login",
        description: "Accepts username and password, then returns a mock token.",
        requestBody: {
          required: true,
          content: {
            "application/json": {
              schema: {
                type: "object",
                required: ["username", "password"],
                properties: {
                  username: { type: "string", example: "admin" },
                  password: { type: "string", example: "123456" }
                }
              }
            }
          }
        },
        responses: {
          "200": {
            description: "Login success",
            content: {
              "application/json": {
                schema: {
                  type: "object",
                  properties: {
                    code: { type: "integer", example: 0 },
                    message: { type: "string", example: "Login success" },
                    data: {
                      type: "object",
                      properties: {
                        token: { type: "string", example: "mock_token_1234567890abcdef" },
                        userId: { type: "integer", example: 10001 },
                        username: { type: "string", example: "admin" }
                      }
                    }
                  }
                }
              }
            }
          },
          "400": {
            description: "Invalid JSON request body"
          },
          "404": {
            description: "Route not found"
          }
        }
      }
    }
  }
};

const swaggerHtml = `
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Mock App API - Swagger UI</title>
  <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5.9.0/swagger-ui.css" />
  <style>
    body { margin: 0; padding: 0; background: #fafafa; }
  </style>
</head>
<body>
  <div id="swagger-ui"></div>
  <script src="https://unpkg.com/swagger-ui-dist@5.9.0/swagger-ui-bundle.js"></script>
  <script src="https://unpkg.com/swagger-ui-dist@5.9.0/swagger-ui-standalone-preset.js"></script>
  <script>
    window.onload = () => {
      window.ui = SwaggerUIBundle({
        spec: ${JSON.stringify(swaggerSpec)},
        dom_id: "#swagger-ui",
        presets: [
          SwaggerUIBundle.presets.apis,
          SwaggerUIStandalonePreset
        ],
        layout: "StandaloneLayout"
      });
    };
  </script>
</body>
</html>
`;

const server = http.createServer((req, res) => {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "OPTIONS, POST, GET");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

  if (req.method === "OPTIONS") {
    res.writeHead(204);
    res.end();
    return;
  }

  if ((req.url === "/api-docs" || req.url === "/") && req.method === "GET") {
    res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
    res.end(swaggerHtml);
    return;
  }

  if (req.url === "/openapi.json" && req.method === "GET") {
    res.writeHead(200, { "Content-Type": "application/json; charset=utf-8" });
    res.end(JSON.stringify(swaggerSpec, null, 2));
    return;
  }

  if (req.url === "/login" && req.method === "POST") {
    let body = "";

    req.on("data", (chunk) => {
      body += chunk.toString();
    });

    req.on("end", () => {
      try {
        const data = body ? JSON.parse(body) : {};
        const { username, password } = data;

        console.log(
          `[${new Date().toISOString()}] login request username=${username || ""} password=${password ? "***" : ""}`
        );

        res.writeHead(200, { "Content-Type": "application/json; charset=utf-8" });
        res.end(JSON.stringify({
          code: 0,
          message: "Login success",
          data: {
            token: "mock_token_1234567890abcdef",
            userId: 10001,
            username: username || "test"
          }
        }));
      } catch (err) {
        console.error("Invalid JSON request body:", err);
        res.writeHead(400, { "Content-Type": "application/json; charset=utf-8" });
        res.end(JSON.stringify({ code: -1, message: "Invalid JSON request body" }));
      }
    });
    return;
  }

  res.writeHead(404, { "Content-Type": "application/json; charset=utf-8" });
  res.end(JSON.stringify({ code: 404, message: "Route not found. Use POST /login or GET /api-docs." }));
});

server.listen(PORT, () => {
  console.log("=================================");
  console.log("Mock API server started");
  console.log(`Base URL: http://localhost:${PORT}`);
  console.log(`Swagger UI: http://localhost:${PORT}/api-docs`);
  console.log(`OpenAPI JSON: http://localhost:${PORT}/openapi.json`);
  console.log(`Login API: POST http://localhost:${PORT}/login`);
  console.log("=================================");
});

