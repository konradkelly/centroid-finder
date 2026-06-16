# Backend deployment

Terraform layout for ECS Fargate backend per `docs/aws-deployment-spec.md`.

## Prerequisites

- AWS CLI configured with deploy permissions
- Terraform >= 1.5
- Docker (for building and pushing the app image)

## 1. Bootstrap remote state (one-time)

```bash
cd terraform/bootstrap
cp terraform.tfvars.example terraform.tfvars
# Edit state_bucket_name to a globally unique value

terraform init
terraform apply
```

## 2. Configure prod environment

```bash
cd terraform/environments/prod
cp terraform.tfvars.example terraform.tfvars
cp backend.tf.example backend.tf   # edit bucket name to match bootstrap output
# Edit github_org / github_repo in terraform.tfvars
```

First apply **without** the ECS service (default `create_ecs_service = false` in tfvars.example):

```bash
terraform init
terraform apply
```

## 3. Build and push the first image

```bash
# From repo root
mvn -B package

aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account>.dkr.ecr.us-east-1.amazonaws.com

export ECR_URI=$(terraform -chdir=terraform/environments/prod output -raw ecr_repository_url)
docker build -t "$ECR_URI:bootstrap" .
docker push "$ECR_URI:bootstrap"
```

## 4. Enable ECS service

In `terraform.tfvars`:

```hcl
create_ecs_service = true
image_tag          = "bootstrap"
```

```bash
terraform apply
```

## 5. Seed EFS with sample videos

One-off EC2 + SSM Session Manager: stage `videos/ensantina.mp4` via S3, mount EFS on the instance, copy, terminate. Full runbook: [backend-deployment.md §5](../docs/backend-deployment.md#5-seed-efs-with-videos) (subsections 5a–5e).

## 6. Verify

```bash
ALB=$(terraform -chdir=terraform/environments/prod output -raw alb_dns_name)
curl -sf "http://$ALB/actuator/health"
curl -sf "http://$ALB/api/videos"
```

## GitHub Actions

After apply, set these GitHub repository settings:

| Type | Name | Value |
|------|------|-------|
| Secret | `AWS_ROLE_ARN` | `terraform output github_deploy_role_arn` |
| Variable | `AWS_REGION` | `us-east-1` |
| Variable | `ECR_REPOSITORY` | ECR repo name from output |
| Variable | `ECS_CLUSTER` | from output |
| Variable | `ECS_SERVICE` | from output |
| Variable | `ECS_TASK_FAMILY` | from output |

Deploy workflow (`deploy.yml`) is added separately per spec §13.2.
