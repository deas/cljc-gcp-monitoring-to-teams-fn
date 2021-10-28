output "project_id" {
  value       = var.project_id
  description = "The project in which resources are applied."
}

output "region" {
  value       = var.region
  description = "The region in which resources are applied."
}

output "function_name" {
  value       = module.monitoring_to_teams.name
  description = "The name of the function created"
}
