# shoplite-order-service

Đặt hàng: lấy giá + trừ kho ở product-service (REST, token client-credentials của chính service, circuit breaker Resilience4j), lưu order, publish event `order.created` lên Kafka.

| Method | Path | Auth |
|---|---|---|
| POST | `/api/orders` `{productId, quantity}` | JWT |
| GET | `/api/orders` | JWT (đơn của chính mình) |

Trạng thái circuit breaker: `/actuator/circuitbreakers`.

Env: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `OIDC_ISSUER_URI`, `OIDC_TOKEN_URI`, `OIDC_CLIENT_SECRET`, `PRODUCT_SERVICE_URL`, `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_TOPIC_REPLICAS`.

## Chạy

- Cả stack: `shoplite-gitops/local` → `docker compose up -d --build`.
- Test: `./mvnw verify` (cần Docker cho Testcontainers).
- Image: `docker build -t <name> .` (multi-stage, layered jar, non-root UID 10001).

Health: `/actuator/health/liveness`, `/actuator/health/readiness` · Metrics: `/actuator/prometheus` · Trace/log: OpenTelemetry Java agent (inject từ ngoài, không có trong image).
