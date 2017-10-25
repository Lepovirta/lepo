# Deployment access

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

# Log reader access

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
