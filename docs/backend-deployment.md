# Backend Deployment — Changes and Next Steps

This document records what was implemented to start deploying the centroid-finder **backend** to AWS, and what remains before the stack is live. The target architecture is defined in [`aws-deployment-spec.md`](aws-deployment-spec.md) (ECS Fargate behind an ALB, RDS, EFS, ECR, GitHub Actions).

**Status:** App hardening and Terraform scaffolding are in place. Infrastructure has **not** been applied to AWS yet.

---

## Summary of Changes

### Application hardening (§3 of spec)

| Change | Files | Notes |
|--------|-------|-------|
| Spring Boot Actuator | `pom.xml`, `src/main/resources/application.yml` | Exposes `/actuator/health` for ALB health checks |
| Production profile | `src/main/resources/application-prod.yml` | DB URL/username/password from env only — no baked-in credentials |
| Server main class | `pom.xml` | `spring-boot-maven-plugin` uses `ServerApplication` (processor JAR still uses `VideoProcessorApp` via assembly plugin) |
| CLI invocation fix | `DefaultJobProcessLauncher.java` | Uses `java -jar processor.jar` with four args matching `CliArgumentParser` (removed broken `PropertiesLauncher` + `analyze` subcommand) |
| Docker image | `Dockerfile`, `.dockerignore` | Temurin 17 JRE; copies server + processor JARs; mount points at `/app/videos` and `/app/results` |

### CI/CD

| Change | Files | Notes |
|--------|-------|-------|
| Deploy workflow | `.github/workflows/deploy.yml` | On push to `main`: Maven package → Docker build/push (`$GITHUB_SHA` tag) → ECS task definition render → `update-service` |

Requires GitHub **environment** `prod` and secrets/variables after first `terraform apply` (see below).

### Terraform

New layout under `terraform/`:

```
terraform/
  bootstrap/                 # One-time S3 state bucket + DynamoDB lock table
  modules/
    network/                 # VPC, subnets, NAT, security groups
    ecr/                     # ECR repository + lifecycle policy
    rds/                     # PostgreSQL 16 + Secrets Manager secret
    efs/                     # Shared storage with /videos and /results access points
    alb/                     # ALB, target group, HTTP or HTTPS listener
    ecs/                     # Fargate cluster, task definition, service, auto scaling, IAM
    github-oidc/             # GitHub Actions OIDC deploy role
  environments/
    prod/                    # Wired prod stack
  README.md                  # Operational runbook (bootstrap, push image, enable service)
```

**Prod defaults** (`terraform/environments/prod/terraform.tfvars.example`):

- `create_ecs_service = false` — apply infra without ECS service until the first image is in ECR
- `image_tag = "bootstrap"` — tag for the initial manual image push
- `alb_certificate_arn` empty — HTTP-only ALB until an ACM cert is provided

### Other

| Change | Files | Notes |
|--------|-------|-------|
| Gitignore | `.gitignore` | Ignores `.terraform/`, state files, and local `terraform.tfvars` |

---

## What Is Not Done Yet

These items from the spec are **out of scope** for the current implementation pass:

| Item | Spec reference | Notes |
|------|----------------|-------|
| Frontend (S3 + CloudFront) | §10 | Backend-only focus; API tested via ALB DNS initially |
| Container smoke test in CI | §13.2 | Run container + Postgres + `POST /process/...` before push |
| JavaCV migration | §3.4, goals.md | Still on JCodec; blocking for full container integration test |
| `workflow_run` gate on CI | §13.2 | Deploy runs on `main` push; not yet chained to `run-tests.yml` |
| EFS video seeding | §8 | Sample videos must be copied to EFS after mount |
| Custom domain / HTTPS ALB | §6 | Optional `alb_certificate_arn`; HTTP works for initial validation |
| VPC endpoints (NAT alternative) | §4 | NAT Gateway used for simplicity (~$32/mo) |

---

## Configuration Matrix

Three different stores are involved. Do not mix them up.

| Store | Purpose |
|-------|---------|
| Local `.env` | Run Spring Boot on your machine against **local Postgres** |
| GitHub Secrets / Variables | CI deploy workflow (`deploy.yml`) via **OIDC** (no long-lived AWS keys) |
| AWS Secrets Manager | Prod RDS credentials injected into **ECS tasks** by Terraform |

### Local `.env` (application dev)

Your `.env` is for **local development**, not GitHub Actions. See [`.env.example`](../.env.example). Nothing here is required for the deploy workflow.

**Required for local app runs:**

| Variable | Example | Notes |
|----------|---------|-------|
| `VIDEOS_DIR` | `./sampleInput` or `./videos` | Video catalog source |
| `RESULTS_DIR` | `./results` | CSV output directory |
| `VIDEO_PROCESSOR_JAR` | `./target/centroid-finder-1.0-SNAPSHOT-jar-with-dependencies.jar` | Processor subprocess JAR |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/centroid_finder` | **Local** Postgres, not RDS |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Local DB user |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Local DB password |

**Optional:**

| Variable | Example | Notes |
|----------|---------|-------|
| `JOB_TIMEOUT` | `PT10M` | Max processor runtime per job |

**Optional for AWS CLI on your machine** (Terraform, `aws ecr`, `docker push`):

Prefer `aws configure` or SSO over putting keys in `.env`. If you use a named profile:

| Variable | Example | Notes |
|----------|---------|-------|
| `AWS_REGION` or `AWS_DEFAULT_REGION` | `us-east-1` | Region for CLI commands |
| `AWS_PROFILE` | `your-profile` | Named profile in `~/.aws/credentials` |

**Optional convenience** (after `terraform apply` — not read by the app):

| Variable | Source | Notes |
|----------|--------|-------|
| `ECR_REPOSITORY_URL` | `terraform output ecr_repository_url` | Manual `docker build` / `docker push` |
| `ALB_DNS_NAME` | `terraform output alb_dns_name` | Quick `curl` smoke tests |

Do **not** put prod RDS username/password in `.env`. Prod DB credentials never belong in local env files for normal operation.

### GitHub Secrets (encrypted)

The deploy workflow uses **OIDC** — do **not** add `AWS_ACCESS_KEY_ID` or `AWS_SECRET_ACCESS_KEY` to GitHub.

Configure under **Settings → Secrets and variables → Actions**. Because `deploy.yml` uses `environment: prod`, you can scope these to the **prod** environment.

**Required today (backend `deploy.yml`):**

| Secret | How to get the value | Used by |
|--------|----------------------|---------|
| `AWS_ROLE_ARN` | `terraform -chdir=terraform/environments/prod output -raw github_deploy_role_arn` | `aws-actions/configure-aws-credentials` OIDC assume-role |

**Planned (full spec — not in `deploy.yml` yet):**

| Secret | When needed |
|--------|-------------|
| `TF_STATE_BUCKET` | CI runs `terraform apply` |
| `TF_LOCK_TABLE` | CI runs `terraform apply` |
| `CF_DISTRIBUTION_ID` | Frontend deploy + CloudFront invalidation job |

### GitHub Variables (non-secret)

**Required today (backend `deploy.yml`):**

| Variable | Example | How to get the value |
|----------|---------|----------------------|
| `AWS_REGION` | `us-east-1` | Spec default |
| `ECR_REPOSITORY` | `centroid-finder` | Repo **name** only (not full URL); `terraform output` or ECR console |
| `ECS_CLUSTER` | `centroid-prod` | `terraform output ecs_cluster_name` |
| `ECS_SERVICE` | `centroid-prod-service` | `terraform output ecs_service_name` |
| `ECS_TASK_FAMILY` | `centroid-prod-task` | `terraform output ecs_task_family` |

### AWS Secrets Manager (AWS — not GitHub)

Created and managed by Terraform. You do **not** copy these into GitHub or `.env`.

| Secret name | Created by | Consumed by |
|-------------|------------|-------------|
| `centroid/prod/rds` | `terraform/modules/rds` (`username` + `password` JSON keys) | ECS task definition → `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` |

ECS tasks also receive non-secret config via plain environment variables in the task definition: `SPRING_DATASOURCE_URL`, `SPRING_PROFILES_ACTIVE=prod`, `VIDEOS_DIR`, `RESULTS_DIR`, `VIDEO_PROCESSOR_JAR`.

### Quick checklist

| Context | What to set |
|---------|-------------|
| Local `.env` | App paths + **local** Postgres — no deploy secrets |
| GitHub (backend now) | 1 secret + 5 variables (see tables above) |
| AWS Secrets Manager | `centroid/prod/rds` — automatic via Terraform |

---

## Next Steps

Follow this order. Detailed commands are in [`terraform/README.md`](../terraform/README.md).

### 1. Bootstrap Terraform remote state (one-time)

```powershell
cd terraform/bootstrap
cp terraform.tfvars.example terraform.tfvars
# Edit state_bucket_name to a globally unique value

terraform init
terraform apply
```

Record outputs: `state_bucket_name`, `lock_table_name`.

### 2. Configure and apply prod infrastructure

```powershell
cd terraform/environments/prod
cp terraform.tfvars.example terraform.tfvars
cp backend.tf.example backend.tf
```

Edit:

- `backend.tf` — bucket name from bootstrap output
- `terraform.tfvars` — `github_org`, `github_repo` (for OIDC trust)

First apply **without** the ECS service:

```powershell
terraform init
terraform apply
```

This creates VPC, RDS, EFS, ECR, ALB, ECS cluster + task definition, and the GitHub OIDC role. ECS **service** is skipped when `create_ecs_service = false`.

### 3. Build and push the first container image

```powershell
# From repo root
mvn -B package

aws ecr get-login-password --region us-east-1 |
  docker login --username AWS --password-stdin <account>.dkr.ecr.us-east-1.amazonaws.com

$ECR_URI = terraform -chdir=terraform/environments/prod output -raw ecr_repository_url
docker build -t "${ECR_URI}:bootstrap" .
docker push "${ECR_URI}:bootstrap"
```

### 4. Enable the ECS service

In `terraform/environments/prod/terraform.tfvars`:

```hcl
create_ecs_service = true
image_tag          = "bootstrap"
```

```powershell
terraform apply
```

Wait until the ECS service is stable and the ALB target group shows **healthy** (tasks need ~120s grace period for JVM startup).

### 5. Seed EFS with videos

The app reads the video catalog from `VIDEOS_DIR` on EFS (`/app/videos` in the container). Copy [`videos/ensantina.mp4`](../videos/ensantina.mp4) into the `/videos` access point root so it appears in the catalog as `ensantina.mp4`.

EFS is only reachable from inside the VPC (private subnets; NFS allowed from the ECS security group). Run the mount/copy commands on a **one-off Amazon Linux EC2** connected via **SSM Session Manager** — not from local Git Bash.

#### 5a. One-time: IAM role for the seeder instance

Check whether you already have a profile (needs `AmazonSSMManagedInstanceCore` on its role):

```bash
aws iam list-instance-profiles \
  --query "InstanceProfiles[].{Name:InstanceProfileName,Role:Roles[0].RoleName}" \
  --output table
```

If a profile exists with SSM access — e.g. **`SystemsManager`** — skip the create block below and set `SEEDER_PROFILE` to that name in step 5c.

Otherwise create **`centroid-efs-seeder`** (one-time):

```bash
# Git Bash — repo root
cat > /tmp/seeder-trust.json <<'EOF'
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": { "Service": "ec2.amazonaws.com" },
    "Action": "sts:AssumeRole"
  }]
}
EOF

aws iam create-role --role-name centroid-efs-seeder \
  --assume-role-policy-document file:///tmp/seeder-trust.json

aws iam attach-role-policy --role-name centroid-efs-seeder \
  --policy-arn arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore

aws iam create-instance-profile --instance-profile-name centroid-efs-seeder
aws iam add-role-to-instance-profile \
  --instance-profile-name centroid-efs-seeder \
  --role-name centroid-efs-seeder

export SEEDER_PROFILE=centroid-efs-seeder
```

If reusing an existing profile: `export SEEDER_PROFILE=SystemsManager` (or your profile name).

Wait ~30s for the instance profile to propagate before launching EC2.

#### 5b. Local: stage the video and collect Terraform outputs

```bash
# Git Bash — repo root
export AWS_REGION=us-east-1
export SEEDER_PROFILE="${SEEDER_PROFILE:-SystemsManager}"
export SEED_BUCKET="centroid-seed-$(aws sts get-caller-identity --query Account --output text)"

aws s3 mb "s3://${SEED_BUCKET}" 2>/dev/null || true
aws s3 cp videos/ensantina.mp4 "s3://${SEED_BUCKET}/ensantina.mp4"

export EFS_ID=$(terraform -chdir=terraform/environments/prod output -raw efs_file_system_id)
export AP_ID=$(terraform -chdir=terraform/environments/prod output -raw efs_videos_access_point_id)
export SUBNET_ID=$(terraform -chdir=terraform/environments/prod output -raw private_subnet_id)
export ECS_SG=$(terraform -chdir=terraform/environments/prod output -raw ecs_security_group_id)
export AMI_ID=$(aws ssm get-parameter \
  --name /aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64 \
  --query Parameter.Value --output text)

PRESIGNED_URL=$(aws s3 presign "s3://${SEED_BUCKET}/ensantina.mp4" --expires-in 3600)
echo "Copy these for step 5d:"
echo "  EFS_ID=$EFS_ID"
echo "  AP_ID=$AP_ID"
echo "  curl -fL -o /tmp/ensantina.mp4 '$PRESIGNED_URL'"
echo "  sudo mount -t efs -o tls,accesspoint=$AP_ID ${EFS_ID}:/ /mnt/efs-videos"
```

Requires [Session Manager plugin](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html) for `aws ssm start-session`.

#### 5c. Local: launch a one-off seeder EC2

```bash
INSTANCE_ID=$(aws ec2 run-instances \
  --image-id "$AMI_ID" \
  --instance-type t3.micro \
  --subnet-id "$SUBNET_ID" \
  --security-group-ids "$ECS_SG" \
  --iam-instance-profile Name="${SEEDER_PROFILE:-SystemsManager}" \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=centroid-efs-seeder}]' \
  --query 'Instances[0].InstanceId' --output text)

aws ec2 wait instance-running --instance-ids "$INSTANCE_ID"
echo "Seeder instance: $INSTANCE_ID"
```

The instance uses the **ECS security group** so EFS accepts NFS from it. It has no public IP; connect through SSM only.

#### 5d. On the instance: mount EFS and copy the video

```bash
aws ssm start-session --target "$INSTANCE_ID"
```

Inside the SSM shell, run the four commands printed at the end of step 5b, then:

```bash
sudo dnf install -y amazon-efs-utils
sudo mkdir -p /mnt/efs-videos
# paste: curl -fL -o /tmp/ensantina.mp4 '...'
# paste: sudo mount -t efs -o tls,accesspoint=... fs-...:/ /mnt/efs-videos
sudo cp /tmp/ensantina.mp4 /mnt/efs-videos/
sudo umount /mnt/efs-videos
exit
```

#### 5e. Local: clean up and verify

```bash
aws ec2 terminate-instances --instance-ids "$INSTANCE_ID"
aws s3 rm "s3://${SEED_BUCKET}/ensantina.mp4"
# Optional: aws s3 rb "s3://${SEED_BUCKET}"

ALB=$(terraform -chdir=terraform/environments/prod output -raw alb_dns_name)
curl -sf "http://${ALB}/api/videos"
```

You should see `ensantina.mp4` in the JSON catalog. Continue to [step 6](#6-verify-the-deployment) for a full smoke test.

### 6. Verify the deployment

```powershell
$ALB = terraform -chdir=terraform/environments/prod output -raw alb_dns_name

curl -sf "http://$ALB/actuator/health"
curl -sf "http://$ALB/api/videos"
```

Optional end-to-end job test:

```text
POST http://<alb>/process/ensantina.mp4?targetColor=FF0000&threshold=100
→ 202 + jobId → poll status → fetch CSV from /results/**
```

### 7. Configure GitHub for automated deploys

Create a GitHub **environment** named `prod` (optional protection rules).

Set the secrets and variables listed in [Configuration Matrix](#configuration-matrix) (GitHub Secrets and GitHub Variables sections). After merging to `main`, `.github/workflows/deploy.yml` builds and deploys a new image tagged with the commit SHA.

### 8. Later phases (spec §16)

1. **Frontend** — See [frontend-deployment.md](frontend-deployment.md) and copy [`frontend-infra-template/`](../frontend-infra-template/) into the React repo
2. **CI hardening** — container smoke test; gate deploy on `run-tests.yml` success
3. **JavaCV** — faster frame extraction + reliable container integration test
4. **HTTPS** — ACM cert in `us-east-1`; set `alb_certificate_arn` in tfvars
5. **Teardown runbook** — ordered `terraform destroy` and RDS snapshot decision

---

## Acceptance Criteria Checklist (from spec §15)

- [ ] `terraform apply` succeeds after bootstrap
- [ ] ECS service stable; ALB target group healthy
- [ ] `GET http://<alb>/api/videos` returns JSON from EFS-backed catalog
- [ ] `POST /process/{file}?targetColor=FF0000&threshold=100` → `202` + `jobId`
- [ ] Poll to `done`; CSV fetchable at `/results/**`
- [ ] Push to `main` deploys new image; running task uses new `GITHUB_SHA` tag
- [ ] Scale-out test: load triggers second task (or manual `desiredCount=2`)
- [ ] RDS not reachable from the public internet

---

## Related Documents

| Document | Purpose |
|----------|---------|
| [`aws-deployment-spec.md`](aws-deployment-spec.md) | Full architecture and configuration spec |
| [`terraform/README.md`](../terraform/README.md) | Terraform bootstrap and apply commands |
| [`goals.md`](goals.md) | Project tier definitions |
| [`.github/workflows/run-tests.yml`](../.github/workflows/run-tests.yml) | CI unit + integration tests |
| [`.github/workflows/deploy.yml`](../.github/workflows/deploy.yml) | Backend deploy workflow |
