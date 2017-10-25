# Production buckets

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

# Staging buckets

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
