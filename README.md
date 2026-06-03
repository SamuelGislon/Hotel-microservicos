# Microsservicos de Hotel - TP2 DevOps

Sistema academico de gestao de hotel em arquitetura de microsservicos, desenvolvido por Samuel Gislon, Lucas Jacinto e Nicoli Zimmermann estudantes da UDESC. Esta entrega adapta o projeto para o Trabalho Pratico 2 de DevOps com Git Flow, CI/CD, Docker, SonarCloud, Render, Dependabot, versionamento semantico e observabilidade com Prometheus e Grafana. teste

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

Desenvolvimento novo deve partir de `develop`:

```bash
git checkout develop
git pull origin develop
git checkout -b feature/minha-feature develop
```

Quando uma feature estiver pronta, abra PR/merge para `develop`. O merge/push em `develop` publica imagens `dev-*` e aciona deploy DEV. Quando a entrega estiver homologada em DEV, abra PR/merge de `develop` para `main`; o merge/push em `main` publica imagens `homol-*` e aciona deploy HOMOL.

Releases seguem Semantic Versioning por tags:

```bash
git tag v1.0.0
git push origin v1.0.0
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
5. analise SonarCloud somente na branch `main`, quando `SONAR_TOKEN`, project key e organizacao estiverem configurados;
6. build da imagem Docker;
7. push para Docker Hub quando `DOCKERHUB_USERNAME` e `DOCKERHUB_TOKEN` existirem;
8. deploy Render por deploy hook em `develop` e `main`.

### SonarCloud

A analise fica na etapa `Analisar com SonarCloud` do workflow reutilizavel `.github/workflows/_java-service-ci-cd.yml`. Cada workflow de microsservico passa sua propria `SONAR_PROJECT_KEY_*`, entao a analise roda separadamente para:

- `Hosped-users`;
- `Hosped-quarto`;
- `hosped-pagamento`;
- `reserva-service`;
- `hosped-gateway`.

A analise SonarCloud esta integrada aos pipelines, porem, por limitacao do plano gratuito, o dashboard visivel e mantido apenas na branch `main`. Por isso, o workflow executa SonarCloud somente quando o ref atual e a branch `main`. A branch `develop` continua executando build, testes, Docker, publicacao de imagem e deploy DEV, mas pula a analise SonarCloud com uma mensagem clara no log. Para validacao academica, a branch `main` contem a analise visivel no SonarCloud e representa HOMOL/release conforme o Git Flow. Nao sera usado upgrade pago do SonarCloud.

O comando usado pelo pipeline e:

```bash
mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.7.0.6970:sonar \
  -Dsonar.projectKey=<project-key> \
  -Dsonar.projectName=<service-name> \
  -Dsonar.host.url=<sonar-host-url> \
  -Dsonar.organization=<sonar-organization> \
  -Dsonar.scanner.skipJreProvisioning=true
```

O token nao e passado na linha de comando; o scanner Maven usa o GitHub Secret `SONAR_TOKEN` exposto como variavel de ambiente. Nao ha `sonar-project.properties` no repositorio porque o scanner Maven consegue inferir fontes, testes e bytecode a partir dos POMs de cada microsservico. O parametro `sonar.scanner.skipJreProvisioning=true` usa o Java 21 ja configurado no GitHub Actions e evita falha de permissao ao consultar metadados de JRE no SonarCloud.

A etapa executa ate 3 tentativas da analise. Isso evita falhas intermitentes de API do SonarCloud, como erro `403` temporario ao baixar metadados ou o scanner engine. Se a terceira tentativa falhar, o pipeline para e indica para conferir `SONAR_TOKEN`, `SONAR_ORGANIZATION` e `SONAR_PROJECT_KEY_*`.

Resultados:

- SonarCloud: acesse `https://sonarcloud.io/projects`, abra o projeto configurado por `SONAR_PROJECT_KEY_*` e consulte o dashboard da branch `main`.

Na `develop`, o log esperado e um notice informando que a analise foi pulada por causa da limitacao do plano gratuito do SonarCloud. Isso nao invalida o pipeline DEV.

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

Configure os servicos Render por ambiente usando imagem Docker externa e deploy hook. O ambiente DEV possui os cinco microsservicos principais. No ambiente HOMOL validado nesta entrega, estao implantados pagamento, quarto e reserva; users e gateway nao foram criados em HOMOL.

URLs Render:

| Servico | DEV | HOMOL |
| --- | --- | --- |
| Gateway | `https://hosped-gateway-dev.onrender.com` | Nao implantado em HOMOL |
| Reserva | `https://reserva-service-dev.onrender.com` | `https://hosped-reserva-homol.onrender.com` |
| Quarto | `https://hosped-quarto-dev.onrender.com` | `https://hosped-quarto-homol.onrender.com` |
| Users | `https://hosped-users-dev.onrender.com` | Nao implantado em HOMOL |
| Pagamento | `https://hosped-pagamento-dev.onrender.com` | `https://hotel-microservicos.onrender.com` |

Observacao: o servico Render `hosped-pagamento-homol` manteve a URL publica `https://hotel-microservicos.onrender.com`.

O workflow chama o deploy hook com `imgURL=<imagem-publicada-no-Docker-Hub>`.

Variaveis obrigatorias em cada Web Service no Render:

```text
PORT=10000
SERVER_PORT=10000
```

O Render injeta/espera uma porta HTTP para detectar que o Web Service esta ativo. Nos microsservicos Spring Boot, `SERVER_PORT` e usado como fallback e `PORT` tem prioridade em `server.port`. Configurar os dois evita falhas como `No open ports detected` e garante que o health check do Render encontre `/actuator/health`.

Configure essas variaveis em:

```text
Render > Web Service > Environment
```

Servicos que precisam dessas variaveis:

```text
hosped-users-dev
hosped-quarto-dev
hosped-pagamento-dev
reserva-service-dev
hosped-gateway-dev
hosped-quarto-homol
hosped-pagamento-homol
hosped-reserva-homol
```

Localmente, o Docker Compose mapeia portas fixas como `8080`, `8081`, `8083`, `8084` e `8085`; no Render, todos os containers devem escutar a porta configurada por `PORT`.

Validacao apos deploy no Render:

```bash
curl https://hosped-users-dev.onrender.com/actuator/health
curl https://hosped-quarto-dev.onrender.com/actuator/health
curl https://hosped-pagamento-dev.onrender.com/actuator/health
curl https://reserva-service-dev.onrender.com/actuator/health
curl https://hosped-gateway-dev.onrender.com/actuator/health
```

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

### Observabilidade Local

Prometheus coleta:

- `hosped-gateway:8080/actuator/prometheus`
- `reserva-service:8080/actuator/prometheus`
- `hosped-quarto:8080/actuator/prometheus`
- `hosped-users:8080/actuator/prometheus`
- `hosped-pagamento:8080/actuator/prometheus`

Grafana provisiona automaticamente o datasource Prometheus e o dashboard `Microsservicos do Hotel`, com paineis para:

- status dos servicos;
- requisicoes HTTP;
- taxa de erro HTTP 5xx;
- tempo medio de resposta;
- memoria JVM;
- CPU do processo.

### Observabilidade Externa

Para permitir acesso sem depender do ambiente local, a solucao preparada e hospedar Prometheus e Grafana como Web Services no Render:

| Ferramenta | Ambiente | URL | Arquivos |
| --- | --- | --- | --- |
| Prometheus externo | DEV | `https://hotel-prometheus-dev.onrender.com` | `observability/prometheus/Dockerfile.render`, `observability/prometheus/prometheus-render-dev.yml` |
| Grafana externo | DEV | `https://hotel-grafana-dev.onrender.com` | `observability/grafana/Dockerfile.render`, `observability/grafana/provisioning-external/**`, `observability/grafana/dashboards/**` |
| Prometheus externo | HOMOL | `https://hotel-prometheus-homol.onrender.com` | `observability/prometheus/Dockerfile.render-homol`, `observability/prometheus/prometheus-render-homol.yml` |
| Grafana externo | HOMOL | `https://hotel-grafana-homol.onrender.com` | `observability/grafana/Dockerfile.render`, `observability/grafana/provisioning-external/**`, `observability/grafana/dashboards/**` |

O Prometheus externo DEV coleta os endpoints publicos DEV:

```text
https://hosped-gateway-dev.onrender.com/actuator/prometheus
https://reserva-service-dev.onrender.com/actuator/prometheus
https://hosped-quarto-dev.onrender.com/actuator/prometheus
https://hosped-users-dev.onrender.com/actuator/prometheus
https://hosped-pagamento-dev.onrender.com/actuator/prometheus
```

O Prometheus externo HOMOL coleta os endpoints publicos HOMOL:

```text
https://hosped-reserva-homol.onrender.com/actuator/prometheus
https://hosped-quarto-homol.onrender.com/actuator/prometheus
https://hotel-microservicos.onrender.com/actuator/prometheus
```

Criacao manual no Render (DEV):

1. Crie um Web Service `hotel-prometheus-dev` a partir deste repositorio.
2. Configure Dockerfile path como `observability/prometheus/Dockerfile.render`.
3. Configure `PORT=10000`.
4. Crie um Web Service `hotel-grafana-dev` a partir deste repositorio.
5. Configure Dockerfile path como `observability/grafana/Dockerfile.render`.
6. Configure variaveis do Grafana:

```text
PORT=10000
GF_SERVER_HTTP_PORT=10000
GF_SECURITY_ADMIN_USER=<usuario-admin>
GF_SECURITY_ADMIN_PASSWORD=<senha-admin>
PROMETHEUS_DATASOURCE_URL=https://hotel-prometheus-dev.onrender.com
```

Criacao manual no Render (HOMOL):

1. Crie um Web Service `hotel-prometheus-homol` a partir deste repositorio.
2. Configure Dockerfile path como `observability/prometheus/Dockerfile.render-homol`.
3. Configure `PORT=10000`.
4. Crie um Web Service `hotel-grafana-homol` a partir deste repositorio.
5. Configure Dockerfile path como `observability/grafana/Dockerfile.render`.
6. Configure variaveis do Grafana:

```text
PORT=10000
GF_SERVER_HTTP_PORT=10000
GF_SECURITY_ADMIN_USER=<usuario-admin>
GF_SECURITY_ADMIN_PASSWORD=<senha-admin>
PROMETHEUS_DATASOURCE_URL=https://hotel-prometheus-homol.onrender.com
```

O Grafana externo provisiona automaticamente o datasource `Prometheus` e o dashboard `Microsservicos do Hotel`.

Validacao externa DEV:

```bash
curl https://hotel-prometheus-dev.onrender.com/-/ready
curl https://hotel-prometheus-dev.onrender.com/api/v1/targets
```

Validacao externa HOMOL:

```bash
curl https://hotel-prometheus-homol.onrender.com/-/ready
curl https://hotel-prometheus-homol.onrender.com/api/v1/targets
```

No Grafana HOMOL, acesse `https://hotel-grafana-homol.onrender.com`, faca login com o usuario/senha configurados e abra:

```text
Dashboards > Microsservicos do Hotel > Microsservicos do Hotel
```

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

Validar endpoints HOMOL no Render:

```bash
curl https://hotel-microservicos.onrender.com/actuator/health
curl https://hosped-quarto-homol.onrender.com/actuator/health
curl https://hosped-reserva-homol.onrender.com/actuator/health

curl https://hotel-microservicos.onrender.com/actuator/prometheus
curl https://hosped-quarto-homol.onrender.com/actuator/prometheus
curl https://hosped-reserva-homol.onrender.com/actuator/prometheus
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
curl -I https://hotel-microservicos.onrender.com/swagger-ui.html
curl -I https://hosped-quarto-homol.onrender.com/swagger-ui.html
curl -I https://hosped-reserva-homol.onrender.com/swagger-ui.html

curl -I https://hotel-microservicos.onrender.com/api-docs
curl -I https://hosped-quarto-homol.onrender.com/api-docs
curl -I https://hosped-reserva-homol.onrender.com/api-docs
```

Esperado em HOMOL: `404` para `/swagger-ui.html` e `/api-docs`.

## Atendimento aos requisitos do Trabalho Pratico 2

| Requisito | Atendimento |
| --- | --- |
| Git Flow | Documentado com `main`, `develop` e `feature/*`; workflows reagem a essas branches. |
| CI com GitHub Actions | Criados workflows individuais por microsservico. |
| Build automatizado | `mvn clean verify` em cada pipeline. |
| Testes automatizados | Testes Maven executados em cada pipeline. |
| SonarCloud | Etapa dedicada no workflow reutilizavel; roda por microsservico somente na branch `main`, quando `SONAR_TOKEN` e vars Sonar existem. |
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
| Prometheus e Grafana | Compose local e configuracao externa Render-ready para Prometheus/Grafana DEV e HOMOL, com datasource e dashboard provisionados. |
| Pipeline separado por microsservico | Cinco workflows especificos criados. |
| README | Este documento descreve arquitetura, execucao, CI/CD, secrets, ambientes e entregaveis. |
| URLs DEV/HOMOL | URLs DEV e HOMOL documentadas para pagamento, quarto e reserva. |
| Dashboards | Dashboard `Microsservicos do Hotel` provisionado no Grafana DEV e HOMOL. |

## Pendencias Externas

- Manter `SONAR_TOKEN`, `SONAR_HOST_URL`, `SONAR_ORGANIZATION` e `SONAR_PROJECT_KEY_*` configurados no GitHub para publicar os relatorios Sonar na branch `main`.
- Manter credenciais reais fora do repositorio, usando GitHub Actions Secrets e Environment Variables do Render.
