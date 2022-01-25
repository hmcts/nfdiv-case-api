data "azurerm_application_insights" "ai" {
  name                = "${var.product}-${var.env}"
  resource_group_name = "${var.product}-${var.env}"
}


locals {
  app_insights_config = jsonencode(
    merge(
      jsondecode(
      file("${path.module}/../lib/applicationinsights.json")), { connectionString = data.azurerm_application_insights.ai.connection_string }
    )
  )
}

resource "azurerm_key_vault_secret" "app_insights_config" {
  name         = "${var.component}-app-insights-config"
  value        = local.app_insights_config
  key_vault_id = data.azurerm_key_vault.key_vault.id
}
