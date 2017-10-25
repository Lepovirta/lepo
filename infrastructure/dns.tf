# Zone

resource "aws_route53_zone" "site" {
  name = "${var.site_domain}."

  tags {
    Domain = "${var.site_domain}"
    Environment = "Production"
  }
}

# Production records

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

# Staging records

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

# Additional records

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
