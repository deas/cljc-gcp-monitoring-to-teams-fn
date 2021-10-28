variable "project_id" {
  type        = string
  description = "The ID of the project to which resources will be applied."
}

variable "region" {
  type        = string
  description = "The region in which resources will be applied."
}

variable "auth_token" {
  type        = string
  description = "The auth_token query parameter expected."
}
