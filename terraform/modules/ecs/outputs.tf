output "cluster_name" {
  value = aws_ecs_cluster.main.name
}

output "cluster_arn" {
  value = aws_ecs_cluster.main.arn
}

output "service_name" {
  value = var.create_service ? aws_ecs_service.app[0].name : null
}

output "task_family" {
  value = aws_ecs_task_definition.app.family
}

output "task_definition_arn" {
  value = aws_ecs_task_definition.app.arn
}

output "container_name" {
  value = local.container_name
}

output "log_group_name" {
  value = local.log_group_name
}

output "task_execution_role_arn" {
  value = aws_iam_role.ecs_task_execution.arn
}

output "task_role_arn" {
  value = aws_iam_role.ecs_task.arn
}
