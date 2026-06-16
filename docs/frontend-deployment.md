# Frontend Deployment

Deploy the React app from a **separate repository** using S3 + CloudFront. API traffic is proxied through CloudFront to the backend ALB so the browser sees a single origin and Spring does not need CORS.

```
Browser
   │
   ▼
CloudFront (frontend repo)
   ├── /*      → S3 (React build)
   └── /api/*  → ALB → ECS (backend repo)
```

A copy-paste Terraform + GitHub Actions package lives in [`frontend-infra-template/`](../frontend-infra-template/).

---

## 1. What you need from the backend

After the backend stack is live, collect one value from the **centroid-finder** repo:

```bash
terraform -chdir=terraform/environments/prod output -raw alb_dns_name
```

Example: `centroid-prod-alb-1234567890.us-east-1.elb.amazonaws.com`

Also confirm the API responds:

```bash
ALB=$(terraform -chdir=terraform/environments/prod output -raw alb_dns_name)
curl -sf "http://$ALB/api/videos"
```

| Backend output | Used for |
|----------------|----------|
| `alb_dns_name` | CloudFront API origin |
| (same bootstrap state bucket) | Frontend Terraform remote state |

Everything else (RDS, EFS, ECS, ECR) stays in the backend repo.

---

## 2. Copy infrastructure into the frontend repo

From the centroid-finder repo root:

```bash
FRONTEND_REPO=/path/to/your-frontend-repo

cp -r frontend-infra-template/terraform "$FRONTEND_REPO/"
mkdir -p "$FRONTEND_REPO/.github/workflows"
cp frontend-infra-template/.github/workflows/deploy.yml "$FRONTEND_REPO/.github/workflows/"
```

Your frontend repo should look like:

```
your-frontend-repo/
  src/
  package.json
  terraform/
    modules/
      frontend/
      github-oidc-frontend/
    environments/
      prod/
  .github/workflows/
    deploy.yml
```

---

## 3. Configure and apply Terraform

```bash
cd terraform/environments/prod
cp backend.tf.example backend.tf
cp terraform.tfvars.example terraform.tfvars
```

Edit `backend.tf` — use the **same** state bucket and lock table as the backend, with a **different key**:

```hcl
key = "frontend/prod/terraform.tfstate"
```

Edit `terraform.tfvars`:

```hcl
github_org        = "your-github-org"
github_repo       = "your-frontend-repo-name"
api_origin_domain = "<alb_dns_name from backend>"
api_origin_protocol = "http-only"   # see §6 when ALB gets HTTPS
api_origin_port     = 80
```

```bash
terraform init
terraform plan
terraform apply
```

Save outputs:

```bash
terraform output -raw cloudfront_url
terraform output -raw github_deploy_role_arn
terraform output -raw frontend_bucket_name
terraform output -raw cloudfront_distribution_id
terraform output -raw cloudfront_domain_name
```

---

## 4. Wire GitHub Actions (frontend repo)

Create a **prod** environment in the frontend repository settings.

| Type | Name | Value |
|------|------|-------|
| Secret | `AWS_ROLE_ARN` | `github_deploy_role_arn` output |
| Variable | `AWS_REGION` | `us-east-1` |
| Variable | `FRONTEND_BUCKET` | `frontend_bucket_name` output |
| Variable | `CF_DISTRIBUTION_ID` | `cloudfront_distribution_id` output |
| Variable | `CF_DOMAIN` | `cloudfront_domain_name` output (no `https://`) |

Push to `main` to trigger the deploy workflow, or run it manually via **workflow_dispatch**.

---

## 5. Frontend app configuration

Build for AWS with root base path (Salamander-Labs uses `/Salamander-Labs/` on GitHub Pages):

```bash
VITE_BASE_PATH=/ npm run build
```

The GitHub Actions workflow sets this automatically. API calls in `src/api.js` already use root-relative paths (`/api/videos`, `/thumbnail/...`, `/process/...`); CloudFront proxies those to the backend ALB.

---

## 6. When the backend ALB gets HTTPS

After you add an ACM certificate to the backend ALB (`alb_certificate_arn` in backend `terraform.tfvars`), update frontend `terraform.tfvars`:

```hcl
api_origin_protocol = "https-only"
api_origin_port     = 443
```

Then `terraform apply` in the frontend repo. CloudFront will talk to the ALB over HTTPS.

---

## 7. Optional custom domain

1. Request an ACM certificate in **us-east-1** for your app domain (e.g. `app.example.com`).
2. In frontend `terraform.tfvars`:

   ```hcl
   cloudfront_aliases         = ["app.example.com"]
   cloudfront_certificate_arn = "arn:aws:acm:us-east-1:ACCOUNT:certificate/..."
   ```

3. Create a Route 53 (or DNS provider) CNAME/alias record pointing to the CloudFront domain name.
4. Rebuild and redeploy the frontend.

---

## 8. Verify end-to-end

```bash
CF_URL=$(terraform -chdir=terraform/environments/prod output -raw cloudfront_url)

# API through CloudFront
curl -sf "$CF_URL/api/videos"

# Health (optional)
curl -sf "$CF_URL/api/videos" | head -c 200
```

Open `CF_URL` in a browser. The UI should load from S3 and API calls should hit `/api/...` on the same host.

---

## 9. Troubleshooting

| Symptom | Likely cause |
|---------|----------------|
| 502/504 on `/api/*` | Wrong `api_origin_domain`, ALB down, or security group blocking port 80/443 |
| SPA routes 404 on refresh | CloudFront custom error responses missing (module sets 403/404 → `index.html`) |
| API works on ALB but not CloudFront | `api_origin_protocol` / `api_origin_port` mismatch with ALB listener |
| GitHub Action `AccessDenied` on S3 | Wrong `AWS_ROLE_ARN` or role trust policy repo name mismatch |
| OIDC provider error on apply | Backend terraform must have run first (creates account-wide GitHub OIDC provider) |
| CORS errors in browser | Production build is calling ALB directly instead of `/api` through CloudFront |

---

## 10. Terraform module reference

Modules live in `terraform/modules/` (also copied into `frontend-infra-template/`):

| Module | Purpose |
|--------|---------|
| `frontend` | S3 bucket, OAC, CloudFront distribution, bucket policy |
| `github-oidc-frontend` | Deploy role for frontend repo (reuses existing GitHub OIDC provider) |

Key variables for `frontend` module:

| Variable | Default | Description |
|----------|---------|-------------|
| `api_origin_domain` | (required) | Backend ALB DNS name |
| `api_origin_protocol` | `http-only` | `http-only` or `https-only` |
| `api_origin_port` | `80` | ALB listener port |

---

## Related docs

- [aws-deployment-spec.md](aws-deployment-spec.md) §10 — architecture spec
- [backend-deployment.md](backend-deployment.md) — backend stack runbook
- [frontend-infra-template/README.md](../frontend-infra-template/README.md) — quick copy instructions
