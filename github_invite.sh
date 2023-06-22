#!/bin/sh

SECRET_TOKEN=<TODO>
GITHUB_NAME=$1
UU_NAME=$2
USERNAME_ME=<TODO> (e.g. TobiasWrigstad)

REPO_URL="https://api.github.com/repos/IOOPM-UU/$UU_NAME"

# Invite user to organisation
curl \
-u "$USERNAME_ME:$SECRET_TOKEN" \
-X PUT \
-H "Accept: application/vnd.github.v3+json" \
https://api.github.com/orgs/IOOPM-UU/memberships/$GITHUB_NAME \
-d '{"role": "member"}'

