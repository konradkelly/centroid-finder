output "alb_dns_name" {
  value = module.alb.dns_name
}

output "ecr_repository_url" {
  value = module.ecr.repository_url
}

output "ecs_cluster_name" {
  value = module.ecs.cluster_name
}

output "ecs_service_name" {
  value = module.ecs.service_name
}

output "ecs_task_family" {
  value = module.ecs.task_family
}

output "ecs_container_name" {
  value = module.ecs.container_name
}

output "rds_endpoint" {
  value     = module.rds.endpoint
  sensitive = true
}

output "github_deploy_role_arn" {
  value = module.github_oidc.role_arn
}

output "efs_file_system_id" {
  value = module.efs.file_system_id
}

output "efs_videos_access_point_id" {
  value = module.efs.videos_access_point_id
}

output "private_subnet_ids" {
  value = module.network.private_subnet_ids
}

output "private_subnet_id" {
  value       = module.network.private_subnet_ids[0]
  description = "First private subnet — use for one-off EFS seeder EC2"
}

output "ecs_security_group_id" {
  value = module.network.ecs_security_group_id
}
