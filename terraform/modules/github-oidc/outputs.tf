output "role_arn" {
  value = aws_iam_role.github_deploy.arn
}

output "role_name" {
  value = aws_iam_role.github_deploy.name
}
