#!/bin/sh -e

# Bootstrap your staticsite environment for Terraform

DEFAULT_ROOT_DOMAIN_NAME="lepo.io"

if ! hash aws 2>/dev/null; then
    echo "AWS CLI is not installed" 1>&2
    exit 1
fi

main() {
    local root_domain_name=$1

    if [ ! "$root_domain_name" ]; then
        root_domain_name=$DEFAULT_ROOT_DOMAIN_NAME
    fi

    local staging_domain_name="staging.$root_domain_name"
    local idempotency_token=""
    local state_bucket_name="tf-state.$root_domain_name"
    local region="eu-west-1"
    local available_certs=''


    if [ "$IDEMPOTENCY_TOKEN" ]; then
        idempotency_token=$IDEMPOTENCY_TOKEN
    else
        idempotency_token=$(date +"%Y%m%d%H%M%S")
    fi

    echo "Root domain name  : $root_domain_name" 1>&2
    echo "Idempotency token : $idempotency_token" 1>&2

    # Available certificates
    available_certs=$(
        aws acm list-certificates \
            --region "us-east-1" \
            --query 'CertificateSummaryList[*].DomainName' \
            --output text)

    # Available S3 buckets
    available_buckets=$(
        aws s3api list-buckets \
            --query 'Buckets[*].Name' \
            --output text)

    # Request a certificate for the root domain
    if contains_text "$root_domain_name" "$available_certs"; then
        echo "Certificate for $root_domain_name exists. Skipping creation." 1>&2
    else
        echo "Requesting a certificate for domain: $root_domain_name" 1>&2
        aws acm request-certificate \
            --domain-name "$root_domain_name" \
            --subject-alternative-names "www.$root_domain_name" \
            --idempotency-token "$idempotency_token" \
            --region "us-east-1"
    fi

    # Request a certificate for the staging domain
    if contains_text "$staging_domain_name" "$available_certs"; then
        echo "Certificate for $staging_domain_name exists. Skipping creation." 1>&2
    else
        echo "Requesting a certificate for domain: $staging_domain_name" 1>&2
        aws acm request-certificate \
            --domain-name "$staging_domain_name" \
            --idempotency-token "$idempotency_token" \
            --region "us-east-1"
    fi

    # Create a bucket for Terraform state
    if contains_text "$state_bucket_name" "$available_buckets"; then
        echo "Bucket $state_bucket_name exists. Skipping creation." 1>&2
    else
        echo "Creating a bucket: $state_bucket_name" 1>&2
        aws s3api create-bucket \
            --bucket "$state_bucket_name" \
            --acl private \
            --create-bucket-configuration "LocationConstraint=$region" \
            --region "$region"
        aws s3api put-bucket-tagging \
            --bucket "$state_bucket_name" \
            --tagging "TagSet=[{Key=Domain,Value=$root_domain_name}]"
        aws s3api put-bucket-versioning \
            --bucket "$state_bucket_name" \
            --versioning-configuration "Status=Enabled"
    fi

    # Initialise Terraform
    echo "Initialising Terraform" 1>&2
    terraform init \
              -backend-config "bucket=$state_bucket_name" \
              -backend-config "key=staticsite.tfstate" \
              -backend-config "region=$region"

    echo "Done" 1>&2
}

contains_text() {
    local text_to_search=$1
    shift
    echo "$@" | grep "$text_to_search" >/dev/null
}

main "$@"
