# CI/CD Validation Guide — Video2Frames Infrastructure (snippet para o validador)

Este arquivo contém um resumo curto e comandos copy/paste para que o validador (o repositório CI externo) possa ser configurado corretamente e testado.

## Objetivo
Fornecer os valores, outputs e comandos que o repositório CI `video-workflow` precisa para construir a imagem, push para ECR Public e aplicar o Terraform que cria os recursos Kubernetes no EKS.

---

## 1) Variáveis que o repositório CI precisa configurar

Repository Variables (não-secretas) — GitHub Actions > Settings > Variables
- AWS_REGION
  - Valor recomendado: `us-east-1` (ver `terraform/vars.tf`)
- ECR_PUBLIC_ALIAS
  - Alias público do ECR (ex.: `p6c0d2v5`). Você _pode_ preencher este valor pegando o output `ecr_public_alias` (ver abaixo)
- EKS_CLUSTER_NAME
  - Nome do EKS. Use o output `cluster_name` do Terraform.

Repository Secrets — GitHub Actions > Settings > Secrets
- AWS_ACCESS_KEY
- AWS_ACCESS_SECRET
- AWS_ACCOUNT_ID
- DB_USER
- DB_PASSWORD
- SNS_TOPIC_ARN
- SONAR_TOKEN

> Observação: o workflow atual constrói a imagem e faz `terraform apply` — as credenciais precisam ter permissões para ler estados remotos (S3), manipular recursos AWS e executar `eks:DescribeCluster`.

---

## 2) Outputs do Terraform que o validador deve extrair (depois do `terraform apply` na pasta `terraform`)
Use estes comandos no diretório `terraform` onde o `apply` foi executado:

```bash
terraform output -raw video_processor_ecr_repository_uri
terraform output -raw ecr_public_alias
terraform output -raw cluster_name
terraform output -raw database_uri
terraform output -raw order_created_topic_arn
```

- `video_processor_ecr_repository_uri` será algo como `public.ecr.aws/<alias>/video2frames-video-processor`.
- `ecr_public_alias` é apenas o `<alias>` (ex: `p6c0d2v5`).

---

## 3) Atenção: mismatch entre repositório ECR criado pela infra e o nome esperado pelo CI
- Infra cria: `video2frames-video-processor` (`public.ecr.aws/<alias>/video2frames-video-processor`).
- CI atual (no workflow) empurra para: `public.ecr.aws/${ECR_PUBLIC_ALIAS}/video-workflow`.

Recomendação (escolha uma):
1) Alinhar o CI para usar o repositório criado pela infra (recomendado)
   - Defina no repositório CI `APP_IMAGE` para: `public.ecr.aws/${ECR_PUBLIC_ALIAS}/video2frames-video-processor` OU
   - Atualize a variável `ECR_PUBLIC_ALIAS` e `APP_IMAGE` conforme o output `video_processor_ecr_repository_uri`.

2) (menos recomendado) Alterar infra para criar `video-workflow` em vez de `video2frames-video-processor`.

---

## 4) Comandos de validação passo-a-passo

A) Testar `terraform plan` (local ou no runner)
```bash
cd terraform
terraform init
terraform plan -var region="$AWS_REGION" -var app_image="$APP_IMAGE:$APP_IMAGE_TAG" \
  -var db_user="$DB_USER" \
  -var db_password="$DB_PASSWORD" -var sns_topic_arn="$SNS_TOPIC_ARN" \
  -var aws_access_key="$AWS_ACCESS_KEY" -var aws_access_secret="$AWS_ACCESS_SECRET" \
  -var aws_region="$AWS_REGION" -var aws_account_id="$AWS_ACCOUNT_ID" -var force_rollout="$(date +%s)"
```

B) Confirmar repositório ECR criado e imagens (após CI rodar)
```bash
# listar repositorios publicos
aws ecr-public describe-repositories --region $AWS_REGION --repository-names "video2frames-video-processor"
# listar imagens
aws ecr-public describe-images --region $AWS_REGION --repository-name video2frames-video-processor
```

C) Terraform Apply (exemplo)
```bash
cd terraform
terraform apply -auto-approve -var region="$AWS_REGION" -var app_image="$APP_IMAGE:$APP_IMAGE_TAG" \
  -var db_user="$DB_USER" -var db_password="$DB_PASSWORD" -var sns_topic_arn="$SNS_TOPIC_ARN" \
  -var aws_access_key="$AWS_ACCESS_KEY" -var aws_access_secret="$AWS_ACCESS_SECRET" -var aws_region="$AWS_REGION" -var aws_account_id="$AWS_ACCOUNT_ID"

terraform output -json > infra-outputs.json
```

D) Validar o cluster / pods / ingress (pós-apply)
```bash
aws eks --region $AWS_REGION update-kubeconfig --name $EKS_CLUSTER_NAME
kubectl get nodes
kubectl get pods -n hackathon
kubectl get ingress -n hackathon
kubectl describe deployment video-workflow-deployment -n hackathon
kubectl logs -l app=video-workflow-app -n hackathon --tail=200
```

E) Verificar Lambda `hackathon-db-init` e logs (se aplicável)
```bash
aws logs filter-log-events --log-group-name /aws/lambda/hackathon-db-init --limit 50 --region $AWS_REGION
```

F) Verificar que a tabela existe (se você tem acesso à rede ao RDS)
```bash
DB_ENDPOINT=$(terraform output -raw database_uri)
DB_HOST=$(echo "$DB_ENDPOINT" | cut -d: -f1)
DB_PORT=$(echo "$DB_ENDPOINT" | cut -d: -f2)
PGPASSWORD="$DB_PASSWORD" psql "host=$DB_HOST port=$DB_PORT dbname=video_status user=$DB_USER sslmode=require" -c '\dt'
```

---

## 5) Permissões mínimas sugeridas para as credenciais do CI (exemplo inicial)
- `s3:GetObject`, `s3:ListBucket` (se usar terraform_remote_state em S3)
- `eks:DescribeCluster`
- ECR Public: `ecr-public:CreateRepository`, `ecr-public:DescribeRepositories`, `ecr-public:DescribeImages`, `ecr-public:PutImage`
- `iam:*` (se seu terraform cria roles/IRSA) — no início pode-se usar `AdministratorAccess` para validação e depois restringir.

---

## 6) Snippet pronto para colar no repositório CI (variáveis & secrets)

Repository Variables (GitHub Actions > Settings > Variables)
```
AWS_REGION=us-east-1
ECR_PUBLIC_ALIAS=<valor extraido com `terraform output -raw ecr_public_alias`>
EKS_CLUSTER_NAME=<valor extraido com `terraform output -raw cluster_name`>
```

Repository Secrets (GitHub Actions > Settings > Secrets)
```
AWS_ACCESS_KEY
AWS_ACCESS_SECRET
AWS_ACCOUNT_ID
DB_USER
DB_PASSWORD
SNS_TOPIC_ARN
SONAR_TOKEN
```

---

## 7) Observações finais
- Recomendo alinhar `APP_IMAGE` no workflow do repo CI com o output `video_processor_ecr_repository_uri` (evita mismatch de nome de repositório).
- Se quiser, eu adapto o arquivo `.github/workflows/ci-cd.yml` para usar diretamente `terraform output -raw video_processor_ecr_repository_uri` (por exemplo via um step que consulta S3/TF state) e definir `APP_IMAGE` dinamicamente.

---

Se quiser que eu salve este conteúdo em outro arquivo/nome ou gere um README em inglês, diga qual opção prefere que eu aplique agora.
