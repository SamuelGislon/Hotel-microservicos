# Hotel Microservices - TP2 DevOps

Sistema academico de gestao de hotel em arquitetura de microsservicos. Esta entrega adapta o projeto para o Trabalho Pratico 2 de DevOps com Git Flow, CI/CD, Docker, Sonar, Render, Dependabot, versionamento semantico e observabilidade com Prometheus e Grafana.

## Arquitetura

| Microsservico | Caminho | Stack principal | Porta local | Swagger DEV | Health/Metrics | Dependencias externas |
| --- | --- | --- | --- | --- | --- | --- |
| Hosped Gateway | `hosped-gateway` | Spring Cloud Gateway, JWT, Actuator | `8080` | Nao possui Swagger proprio | `/actuator/health`, `/actuator/prometheus` | Servicos HTTP internos |
| Reserva Service | `reserva-service` | Spring Boot, JPA, Flyway, RabbitMQ, Resilience4j, Actuator, Swagger | `8081` | `/swagger-ui.html`, `/api-docs` | `/actuator/health`, `/actuator/prometheus` | PostgreSQL, RabbitMQ, quartos, pagamentos |
| Hosped Quarto | `Hosped-quarto` | Spring Boot, JPA, RabbitMQ, Actuator, Swagger | `8084` | `/swagger-ui.html`, `/api-docs` | `/actuator/health`, `/actuator/prometheus` | PostgreSQL, RabbitMQ |
| Hosped Users | `Hosped-users` | Spring Boot, JPA, Security/JWT, Actuator, Swagger | `8085` | `/swagger-ui.html`, `/api-docs` | `/actuator/health`, `/actuator/prometheus` | PostgreSQL, JWT |
| Hosped Pagamento | `hosped-pagamento` | Spring Boot, JPA, RabbitMQ, Mail, Actuator, Swagger | `8083` | `/swagger-ui.html`, `/api-docs` | `/actuator/health`, `/actuator/prometheus` | PostgreSQL, RabbitMQ, SMTP opcional |

Todos os microsservicos usam Java 21, Maven, Dockerfile proprio e testes automatizados em `src/test`.

## Execucao Local

Crie o arquivo de ambiente local:

```bash
cp .env.example .env
```

Suba a stack da aplicacao:

```bash
docker compose up --build
```

Suba aplicacao mais observabilidade:

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up --build
```

URLs locais principais:

| Recurso | URL |
| --- | --- |
| API Gateway | `http://localhost:8080` |
| Reserva Service | `http://localhost:8081` |
| Hosped Pagamento | `http://localhost:8083` |
| Hosped Quarto | `http://localhost:8084` |
| Hosped Users | `http://localhost:8085` |
| RabbitMQ Management | `http://localhost:15672` |
| Prometheus | `http://localhost:9090` |
| Grafana | `http://localhost:3000` |

Credenciais padrao do Grafana local: `admin` / `admin`, alteraveis por `GRAFANA_ADMIN_USER` e `GRAFANA_ADMIN_PASSWORD`.

## Ambientes DEV e HOMOL

Os ambientes sao controlados por `SPRING_PROFILES_ACTIVE`.

| Ambiente | Branch | Profile | Swagger | Logs |
| --- | --- | --- | --- | --- |
| DEV | `develop` | `dev` | Habilitado nos servicos REST | Mais detalhado |
| HOMOL | `main` | `homol` | Desabilitado por `springdoc.swagger-ui.enabled=false` e `springdoc.api-docs.enabled=false` | Mais controlado |

No Docker Compose local, a aplicacao sobe em DEV por padrao. O `reserva-service` usa `docker,dev` para manter as configuracoes de integracao via containers.

Exemplo Render DEV:

```text
SPRING_PROFILES_ACTIVE=dev
APP_ENVIRONMENT=DEV
```

Exemplo Render HOMOL:

```text
SPRING_PROFILES_ACTIVE=homol
APP_ENVIRONMENT=HOMOL
```

## Swagger, Health e Metricas

Swagger em DEV:

```text
http://localhost:8081/swagger-ui.html
http://localhost:8083/swagger-ui.html
http://localhost:8084/swagger-ui.html
http://localhost:8085/swagger-ui.html
```

Health e metricas:

```text
http://localhost:<porta>/actuator/health
http://localhost:<porta>/actuator/info
http://localhost:<porta>/actuator/prometheus
```

Em HOMOL, Swagger e OpenAPI devem responder como indisponiveis/desabilitados nos servicos REST. O Gateway nao possui Swagger proprio.

## Git Flow

Fluxo esperado:

- `main`: codigo homologado; deploy automatico para HOMOL.
- `develop`: integracao de desenvolvimento; deploy automatico para DEV.
- `feature/*`: trabalho incremental; executa CI e build de imagem, sem deploy automatico.

O repositorio atual possui apenas `main`. Para iniciar o fluxo:

```bash
git checkout main
git checkout -b develop
git push -u origin develop
git checkout -b feature/minha-feature develop
```

## CI/CD

Os workflows ficam em `.github/workflows/`.

| Workflow | Microsservico |
| --- | --- |
| `hosped-users.yml` | `Hosped-users` |
| `hosped-quarto.yml` | `Hosped-quarto` |
| `hosped-pagamento.yml` | `hosped-pagamento` |
| `reserva-service.yml` | `reserva-service` |
| `hosped-gateway.yml` | `hosped-gateway` |

Todos chamam o workflow reutilizavel `_java-service-ci-cd.yml`, que executa:

1. checkout;
2. setup Java 21;
3. cache Maven;
4. `mvn -B clean verify`;
5. analise SonarCloud/SonarQube quando `SONAR_TOKEN` e project key existirem;
6. build da imagem Docker;
7. push para Docker Hub quando `DOCKERHUB_USERNAME` e `DOCKERHUB_TOKEN` existirem;
8. deploy Render por deploy hook em `develop` e `main`.

## Docker Hub e Tags

As imagens usam o namespace `DOCKERHUB_NAMESPACE` quando configurado; caso contrario, usam `DOCKERHUB_USERNAME`.

Namespace Docker Hub utilizado no ambiente DEV:

```text
DOCKERHUB_NAMESPACE=samuelgislon
```

Repositorios Docker Hub:

| Microsservico | Repositorio | Imagem DEV inicial |
| --- | --- | --- |
| Hosped Users | `samuelgislon/hosped-users` | `docker.io/samuelgislon/hosped-users:dev-5607a70953f54aed88b773d52679e6851808c878` |
| Hosped Quarto | `samuelgislon/hosped-quarto` | `docker.io/samuelgislon/hosped-quarto:dev-5607a70953f54aed88b773d52679e6851808c878` |
| Hosped Pagamento | `samuelgislon/hosped-pagamento` | `docker.io/samuelgislon/hosped-pagamento:dev-5607a70953f54aed88b773d52679e6851808c878` |
| Reserva Service | `samuelgislon/reserva-service` | `docker.io/samuelgislon/reserva-service:dev-5607a70953f54aed88b773d52679e6851808c878` |
| Hosped Gateway | `samuelgislon/hosped-gateway` | `docker.io/samuelgislon/hosped-gateway:dev-5607a70953f54aed88b773d52679e6851808c878` |

Tags geradas:

- `sha-<short_sha>` em builds gerais;
- `dev-<GITHUB_SHA>` em `develop`;
- `homol-<GITHUB_SHA>` em `main`;
- `feature-...-<short_sha>` em `feature/*`;
- `vX.Y.Z` e `latest` em tags semanticas `v*.*.*`.

## Versionamento Semantico

A versao base esta em `VERSION` e nos POMs como `1.0.0-SNAPSHOT`.

Regras:

- `MAJOR`: mudancas incompativeis de API, contratos, eventos ou dados.
- `MINOR`: novas funcionalidades compativeis.
- `PATCH`: correcoes compativeis e ajustes internos.

Criar release:

```bash
git tag v1.0.0
git push origin v1.0.0
```

Tags `v*.*.*` disparam os pipelines e publicam imagens `vX.Y.Z` e `latest`.

## Secrets e Vars do GitHub

Secrets:

```text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
SONAR_TOKEN
RENDER_DEPLOY_HOOK_HOSPED_USERS_DEV
RENDER_DEPLOY_HOOK_HOSPED_USERS_HOMOL
RENDER_DEPLOY_HOOK_HOSPED_QUARTO_DEV
RENDER_DEPLOY_HOOK_HOSPED_QUARTO_HOMOL
RENDER_DEPLOY_HOOK_HOSPED_PAGAMENTO_DEV
RENDER_DEPLOY_HOOK_HOSPED_PAGAMENTO_HOMOL
RENDER_DEPLOY_HOOK_RESERVA_SERVICE_DEV
RENDER_DEPLOY_HOOK_RESERVA_SERVICE_HOMOL
RENDER_DEPLOY_HOOK_HOSPED_GATEWAY_DEV
RENDER_DEPLOY_HOOK_HOSPED_GATEWAY_HOMOL
JWT_SECRET
JWT_ISSUER
JWT_EXPIRATION
DATABASE_URL_DEV
DATABASE_URL_HOMOL
RABBITMQ_URL_DEV
RABBITMQ_URL_HOMOL
```

Vars recomendadas:

```text
DOCKERHUB_NAMESPACE
SONAR_HOST_URL
SONAR_ORGANIZATION
SONAR_PROJECT_KEY_HOSPED_USERS
SONAR_PROJECT_KEY_HOSPED_QUARTO
SONAR_PROJECT_KEY_HOSPED_PAGAMENTO
SONAR_PROJECT_KEY_RESERVA_SERVICE
SONAR_PROJECT_KEY_HOSPED_GATEWAY
```

Credenciais de banco, RabbitMQ, JWT e SMTP devem ser configuradas no Render como variaveis de ambiente de cada servico, nunca commitadas.

## Render DEV e HOMOL

Crie dois servicos Render para cada microsservico: um DEV e um HOMOL. Configure cada servico para usar imagem Docker externa e gere um deploy hook.

URLs Render:

| Servico | DEV | HOMOL |
| --- | --- | --- |
| Gateway | `https://hosped-gateway-dev.onrender.com` | `https://hosped-gateway-homol.onrender.com` |
| Reserva | `https://reserva-service-dev.onrender.com` | `https://reserva-service-homol.onrender.com` |
| Quarto | `https://hosped-quarto-dev.onrender.com` | `https://hosped-quarto-homol.onrender.com` |
| Users | `https://hosped-users-dev.onrender.com` | `https://hosped-users-homol.onrender.com` |
| Pagamento | `https://hosped-pagamento-dev.onrender.com` | `https://hosped-pagamento-homol.onrender.com` |

O workflow chama o deploy hook com `imgURL=<imagem-publicada-no-Docker-Hub>`.

Deploy hooks DEV configurados como GitHub Actions Secrets:

| Servico Render DEV | Secret GitHub |
| --- | --- |
| `hosped-users-dev` | `RENDER_DEPLOY_HOOK_HOSPED_USERS_DEV` |
| `hosped-quarto-dev` | `RENDER_DEPLOY_HOOK_HOSPED_QUARTO_DEV` |
| `hosped-pagamento-dev` | `RENDER_DEPLOY_HOOK_HOSPED_PAGAMENTO_DEV` |
| `reserva-service-dev` | `RENDER_DEPLOY_HOOK_RESERVA_SERVICE_DEV` |
| `hosped-gateway-dev` | `RENDER_DEPLOY_HOOK_HOSPED_GATEWAY_DEV` |

Os valores dos deploy hooks sao secretos e nao devem ser commitados no repositorio.

## Infraestrutura DEV No Render

Banco PostgreSQL DEV/HOMOL:

```text
Servico Render: hotel-microservicos-bancos
Internal host: dpg-d8avm9u7r5hc73fehk0g-a
Port: 5432
Username: usuario_bd_hotel
Password: configurado apenas no Render
```

Databases DEV criados na mesma instancia PostgreSQL:

```text
hosped_users_dev
hosped_quartos_dev
hosped_pagamentos_dev
reserva_db_dev
```

Databases HOMOL criados na mesma instancia PostgreSQL:

```text
hosped_users_homol
hosped_quartos_homol
hosped_pagamentos_homol
reserva_db_homol
```

Variaveis de banco nos servicos DEV:

```text
# Hosped Users
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-d8avm9u7r5hc73fehk0g-a:5432/hosped_users_dev
SPRING_DATASOURCE_USERNAME=usuario_bd_hotel
SPRING_DATASOURCE_PASSWORD=<configurado-no-Render>

# Hosped Quarto
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-d8avm9u7r5hc73fehk0g-a:5432/hosped_quartos_dev
SPRING_DATASOURCE_USERNAME=usuario_bd_hotel
SPRING_DATASOURCE_PASSWORD=<configurado-no-Render>

# Hosped Pagamento
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-d8avm9u7r5hc73fehk0g-a:5432/hosped_pagamentos_dev
SPRING_DATASOURCE_USERNAME=usuario_bd_hotel
SPRING_DATASOURCE_PASSWORD=<configurado-no-Render>

# Reserva Service
DB_HOST=dpg-d8avm9u7r5hc73fehk0g-a
DB_PORT=5432
DB_NAME=reserva_db_dev
DB_USER=usuario_bd_hotel
DB_PASSWORD=<configurado-no-Render>
```

RabbitMQ DEV no CloudAMQP:

```text
Host: shark.rmq.cloudamqp.com
Port: 5671
Virtual host DEV: ojyobvsg
SSL: true
Username/password: configurados apenas no Render
```

RabbitMQ HOMOL no CloudAMQP:

```text
Host: shark.rmq.cloudamqp.com
Port: 5671
Virtual host HOMOL: ldkpunsg
SSL: true
Username/password: configurados apenas no Render
```

Validacao RabbitMQ DEV realizada no RabbitMQ Manager:

```text
Connections: 3 conexoes ativas
Exchanges: hosped.exchange, integracao.eventos.exchange, reserva.eventos.exchange
Queues: ms-pagamentos.reservas-criadas, ms-reservas.pagamentos-processados,
        pagamento.reserva.confirmado.queue, quarto.reserva.checkin.queue,
        quarto.reserva.checkout.queue, reserva.eventos.monitoramento.queue
```

Valores sensiveis como Docker Hub token, deploy hooks, senhas do PostgreSQL, senhas do CloudAMQP, `JWT_SECRET` e credenciais SMTP devem ficar apenas em GitHub Actions Secrets ou nas Environment Variables do Render.

## Observabilidade

Prometheus coleta:

- `hosped-gateway:8080/actuator/prometheus`
- `reserva-service:8080/actuator/prometheus`
- `hosped-quarto:8080/actuator/prometheus`
- `hosped-users:8080/actuator/prometheus`
- `hosped-pagamento:8080/actuator/prometheus`

Grafana provisiona automaticamente o datasource Prometheus e o dashboard `Hotel Microservices`, com paineis para:

- status dos servicos;
- requisicoes HTTP;
- taxa de erro HTTP 5xx;
- tempo medio de resposta;
- memoria JVM;
- CPU do processo.

## Validacao Manual

Build e testes por microsservico:

```bash
cd Hosped-users && mvn clean verify
cd ../Hosped-quarto && mvn clean verify
cd ../hosped-pagamento && mvn clean verify
cd ../reserva-service && mvn clean verify
cd ../hosped-gateway && mvn clean verify
```

Validar compose:

```bash
docker compose config
docker compose -f docker-compose.yml -f docker-compose.observability.yml config
```

Validar endpoints:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/prometheus
curl http://localhost:8083/swagger-ui.html
```

Validar endpoints DEV no Render:

```bash
curl https://hosped-gateway-dev.onrender.com/actuator/health
curl https://hosped-users-dev.onrender.com/actuator/health
curl https://hosped-quarto-dev.onrender.com/actuator/health
curl https://hosped-pagamento-dev.onrender.com/actuator/health
curl https://reserva-service-dev.onrender.com/actuator/health

curl https://hosped-gateway-dev.onrender.com/actuator/prometheus
curl https://hosped-users-dev.onrender.com/actuator/prometheus
curl https://hosped-quarto-dev.onrender.com/actuator/prometheus
curl https://hosped-pagamento-dev.onrender.com/actuator/prometheus
curl https://reserva-service-dev.onrender.com/actuator/prometheus
```

Validar Swagger DEV no Render:

```text
https://hosped-users-dev.onrender.com/swagger-ui.html
https://hosped-quarto-dev.onrender.com/swagger-ui.html
https://hosped-pagamento-dev.onrender.com/swagger-ui.html
https://reserva-service-dev.onrender.com/swagger-ui.html
```

O Gateway nao possui Swagger proprio.

Validar HOMOL sem Swagger:

```bash
SPRING_PROFILES_ACTIVE=homol mvn spring-boot:run
curl -i http://localhost:8083/swagger-ui.html
curl -i http://localhost:8083/api-docs
```

## Atendimento aos requisitos do Trabalho Pratico 2

| Requisito | Atendimento |
| --- | --- |
| Git Flow | Documentado com `main`, `develop` e `feature/*`; workflows reagem a essas branches. |
| CI com GitHub Actions | Criados workflows individuais por microsservico. |
| Build automatizado | `mvn clean verify` em cada pipeline. |
| Testes automatizados | Testes Maven executados em cada pipeline. |
| SonarQube/SonarCloud | Analise opcional via `SONAR_TOKEN` e vars de projeto. |
| CD automatico | Render deploy hooks em `develop` e `main`. |
| DEV em `develop` | `develop` publica tag `dev-*` e aciona hook DEV. |
| HOMOL em `main` | `main` publica tag `homol-*` e aciona hook HOMOL. |
| Render ou equivalente | Suporte Render por deploy hook com `imgURL`. |
| Swagger DEV | Perfis `dev` deixam Swagger habilitado. |
| Swagger HOMOL | Perfis `homol` desabilitam Swagger/OpenAPI. |
| Dockerfile | Todos os microsservicos possuem Dockerfile multi-stage. |
| Build de imagem Docker | Workflow faz build de imagem por microsservico. |
| Publicacao de imagem | Push para Docker Hub com secrets. |
| Semantic Versioning | `VERSION`, POMs `1.0.0-SNAPSHOT`, tags `v*.*.*` e regras documentadas. |
| Dependabot | `.github/dependabot.yml` cobre Maven, Actions, Docker e Compose. |
| GitHub Secrets | Secrets esperados documentados e usados nos workflows. |
| Prometheus e Grafana | Compose, Prometheus config, datasource e dashboard Grafana criados. |
| Pipeline separado por microsservico | Cinco workflows especificos criados. |
| README | Este documento descreve arquitetura, execucao, CI/CD, secrets, ambientes e entregaveis. |
| URLs DEV/HOMOL | URLs DEV documentadas; URLs HOMOL ficam como padrao esperado para a pessoa responsavel por HOMOL. |
| Dashboards | Dashboard `Hotel Microservices` provisionado no Grafana. |

## Pendencias Externas

- Configurar SonarCloud/SonarQube caso a analise estatica seja exigida no ambiente remoto.
- Concluir configuracao HOMOL em `main`, incluindo services Render, deploy hooks HOMOL e variaveis runtime HOMOL.
- Manter credenciais reais fora do repositorio, usando GitHub Actions Secrets e Environment Variables do Render.
