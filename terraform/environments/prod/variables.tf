variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "environment" {
  type    = string
  default = "prod"
}

variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
}

variable "github_org" {
  type        = string
  description = "GitHub organization or user for OIDC trust"
}

variable "github_repo" {
  type        = string
  description = "GitHub repository name for OIDC trust"
}

variable "alb_certificate_arn" {
  type        = string
  default     = ""
  description = "ACM certificate ARN for HTTPS listener (us-east-1). Leave empty for HTTP-only ALB."
}

variable "image_tag" {
  type        = string
  default     = "bootstrap"
  description = "ECR image tag for the ECS task definition"
}

variable "create_ecs_service" {
  type        = bool
  default     = true
  description = "Set false until the first container image is pushed to ECR"
}

variable "ecs_desired_count" {
  type    = number
  default = 1
}
