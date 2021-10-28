resource "random_pet" "main" {
  length    = 2
  separator = "-"
}

resource "google_storage_bucket" "trigger_bucket" {
  name          = "gcp-monitoring-to-teams-${random_pet.main.id}-trigger"
  force_destroy = true
  location      = var.region
  project       = var.project_id
  storage_class = "REGIONAL"
}


# TODO: No java on gcp js build -> so we cannot use shadown-cljs there
resource "local_file" "package" {
  filename = "${path.module}/dist/function/package.json"
  content  = <<EOF
{
  "name": "cljc-gcp-monitoring-to-teams-fn",
  "version": "0.0.1",
  "private": true,
  "dependencies": {
    "strftime": "^0.10.0"
  }
}
EOF
}
/*
resource "local_file" "shadow" {
  content  = replace(file("${path.module}/shadow-cljs.edn"), "/(src\\/)|(dist/function\\/)/", "")
  filename = "${path.module}/src/shadow-cljs.edn"
}
*/

module "monitoring_to_teams" {
  source      = "terraform-google-modules/event-function/google"
  version     = "2.0.0"
  description = "Sends notifications to MS Teams"
  entry_point = "handleRequest"
  # service_account_email = 
  trigger_http = true # endpoint returned as https_trigger_url
  /*
  event_trigger = {
    event_type = "google.storage.object.finalize"
    resource   = google_storage_bucket.trigger_bucket.name
  }
  */

  environment_variables = {
    AUTH_TOKEN = var.auth_token
  }
  name                   = "gcp-monitoring-to-teams-${random_pet.main.id}"
  project_id             = var.project_id
  region                 = var.region
  source_directory       = "${path.module}/dist/function"
  runtime                = "nodejs14"
  source_dependent_files = [local_file.package] #, local_file.shadow]
}

# IAM entry for all users to invoke the function
resource "google_cloudfunctions_function_iam_member" "invoker" {
  project        = var.project_id
  region         = var.region
  cloud_function = module.monitoring_to_teams.name

  role   = "roles/cloudfunctions.invoker"
  member = "allUsers"
}

resource "null_resource" "wait_for_function" {
  provisioner "local-exec" {
    command = "sleep 60"
  }

  depends_on = [module.monitoring_to_teams]
}
