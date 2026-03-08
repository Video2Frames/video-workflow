############################################
# API Gateway HTTP API
############################################

resource "aws_apigatewayv2_api" "video_api" {
  name          = "${local.app_name}-api"
  protocol_type = "HTTP"
}

############################################
# Integration (dinâmica via Ingress)
############################################

resource "aws_apigatewayv2_integration" "video_integration" {
  api_id             = aws_apigatewayv2_api.video_api.id
  integration_type   = "HTTP_PROXY"
  integration_method = "ANY"

  integration_uri = "http://${kubernetes_ingress_v1.app.status[0].load_balancer[0].ingress[0].hostname}/{proxy}"

  payload_format_version = "1.0"

  depends_on = [
    kubernetes_ingress_v1.app
  ]
}

############################################
# Route
############################################

resource "aws_apigatewayv2_route" "video_route" {
  api_id    = aws_apigatewayv2_api.video_api.id
  route_key = "ANY /hackathon/v1/video-workflow/{proxy+}"

  target = "integrations/${aws_apigatewayv2_integration.video_integration.id}"

  authorization_type = "NONE"
}

############################################
# Stage
############################################

resource "aws_apigatewayv2_stage" "prod" {
  api_id      = aws_apigatewayv2_api.video_api.id
  name        = "$default"
  auto_deploy = true
}

############################################
# Output
############################################

output "api_gateway_url" {
  value = aws_apigatewayv2_api.video_api.api_endpoint
}