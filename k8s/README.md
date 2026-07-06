[README-k8s-final.md](https://github.com/user-attachments/files/29709381/README-k8s-final.md)
# Kubernetes - Deploy Local com Minikube

## Pre-requisitos

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
├── Hosped-quarto/
├── Hosped-users/
└── reserva-service/
```

Cada ms possui seus proprios arquivos de manifest. A pasta `infra/` contem PostgreSQL e RabbitMQ compartilhados por todos os ms.

## Descricao dos manifests

**ConfigMap** — variaveis de ambiente nao sensiveis como URLs dos servicos internos, portas e nomes. No cluster Kubernetes os servicos se comunicam pelos nomes dos Services (ex: `hosped-pagamento-service:8083`) em vez de `localhost`.

**Secret** — dados sensiveis como senhas de banco de dados e tokens JWT. Os valores sao injetados nos containers em tempo de execucao e nunca ficam expostos no codigo.

**Deployment** — define como o ms roda: qual imagem Docker usar, numero de replicas (2 por ms) e health checks via `/actuator/health`. O Kubernetes monitora os pods e reinicia automaticamente se ficarem fora do ar.

**Service** — cria um endereco interno fixo do tipo `ClusterIP` para os ms se comunicarem dentro do cluster. Sem o Service, os pods nao teriam endereco estavel pois o IP muda a cada reinicio.

**Ingress** — expoe cada ms externamente via dominio local usando nginx:

```
pagamento.local → hosped-pagamento-service:8083
gateway.local   → hosped-gateway-service:8080
quarto.local    → hosped-quarto-service:8084
users.local     → hosped-users-service:8085
reserva.local   → reserva-service-service:8081
```

## Como executar

### 1. Iniciar o Minikube

```bash
minikube start --memory=6144 --cpus=4
```

Iniciado com 6GB de RAM e 4 CPUs para suportar todos os pods simultaneamente.

### 2. Habilitar o Ingress

```bash
minikube addons enable ingress
```

### 3. Aplicar a infraestrutura

```bash
kubectl apply -f k8s/infra/
```

Aguardar PostgreSQL e RabbitMQ ficarem prontos:

```bash
kubectl get pods -w
```

### 4. Aplicar os microsservicos

```bash
kubectl apply -f k8s/hosped-pagamento/
kubectl apply -f k8s/hosped-gateway/
kubectl apply -f k8s/Hosped-quarto/
kubectl apply -f k8s/Hosped-users/
kubectl apply -f k8s/reserva-service/
```

### 5. Configurar o arquivo hosts (Windows)

Abrir o Bloco de Notas como administrador e adicionar no final do arquivo `C:\Windows\System32\drivers\etc\hosts`:

```
127.0.0.1 pagamento.local
127.0.0.1 gateway.local
127.0.0.1 quarto.local
127.0.0.1 users.local
127.0.0.1 reserva.local
```

Este passo simula um servidor DNS local — faz com que o sistema operacional resolva esses dominios para `127.0.0.1` onde o Minikube tunnel estara ouvindo. Em producao esses dominios seriam resolvidos por um servidor DNS real apontando para o IP do cluster.

### 6. Iniciar o tunnel

Em um terminal separado, manter rodando:

```bash
minikube tunnel
```

O tunnel cria uma ponte entre a maquina e o cluster Kubernetes permitindo que o trafego do Ingress seja roteado corretamente:

```
navegador → dominio.local → 127.0.0.1 → tunnel → Ingress → Service → Pod
```

### 7. Verificar os pods

```bash
kubectl get pods
```

Todos os pods devem estar com status `1/1 Running`.

### 8. Testar os servicos

```
http://pagamento.local/actuator/health
http://gateway.local/actuator/health
http://quarto.local/actuator/health
http://users.local/actuator/health
http://reserva.local/actuator/health
```

## Replicacao

Cada microsservico e executado com 2 replicas conforme definido nos Deployments:

```yaml
spec:
  replicas: 2
```

Para verificar os pods em execucao:

```bash
kubectl get pods
```

## Comandos uteis

```bash
# Ver todos os pods
kubectl get pods

# Ver logs de um pod
kubectl logs <nome-do-pod>

# Ver detalhes de um pod
kubectl describe pod <nome-do-pod>

# Reiniciar um deployment
kubectl rollout restart deployment/<nome-do-deployment>

# Parar o Minikube
minikube stop
```
