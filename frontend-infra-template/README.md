# Frontend infrastructure template

Copy this folder into your **frontend repository** root (merge `terraform/` and `.github/` with your existing layout).

## What it creates

- Private S3 bucket for the React build
- CloudFront distribution with:
  - `/*` → S3 (static assets, SPA fallback)
  - `/api/*` → your backend ALB (no CORS needed in Spring)
- GitHub OIDC deploy role (S3 sync + CloudFront invalidation)

## Prerequisites

1. Backend deployed and healthy (`curl http://<alb>/api/videos`).
2. Backend bootstrap state bucket already exists (reuse the same S3 bucket with key `frontend/prod/terraform.tfstate`).
3. GitHub OIDC provider already exists in your AWS account (created by backend terraform).

## Quick start

```bash
# 1. Copy template into your frontend repo
#    cp -r frontend-infra-template/terraform YOUR_FRONTEND_REPO/terraform
#    cp frontend-infra-template/.github/workflows/deploy.yml YOUR_FRONTEND_REPO/.github/workflows/

# 2. Configure prod
cd terraform/environments/prod
cp backend.tf.example backend.tf      # same state bucket as backend, different key
cp terraform.tfvars.example terraform.tfvars

# 3. Set api_origin_domain from backend repo:
#    terraform -chdir=../centroid-finder/terraform/environments/prod output -raw alb_dns_name

terraform init
terraform apply
```

## GitHub settings (frontend repo)

Create environment **prod** and set:

| Type | Name | Value |
|------|------|-------|
| Secret | `AWS_ROLE_ARN` | `terraform output -raw github_deploy_role_arn` |
| Variable | `AWS_REGION` | `us-east-1` |
| Variable | `FRONTEND_BUCKET` | `terraform output -raw frontend_bucket_name` |
| Variable | `CF_DISTRIBUTION_ID` | `terraform output -raw cloudfront_distribution_id` |
| Variable | `CF_DOMAIN` | `terraform output -raw cloudfront_domain_name` |

## App config

Build with same-origin API routing:

```bash
VITE_API_BASE=/api npm run build
```

In code, prefix API calls with `import.meta.env.VITE_API_BASE` (or your env helper).

## Verify

```bash
CF_URL=$(terraform output -raw cloudfront_url)
curl -sf "$CF_URL/api/videos"
# Open $CF_URL in a browser
```

Full runbook: [frontend-deployment.md](../docs/frontend-deployment.md) in the backend repo.
