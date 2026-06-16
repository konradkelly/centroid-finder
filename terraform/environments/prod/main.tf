terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "centroid-finder"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}

locals {
  name_prefix = "centroid-${var.environment}"
}

module "network" {
  source = "../../modules/network"

  name_prefix = local.name_prefix
  vpc_cidr    = var.vpc_cidr
}

module "ecr" {
  source = "../../modules/ecr"

  name_prefix = local.name_prefix
}

module "rds" {
  source = "../../modules/rds"

  name_prefix        = local.name_prefix
  private_subnet_ids = module.network.private_subnet_ids
  security_group_id  = module.network.rds_security_group_id
}

module "efs" {
  source = "../../modules/efs"

  name_prefix        = local.name_prefix
  private_subnet_ids = module.network.private_subnet_ids
  security_group_id  = module.network.efs_security_group_id
}

module "alb" {
  source = "../../modules/alb"

  name_prefix         = local.name_prefix
  vpc_id              = module.network.vpc_id
  subnet_ids          = module.network.public_subnet_ids
  security_group_id   = module.network.alb_security_group_id
  certificate_arn     = var.alb_certificate_arn
}

module "ecs" {
  source = "../../modules/ecs"

  name_prefix                 = local.name_prefix
  private_subnet_ids          = module.network.private_subnet_ids
  security_group_id           = module.network.ecs_security_group_id
  container_image             = "${module.ecr.repository_url}:${var.image_tag}"
  rds_address                 = module.rds.address
  rds_db_name                 = module.rds.db_name
  rds_secret_arn              = module.rds.secret_arn
  efs_file_system_id          = module.efs.file_system_id
  efs_file_system_arn         = module.efs.file_system_arn
  efs_videos_access_point_id  = module.efs.videos_access_point_id
  efs_results_access_point_id = module.efs.results_access_point_id
  target_group_arn            = module.alb.target_group_arn
  create_service              = var.create_ecs_service
  desired_count               = var.ecs_desired_count
}

module "github_oidc" {
  source = "../../modules/github-oidc"

  github_org              = var.github_org
  github_repo             = var.github_repo
  ecr_repository_arn      = module.ecr.repository_arn
  ecs_service_name        = coalesce(module.ecs.service_name, "${local.name_prefix}-service")
  task_execution_role_arn = module.ecs.task_execution_role_arn
  task_role_arn           = module.ecs.task_role_arn
}
