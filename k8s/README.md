# Kubernetes - Deploy Local com Minikube

Este diretorio contem os manifests do Trabalho Pratico 3 para executar a aplicacao de microsservicos em um cluster local Minikube, com Prometheus e Grafana dentro do proprio cluster.

## Estrutura

```text
k8s/
├── infra/
│   ├── postgres-deployment.yml
│   ├── postgres-secret.yml
│   ├── rabbitmq-deployment.yml
│   └── rabbitmq-secret.yml
├── hosped-gateway/
├── Hosped-users/
├── Hosped-quarto/
├── hosped-pagamento/
├── reserva-service/
└── observability/
    ├── prometheus-*.yml
    └── grafana-*.yml
```

Cada microsservico possui seus proprios arquivos `Deployment`, `Service`, `ConfigMap`, `Secret` e `Ingress`. Todos os deployments dos microsservicos usam `replicas: 2`.

## Pre-requisitos

- Docker instalado e rodando
- Minikube instalado
- kubectl instalado

## 1. Iniciar o Minikube

```bash
minikube start --memory=4096 --cpus=4
minikube addons enable ingress
```

Use mais memoria apenas se o Docker Desktop/WSL tiver limite suficiente configurado.

## 2. Preparar as imagens dos microsservicos

Os manifests usam as imagens `samuelgislon/<servico>:latest`. Se essas imagens ja estiverem publicadas e acessiveis no Docker Hub, o Kubernetes pode puxa-las diretamente. Para garantir a execucao local durante a avaliacao, construa as imagens dentro do Docker do Minikube:

```bash
eval $(minikube docker-env)

docker build -t samuelgislon/hosped-gateway:latest ./hosped-gateway
docker build -t samuelgislon/reserva-service:latest ./reserva-service
docker build -t samuelgislon/hosped-quarto:latest ./Hosped-quarto
docker build -t samuelgislon/hosped-users:latest ./Hosped-users
docker build -t samuelgislon/hosped-pagamento:latest ./hosped-pagamento
```

No PowerShell:

```powershell
minikube docker-env | Invoke-Expression
```

Depois execute os mesmos comandos `docker build`.

## 3. Aplicar os manifests

```bash
kubectl apply -f k8s/infra/
kubectl rollout status deployment/postgres
kubectl rollout status deployment/rabbitmq

kubectl apply -f k8s/Hosped-users/
kubectl apply -f k8s/Hosped-quarto/
kubectl apply -f k8s/hosped-pagamento/
kubectl apply -f k8s/reserva-service/
kubectl apply -f k8s/hosped-gateway/

kubectl apply -f k8s/observability/
```

Se o `kubectl apply` falhar ao baixar o OpenAPI do cluster com mensagem parecida com `failed to download openapi` ou `connection reset by peer`, tente aplicar sem a validacao local de schema:

```bash
kubectl apply --validate=false -f k8s/infra/
```

Se ainda falhar, o contexto/API do Minikube provavelmente esta instavel. Recrie o contexto e confira o cluster:

```bash
minikube status
minikube update-context
kubectl cluster-info
kubectl get nodes
```

Caso o cluster nao responda, reinicie o Minikube:

```bash
minikube stop
minikube start --memory=4096 --cpus=4
minikube addons enable ingress
```

## 4. Configurar o Ingress local

Descubra o IP do Minikube:

```bash
minikube ip
```

Em Linux ou driver com IP roteavel, adicione no `/etc/hosts`, trocando `<MINIKUBE_IP>` pelo valor retornado:

```text
<MINIKUBE_IP> gateway.local
<MINIKUBE_IP> users.local
<MINIKUBE_IP> quarto.local
<MINIKUBE_IP> pagamento.local
<MINIKUBE_IP> reserva.local
<MINIKUBE_IP> prometheus.local
<MINIKUBE_IP> grafana.local
```

Em Windows/macOS com Docker Desktop, se os hosts nao resolverem pelo IP do Minikube, mantenha um terminal aberto com:

```bash
minikube tunnel
```

Nesse caso, aponte os mesmos dominios para `127.0.0.1` no arquivo `hosts`.

## 5. Acessar a aplicacao

Endpoints de saude:

```text
http://gateway.local/actuator/health
http://users.local/actuator/health
http://quarto.local/actuator/health
http://pagamento.local/actuator/health
http://reserva.local/actuator/health
```

Swagger dos servicos REST, quando habilitado pelo profile:

```text
http://users.local/swagger-ui.html
http://quarto.local/swagger-ui.html
http://pagamento.local/swagger-ui.html
http://reserva.local/swagger-ui.html
```

## 6. Acessar Prometheus e Grafana

```text
Prometheus: http://prometheus.local
Grafana:    http://grafana.local
```

Credenciais locais do Grafana:

```text
usuario: admin
senha:   admin
```

O Grafana ja sobe com datasource apontando para `prometheus-service:9090` e com dashboard basico provisionado por ConfigMap.

Alternativa por port-forward, sem depender do Ingress:

```bash
kubectl port-forward svc/prometheus-service 9090:9090
kubectl port-forward svc/grafana-service 3000:3000
```

Depois acesse `http://localhost:9090` e `http://localhost:3000`.

## 7. Comandos uteis de validacao

Ver todos os recursos principais:

```bash
kubectl get pods
kubectl get deployments
kubectl get services
kubectl get ingress
```

Evidenciar multiplas replicas dos microsservicos:

```bash
kubectl get deployment hosped-gateway reserva-service hosped-quarto hosped-users hosped-pagamento
kubectl get pods -l 'app in (hosped-gateway,reserva-service,hosped-quarto,hosped-users,hosped-pagamento)'
```

Verificar Prometheus e Grafana:

```bash
kubectl get deployment prometheus grafana
kubectl get pods -l 'app in (prometheus,grafana)'
kubectl port-forward svc/prometheus-service 9090:9090
```

Com o port-forward ativo, confira os targets em:

```text
http://localhost:9090/targets
```

Logs e detalhes:

```bash
kubectl logs deployment/hosped-gateway --tail=100
kubectl logs deployment/reserva-service --tail=100
kubectl describe pod <nome-do-pod>
kubectl rollout status deployment/<nome-do-deployment>
```

Validar os manifests antes de aplicar:

```bash
kubectl apply --dry-run=client -f k8s/infra/
kubectl apply --dry-run=client -f k8s/Hosped-users/
kubectl apply --dry-run=client -f k8s/Hosped-quarto/
kubectl apply --dry-run=client -f k8s/hosped-pagamento/
kubectl apply --dry-run=client -f k8s/reserva-service/
kubectl apply --dry-run=client -f k8s/hosped-gateway/
kubectl apply --dry-run=client -f k8s/observability/
```

## 8. Secrets locais

Os `Secret` deste diretorio usam valores didaticos para execucao local. Nao coloque senhas, tokens, chaves JWT ou credenciais SMTP reais no repositorio.

Antes de usar em um ambiente real, substitua os valores com `kubectl create secret ...` ou por um mecanismo externo de secrets. O e-mail do microsservico de pagamentos fica desativado por padrao (`PAGAMENTO_EMAIL_ENABLED=false`), portanto `MAIL_USERNAME` e `MAIL_PASSWORD` ficam vazios no Kubernetes local.

## 9. Encerrar o ambiente

```bash
kubectl delete -f k8s/observability/
kubectl delete -f k8s/hosped-gateway/
kubectl delete -f k8s/reserva-service/
kubectl delete -f k8s/hosped-pagamento/
kubectl delete -f k8s/Hosped-quarto/
kubectl delete -f k8s/Hosped-users/
kubectl delete -f k8s/infra/
minikube stop
```
