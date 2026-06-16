output "file_system_id" {
  value = aws_efs_file_system.main.id
}

output "file_system_arn" {
  value = aws_efs_file_system.main.arn
}

output "videos_access_point_id" {
  value = aws_efs_access_point.videos.id
}

output "results_access_point_id" {
  value = aws_efs_access_point.results.id
}
