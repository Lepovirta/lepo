#### Terraform config

terraform {
  backend "s3" {
    encrypt = true
  }
}

#### Variables

variable "site_domain" {
  type = "string"
  description = "Domain name for the website"
}

#### Providers

provider "aws" {
  region = "eu-west-2"
}

provider "aws" {
  region = "us-east-1"
  alias = "us-east-1"
}

#### Data sources

data "aws_acm_certificate" "production" {
  provider = "aws.us-east-1"
  domain = "${var.site_domain}"
}

data "aws_acm_certificate" "staging" {
  provider = "aws.us-east-1"
  domain = "staging.${var.site_domain}"
}

#### Resources

# Buckets

resource "aws_s3_bucket" "production_site" {
  bucket_prefix = "production.${var.site_domain}"
  acl = "public-read"

  website {
    index_document = "index.html"
  }

  tags {
    Domain = "${var.site_domain}"
    Environment = "Production"
  }
}

resource "aws_s3_bucket" "production_logs" {
  bucket_prefix = "logs.production.${var.site_domain}"
  acl = "private"

  tags {
    Domain = "${var.site_domain}"
    Environment = "Production"
  }
}

resource "aws_s3_bucket" "staging_site" {
  bucket_prefix = "staging.${var.site_domain}"
  acl = "public-read"

  website {
    index_document = "index.html"
  }

  lifecycle_rule {
    enabled = true

    expiration {
      days = 90
    }
  }

  tags {
    Domain = "${var.site_domain}"
    Environment = "Staging"
  }
}

# Bucket policies

resource "aws_s3_bucket_policy" "public_production" {
  bucket = "${aws_s3_bucket.production_site.id}"
  policy = <<POLICY
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "AddPerm",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "${aws_s3_bucket.production_site.arn}/*"
        }
    ]
}
POLICY
}

resource "aws_s3_bucket_policy" "public_staging" {
  bucket = "${aws_s3_bucket.staging_site.id}"
  policy = <<POLICY
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "AddPerm",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "${aws_s3_bucket.staging_site.arn}/*"
        }
    ]
}
POLICY
}

# IAM policies and groups

resource "aws_iam_policy" "deploy" {
  description = "Deployment access for ${var.site_domain}"
  policy = <<POLICY
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:ListBucket",
                "s3:AbortMultipartUpload",
                "s3:DeleteObject",
                "s3:GetObject",
                "s3:GetObjectAcl",
                "s3:PutObject",
                "s3:PutObjectAcl"
            ],
            "Resource": [
                "${aws_s3_bucket.production_site.arn}",
                "${aws_s3_bucket.production_site.arn}/*",
                "${aws_s3_bucket.staging_site.arn}",
                "${aws_s3_bucket.staging_site.arn}/*"
            ]
        }
    ]
}
POLICY
}

resource "aws_iam_group" "deploy" {
  name = "${var.site_domain}-deploy"
}

resource "aws_iam_policy_attachment" "deploy" {
  name = "deploy-${var.site_domain}"
  groups = ["${aws_iam_group.deploy.name}"]
  policy_arn = "${aws_iam_policy.deploy.arn}"
}

resource "aws_iam_policy" "logs" {
  description = "Deployment access for ${var.site_domain}"
  policy = <<POLICY
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:ListBucket",
                "s3:GetObject"
            ],
            "Resource": [
                "${aws_s3_bucket.production_site.arn}",
                "${aws_s3_bucket.production_site.arn}/*"
            ]
        }
    ]
}
POLICY
}

resource "aws_iam_group" "logs" {
  name = "${var.site_domain}-log-reader"
}

resource "aws_iam_policy_attachment" "logs" {
  name = "logs-${var.site_domain}"
  groups = ["${aws_iam_group.logs.name}"]
  policy_arn = "${aws_iam_policy.logs.arn}"
}

# Cloudfront distributions

resource "aws_cloudfront_distribution" "production" {
  comment = "CDN for ${var.site_domain} production site"
  enabled = true
  aliases = [
    "${var.site_domain}",
    "www.${var.site_domain}"
  ]
  default_root_object = "index.html"
  is_ipv6_enabled = true
  price_class = "PriceClass_All"

  origin {
    domain_name = "${aws_s3_bucket.production_site.website_endpoint}"
    origin_id = "SiteProductionOrigin"

    custom_origin_config {
      http_port = 80
      https_port = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols = ["TLSv1", "TLSv1.1", "TLSv1.2"]
    }
  }

  default_cache_behavior {
    allowed_methods = ["GET", "HEAD", "OPTIONS"]
    cached_methods = ["GET", "HEAD"]
    target_origin_id = "SiteProductionOrigin"
    compress = true
    viewer_protocol_policy = "redirect-to-https"
    min_ttl = 0
    max_ttl = 86400
    default_ttl = 600

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
    }
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn = "${data.aws_acm_certificate.production.arn}"
    ssl_support_method = "sni-only"
    minimum_protocol_version = "TLSv1"
  }

  logging_config {
    bucket = "${aws_s3_bucket.production_logs.bucket_domain_name}"
    include_cookies = false
  }

  tags {
    Domain = "${var.site_domain}"
    Environment = "Production"
  }
}

resource "aws_cloudfront_distribution" "staging" {
  comment = "CDN for ${var.site_domain} staging site"
  enabled = true
  aliases = [
    "staging.${var.site_domain}"
  ]
  default_root_object = "index.html"
  is_ipv6_enabled = true
  price_class = "PriceClass_100"

  origin {
    domain_name = "${aws_s3_bucket.staging_site.website_endpoint}"
    origin_id = "SiteStagingOrigin"

    custom_origin_config {
      http_port = 80
      https_port = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols = ["TLSv1", "TLSv1.1", "TLSv1.2"]
    }
  }

  default_cache_behavior {
    allowed_methods = ["GET", "HEAD", "OPTIONS"]
    cached_methods = ["GET", "HEAD"]
    target_origin_id = "SiteStagingOrigin"
    compress = true
    viewer_protocol_policy = "redirect-to-https"
    min_ttl = 0
    max_ttl = 86400
    default_ttl = 600

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
    }
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn = "${data.aws_acm_certificate.staging.arn}"
    ssl_support_method = "sni-only"
    minimum_protocol_version = "TLSv1"
  }

  tags {
    Domain = "${var.site_domain}"
    Environment = "Staging"
  }
}

# DNS records

resource "aws_route53_zone" "site" {
  name = "${var.site_domain}."

  tags {
    Domain = "${var.site_domain}"
    Environment = "Production"
  }
}

resource "aws_route53_record" "production_a" {
  zone_id = "${aws_route53_zone.site.zone_id}"
  name = "${var.site_domain}"
  type = "A"

  alias {
    name = "${aws_cloudfront_distribution.production.domain_name}"
    zone_id = "Z2FDTNDATAQYW2"
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "production_aaaa" {
  zone_id = "${aws_route53_zone.site.zone_id}"
  name = "${var.site_domain}"
  type = "AAAA"

  alias {
    name = "${aws_cloudfront_distribution.production.domain_name}"
    zone_id = "Z2FDTNDATAQYW2"
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "production_www_a" {
  zone_id = "${aws_route53_zone.site.zone_id}"
  name = "www.${var.site_domain}"
  type = "A"

  alias {
    name = "${aws_cloudfront_distribution.production.domain_name}"
    zone_id = "Z2FDTNDATAQYW2"
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "production_www_aaaa" {
  zone_id = "${aws_route53_zone.site.zone_id}"
  name = "www.${var.site_domain}"
  type = "AAAA"

  alias {
    name = "${aws_cloudfront_distribution.production.domain_name}"
    zone_id = "Z2FDTNDATAQYW2"
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "staging_a" {
  zone_id = "${aws_route53_zone.site.zone_id}"
  name = "staging.${var.site_domain}"
  type = "A"

  alias {
    name = "${aws_cloudfront_distribution.staging.domain_name}"
    zone_id = "Z2FDTNDATAQYW2"
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "staging_aaaa" {
  zone_id = "${aws_route53_zone.site.zone_id}"
  name = "staging.${var.site_domain}"
  type = "AAAA"

  alias {
    name = "${aws_cloudfront_distribution.staging.domain_name}"
    zone_id = "Z2FDTNDATAQYW2"
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "email" {
  zone_id = "${aws_route53_zone.site.zone_id}"
  name = "${var.site_domain}"
  type = "MX"
  ttl = 3600
  records = [
    "1 ASPMX.L.GOOGLE.COM.",
    "5 ALT1.ASPMX.L.GOOGLE.COM.",
    "5 ALT2.ASPMX.L.GOOGLE.COM.",
    "10 ALT3.ASPMX.L.GOOGLE.COM.",
    "10 ALT4.ASPMX.L.GOOGLE.COM"
  ]
}

resource "aws_route53_record" "text" {
  zone_id = "${aws_route53_zone.site.zone_id}"
  name = "${var.site_domain}"
  type = "TXT"
  ttl = 3600
  records = [
    "keybase-site-verification=2Z-izJ5MISN3Y_DhveqB8MG-c1_QAYWXR3dMi4EcTDY",
    "google-site-verification=sGM3w0qg_NhJXdadfxmlwTFcP90qJb0GX8p4kIyEYI4"
  ]
}
