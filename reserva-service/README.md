# Reserva Service

Microsserviço backend em Java/Spring Boot para gestão de hóspedes e reservas em um sistema de hotelaria orientado a microsserviços, desenvolvido em contexto acadêmico por estudantes de Engenharia de Software da UDESC. O serviço funciona sozinho, possui banco próprio, API própria, testes próprios e demonstra explicitamente dois padrões importantes de arquitetura distribuída:

- `Event-Driven` com RabbitMQ
- `Circuit Breaker` com Resilience4j

O foco é acadêmico-profissional: a aplicação é executável localmente, organizada como projeto real e preparada para futura integração com `quarto-service`, `pagamento-service` e `autenticacao-service`.

## Responsabilidades

O `reserva-service` é responsável por:

- cadastro, consulta, atualização e exclusão de hóspedes
- criação e consulta de reservas
- histórico de reservas por hóspede
- controle de status da reserva
- check-in e check-out
- comentários de encerramento
- modelagem do estado de pagamento
- publicação de eventos de domínio relacionados às reservas

O serviço:

- é dono dos dados de `hóspede` e `reserva`
- não é dono do domínio de `quarto`
- não é dono do domínio de `pagamento`
- não implementa autenticação completa nesta versão

## Stack utilizada

- Java 21
- Spring Boot
- Maven
- Spring Web
- Spring Data JPA
- Spring Validation
- PostgreSQL
- Flyway
- RabbitMQ / Spring AMQP
- Resilience4j
- Springdoc OpenAPI / Swagger
- Actuator
- WebClient
- JUnit 5 / Mockito
- Docker / Docker Compose
- Lombok

## Estrutura do projeto

```text
src/main/java/br/edu/udesc/reservaservice
├── api
│   ├── controller
│   ├── request
│   └── response
├── application
│   ├── dto
│   ├── mapper
│   └── service
├── domain
│   ├── enum
│   ├── event
│   ├── exception
│   ├── model
│   └── repository
├── exception
├── infrastructure
│   ├── config
│   ├── integration
│   │   ├── client
│   │   ├── fallback
│   │   └── gateway
│   ├── messaging
│   │   ├── consumer
│   │   ├── payload
│   │   └── producer
│   └── security
└── shared
    ├── base
    └── util
```

## Modelagem principal

### Hóspede

- `id`
- `nomeCompleto`
- `cpf`
- `email`
- `telefone`
- `ativo`
- `criadoAt`
- `atualizadoAt`

### Reserva

- `id`
- `hospede`
- `quartoId`
- `quartoNumero`
- `checkInData`
- `checkOutData`
- `reservaStatus`
- `pagamentoModo`
- `pagamentoStatus`
- `criadoAt`
- `atualizadoAt`
- `checkInRealizadoAt`
- `checkOutRealizadoAt`

### ReservaComentario

- `id`
- `reserva`
- `comentario`
- `criadoAt`

### ReservaStatusHistorico

- `id`
- `reserva`
- `statusAnterior`
- `statusNovo`
- `motivo`
- `atualizadoAt`

### EventoPublicacaoLog

- `id`
- `tipoEvento`
- `agregadoId`
- `payloadResumo`
- `publicadoEm`
- `statusPublicacao`
- `mensagemErro`

## Regras de negócio implementadas

### Hóspedes

- nome completo obrigatório
- CPF obrigatório, normalizado e único
- e-mail obrigatório
- telefone obrigatório
- exclusão bloqueada quando existir qualquer reserva vinculada, inclusive futura ou histórica

### Reservas

- hóspede responsável obrigatório
- `checkInData` e `checkOutData` obrigatórios
- `checkOutData` deve ser posterior a `checkInData`
- `quartoId` é referência externa, não entidade local
- comentário de encerramento só pode ser registrado em reserva `ENCERRADA`

### Fluxo de status

Para `PAGO_NO_HOTEL`:

- `PENDENTE -> ATIVA -> ENCERRADA`

Para `PAGO_ANTECIPADO`:

- `PENDENTE -> PAGA -> ATIVA -> ENCERRADA`

Regras adicionais:

- não permite check-in em reserva encerrada
- não permite check-out em reserva ainda não iniciada
- não permite check-in antecipado sem pagamento confirmado
- registra histórico de mudanças de status em tabela própria

## Event-Driven com RabbitMQ

O serviço publica eventos de domínio reais no RabbitMQ através da exchange:

- `reserva.eventos.exchange`

Routing keys publicadas:

- `reserva.criada`
- `reserva.checkin.realizado`
- `reserva.checkout.realizado`
- `reserva.pagamento.confirmado`

Payload padrão dos eventos:

- `eventId`
- `eventType`
- `occurredAt`
- `reservaId`
- `hospedeId`
- `quartoId`
- `reservaStatus`
- `pagamentoModo`
- `pagamentoStatus`

Eventos publicados:

- `RESERVA_CRIADA`
- `CHECKIN_REALIZADO`
- `CHECKOUT_REALIZADO`
- `PAGAMENTO_RESERVA_CONFIRMADO`

### Como foi implementado

- os serviços de aplicação disparam `ReservaDomainEvent`
- o `SpringReservaDomainEventPublisher` publica eventos internos do Spring
- o `ReservaEventoRabbitProducer` envia os eventos ao RabbitMQ após commit da transação
- cada tentativa de publicação gera registro em `evento_publicacao_log`
- existe uma fila local de monitoramento `reserva.eventos.monitoramento.queue` para observabilidade

### Consumer implementado

O projeto também possui um consumer real de integração externa:

- fila: `pagamento.reserva.confirmado.queue`
- exchange: `integracao.eventos.exchange`
- routing key: `pagamento.reserva.confirmado`

Esse consumer recebe um evento externo simulado de pagamento confirmado e atualiza a reserva antecipada de forma idempotente básica.

Para facilitar a demonstração local sem outro microsserviço pronto, existe o endpoint:

- `POST /api/v1/tecnico/simulacoes/pagamento-service/reservas/{reservaId}/confirmacao`

Ele publica o evento externo simulado na exchange de integração.

## Circuit Breaker com Resilience4j

O padrão foi aplicado em dois gateways HTTP de integração futura:

- `HttpQuartoDisponibilidadeGateway`
- `HttpPagamentoGateway`

Os circuit breakers configurados são:

- `quartoDisponibilidade`
- `pagamentoService`

Também há `TimeLimiter` para ambos.

### Estratégia dos gateways

Cada integração possui:

- interface de gateway
- implementação `fake` para execução standalone
- implementação `http` protegida com Circuit Breaker
- fallback explícito

Seleção por propriedade:

- `app.integracao.quarto.strategy=fake|http`
- `app.integracao.pagamento.strategy=fake|http`

### Fallback do quarto-service

No fallback do quarto-service, o comportamento padrão é acadêmico e seguro para demonstração:

- quando o serviço externo falha, o fallback informa indisponibilidade controlada
- por padrão, a propriedade `app.integracao.quarto.permitir-reserva-sem-validacao-externa=true` permite continuar a operação em modo standalone

### Fallback do pagamento-service

No fallback do pagamento-service, a resposta é controlada:

- `statusExterno = FALLBACK`
- `fallbackAcionado = true`
- a API continua funcionando sem derrubar o serviço inteiro

### Como demonstrar o Circuit Breaker

1. Suba o serviço com profile `http-demo`
2. Ative indisponibilidade simulada:

```bash
curl -X POST http://localhost:8080/api/v1/tecnico/simulacoes/quarto-service/indisponivel \
  -H "Content-Type: application/json" \
  -d '{"ativo":true}'
```

3. Chame o endpoint protegido:

```bash
curl "http://localhost:8080/api/v1/tecnico/quartos/11111111-1111-1111-1111-111111111111/disponibilidade?checkInData=2026-05-01&checkOutData=2026-05-03"
```

4. O retorno indicará `fallbackAcionado=true`

O mesmo vale para a simulação de pagamento:

```bash
curl -X POST http://localhost:8080/api/v1/tecnico/simulacoes/pagamento-service/indisponivel \
  -H "Content-Type: application/json" \
  -d '{"ativo":true}'
```

```bash
curl http://localhost:8080/api/v1/tecnico/pagamentos/11111111-1111-1111-1111-111111111111/status-integracao
```

## Endpoints principais

### Hóspedes

- `POST /api/v1/hospedes`
- `GET /api/v1/hospedes`
- `GET /api/v1/hospedes/{id}`
- `GET /api/v1/hospedes/cpf/{cpf}`
- `PUT /api/v1/hospedes/{id}`
- `DELETE /api/v1/hospedes/{id}`

### Reservas

- `POST /api/v1/reservas`
- `GET /api/v1/reservas`
- `GET /api/v1/reservas/{id}`
- `GET /api/v1/reservas/hospede/{hospedeId}`
- `POST /api/v1/reservas/{id}/check-in`
- `POST /api/v1/reservas/{id}/check-out`
- `POST /api/v1/reservas/{id}/confirma-pagamento`
- `POST /api/v1/reservas/{id}/comentarios`

### Técnicos

- `GET /actuator/health`
- `GET /api/v1/tecnico/quartos/{quartoId}/disponibilidade`
- `GET /api/v1/tecnico/pagamentos/{reservaId}/status-integracao`
- `POST /api/v1/tecnico/simulacoes/quarto-service/indisponivel`
- `POST /api/v1/tecnico/simulacoes/pagamento-service/indisponivel`
- `POST /api/v1/tecnico/simulacoes/pagamento-service/reservas/{reservaId}/confirmacao`
- `GET /api/v1/tecnico/simulacoes/quarto-service/disponibilidade/{quartoId}`
- `GET /api/v1/tecnico/simulacoes/pagamento-service/reservas/{reservaId}/status`

## Documentação OpenAPI

Com a aplicação em execução:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Execução local

### Pré-requisitos

- Java 21
- Maven 3.9+
- PostgreSQL
- RabbitMQ

### Variáveis de ambiente

Exemplo:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=reserva_db
export DB_USER=reserva_user
export DB_PASSWORD=reserva_pass

export RABBITMQ_HOST=localhost
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest
```

### Rodando com Maven

Modo standalone, usando gateways fake:

```bash
mvn spring-boot:run
```

Modo demonstrando gateways HTTP com Circuit Breaker:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=http-demo
```

## Execução com Docker

O projeto inclui:

- `reserva-postgres`
- `quarto-postgres`
- `users-postgres`
- `rabbitmq`
- `reserva-service`
- `quarto-service`
- `users-service`

Subida completa a partir da raiz do projeto:

```bash
docker compose up --build
```

Serviços disponíveis:

- API reservas: `http://localhost:8081`
- API quartos: `http://localhost:8082`
- API users: `http://localhost:8083`
- RabbitMQ Management: `http://localhost:15672`
- PostgreSQL reservas: `localhost:5432`
- PostgreSQL quartos: `localhost:5433`
- PostgreSQL users: `localhost:5434`

## Testes

Rodar testes:

```bash
mvn test
```

Cobertura priorizada:

- CPF duplicado
- exclusão de hóspede com reserva vinculada
- criação de reserva com datas inválidas
- fluxo de status para pagamento no hotel
- fluxo de status para pagamento antecipado
- check-in e check-out inválidos
- publicação de eventos
- consumer de pagamento confirmado
- fallback dos gateways HTTP
- controllers principais com MockMvc

## Tratamento de erros

As respostas de erro são padronizadas com:

- `timestamp`
- `status`
- `error`
- `message`
- `path`

Exceções específicas:

- `HospedeNaoEncontradoException`
- `ReservaNaoEncontradaException`
- `CpfDuplicadoException`
- `DataReservaInvalidaException`
- `AlteracaoStatusInvalidaException`
- `ExclusaoHospedeNaoPermitidaException`
- `RegraDeNegocioException`
- `IntegracaoExternaException`

## Preparação para futuras integrações

### autenticacao-service

- existe a interface `AuthContextProvider`
- o serviço já está pronto para receber filtros/interceptadores de segurança
- o código de domínio não depende do mecanismo atual de autenticação

### quarto-service

- a reserva mantém apenas referência externa por `quartoId`
- existe gateway fake
- existe gateway HTTP protegido por Circuit Breaker
- existe simulador local para demonstrar indisponibilidade e fallback

### pagamento-service

- a confirmação de pagamento da reserva é modelada internamente
- existe gateway fake
- existe gateway HTTP protegido por Circuit Breaker
- existe consumer real para evento externo simulado de pagamento confirmado

### mensageria

- RabbitMQ configurado
- eventos publicados em exchange dedicada
- consumer de integração preparado para evolução futura
- fila de monitoramento local para observabilidade

## Observação sobre o ambiente atual

Durante esta implementação, o workspace estava vazio e o shell disponível não possuía `java`, `mvn` e `docker` instalados/configurados. Por isso, o projeto foi gerado completo, mas a validação executável local depende da disponibilização dessas ferramentas no ambiente.
