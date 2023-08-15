#!/bin/sh

SECRET_TOKEN="${AU_GITHUB_USER_SECRET}"
GITHUB_NAME=$1
UU_NAME=$2
USERNAME_ME="${AU_GITHUB_USER}"

REPO_URL="https://api.github.com/repos/$AU_GITHUB_ORG/$UU_NAME"

# Invite user to organisation
curl \
-u "$USERNAME_ME:$SECRET_TOKEN" \
-X PUT \
-H "Accept: application/vnd.github.v3+json" \
https://api.github.com/orgs/$AU_GITHUB_ORG/memberships/$GITHUB_NAME \
-d '{"role": "member"}'

