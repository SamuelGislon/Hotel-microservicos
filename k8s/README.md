# Kubernetes - Deploy Local com Minikube

## Objetivo

Deploy dos microsserviços do sistema Hotel em um cluster Kubernetes local utilizando Minikube, com múltiplas réplicas, observabilidade e configuração via ConfigMap e Secret.

## Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e rodando
- [Minikube](https://minikube.sigs.k8s.io/docs/start/) instalado
- [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/) instalado

## Estrutura de arquivos

```
k8s/
├── infra/
│   ├── postgres-deployment.yml
│   └── rabbitmq-deployment.yml
├── hosped-pagamento/
│   ├── configmap.yml
│   ├── secret.yml
│   ├── deployment.yml
│   ├── service.yml
│   └── ingress.yml
├── hosped-gateway/
│   ├── configmap.yml
│   ├── secret.yml
│   ├── deployment.yml
│   ├── service.yml
│   └── ingress.yml
├── Hosped-quarto/
│   ├── configmap.yml
│   ├── secret.yml
│   ├── deployment.yml
│   ├── service.yml
│   └── ingress.yml
├── Hosped-users/
│   ├── configmap.yml
│   ├── secret.yml
│   ├── deployment.yml
│   ├── service.yml
│   └── ingress.yml
└── reserva-service/
    ├── configmap.yml
    ├── secret.yml
    ├── deployment.yml
    ├── service.yml
    └── ingress.yml
```

## Descrição dos manifests

Cada microsserviço possui seus próprios arquivos de configuração:

- **ConfigMap** — variáveis de ambiente não sensíveis (URLs, portas, nomes de serviços)
- **Secret** — dados sensíveis (senhas de banco, tokens JWT, credenciais)
- **Deployment** — define como o ms é executado: imagem Docker, número de réplicas (mínimo 2), health checks
- **Service** — expõe o ms internamente no cluster via ClusterIP
- **Ingress** — expõe o ms externamente via domínio local (ex: `pagamento.local`)

A pasta `infra/` contém os deployments compartilhados de infraestrutura (PostgreSQL e RabbitMQ) utilizados por todos os microsserviços.

## Como executar

### 1. Iniciar o Minikube

```bash
minikube start
```

### 2. Habilitar o Ingress

```bash
minikube addons enable ingress
```

### 3. Aplicar a infraestrutura

```bash
kubectl apply -f k8s/infra/postgres-deployment.yml
kubectl apply -f k8s/infra/rabbitmq-deployment.yml
```

Aguardar os pods ficarem prontos:

```bash
kubectl get pods -w
```

### 4. Aplicar os microsserviços

```bash
kubectl apply -f k8s/hosped-pagamento/
kubectl apply -f k8s/hosped-gateway/
kubectl apply -f k8s/Hosped-quarto/
kubectl apply -f k8s/Hosped-users/
kubectl apply -f k8s/reserva-service/
```

### 5. Verificar os pods

```bash
kubectl get pods
```

Todos os pods devem estar com status `Running` e `1/1` ou `2/2`.

### 6. Configurar o arquivo hosts (Windows)

Abrir PowerShell como administrador e executar:

```powershell
Add-Content -Path "C:\Windows\System32\drivers\etc\hosts" -Value "127.0.0.1 pagamento.local"
Add-Content -Path "C:\Windows\System32\drivers\etc\hosts" -Value "127.0.0.1 gateway.local"
Add-Content -Path "C:\Windows\System32\drivers\etc\hosts" -Value "127.0.0.1 quarto.local"
Add-Content -Path "C:\Windows\System32\drivers\etc\hosts" -Value "127.0.0.1 users.local"
Add-Content -Path "C:\Windows\System32\drivers\etc\hosts" -Value "127.0.0.1 reserva.local"
```

### 7. Iniciar o tunnel

Em um terminal separado, manter rodando:

```bash
minikube tunnel
```

### 8. Testar os serviços

```
http://pagamento.local/actuator/health
http://gateway.local/actuator/health
http://quarto.local/actuator/health
http://users.local/actuator/health
http://reserva.local/actuator/health
```

## Replicação

Cada microsserviço é executado com **2 réplicas** conforme definido nos Deployments:

```yaml
spec:
  replicas: 2
```

Para verificar os pods em execução:

```bash
kubectl get pods
```

## Configuração

### ConfigMap
Armazena variáveis de ambiente não sensíveis como URLs dos serviços internos, portas e nomes de aplicação. No cluster Kubernetes, os serviços se comunicam pelos nomes dos Services (ex: `hosped-pagamento-service:8083`) em vez de `localhost`.

### Secret
Armazena dados sensíveis como senhas de banco de dados, tokens JWT e credenciais de e-mail. Os valores são injetados nos containers em tempo de execução.

## Observabilidade

Cada microsserviço expõe métricas via:

```
http://<servico>.local/actuator/prometheus
```

O Prometheus e Grafana podem ser configurados dentro do cluster apontando para esses endpoints.

## Comandos úteis

```bash
# Ver todos os pods
kubectl get pods

# Ver logs de um pod
kubectl logs <nome-do-pod>

# Ver detalhes de um pod
kubectl describe pod <nome-do-pod>

# Ver todos os services
kubectl get services

# Ver todos os ingresses
kubectl get ingress

# Reiniciar um deployment
kubectl rollout restart deployment/<nome-do-deployment>

# Parar o Minikube
minikube stop
```