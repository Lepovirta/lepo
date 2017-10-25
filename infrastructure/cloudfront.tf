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
