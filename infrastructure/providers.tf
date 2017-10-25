# Providers

provider "aws" {
  region = "eu-west-2"
}

provider "aws" {
  region = "us-east-1"
  alias = "us-east-1"
}

# Data sources

data "aws_acm_certificate" "production" {
  provider = "aws.us-east-1"
  domain = "${var.site_domain}"
}

data "aws_acm_certificate" "staging" {
  provider = "aws.us-east-1"
  domain = "staging.${var.site_domain}"
}
