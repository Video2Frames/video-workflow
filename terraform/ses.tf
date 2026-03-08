############################################
# SES - Email Identity
############################################

resource "aws_ses_email_identity" "noreply" {
  email = var.ses_from_email
}