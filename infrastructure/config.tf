terraform {
  backend "s3" {
    encrypt = true
  }
}

variable "site_domain" {
  type = "string"
  description = "Domain name for the website"
}
