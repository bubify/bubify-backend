#!/bin/sh

SECRET_TOKEN="${AU_GITHUB_USER_SECRET}"
GITHUB_NAME=$1
UU_NAME=$2
USERNAME_ME="${AU_GITHUB_USER}"

REPO_URL="https://api.github.com/repos/$AU_GITHUB_ORG/$UU_NAME"

# Create repo from template
curl \
-u "$USERNAME_ME:$SECRET_TOKEN" \
-X POST \
-H "Accept: application/vnd.github.baptiste-preview+json" \
https://api.github.com/repos/$AU_GITHUB_ORG/Vanilla.Student.Repo/generate \
-d "{\"name\":\"$UU_NAME\", \"owner\":\"$AU_GITHUB_ORG\", \"private\" : true }"

sleep 5

# Add user as owner of repository
curl \
-u "$USERNAME_ME:$SECRET_TOKEN" \
-X PUT \
-H "Accept: application/vnd.github.v3+json" \
$REPO_URL/collaborators/$GITHUB_NAME \
-d "{\"permission\":\"admin\" }"

sleep 5

# # Remove our user from collaborators to avoid spam
curl \
-u "$USERNAME_ME:$SECRET_TOKEN" \
-X DELETE \
-H "Accept: application/vnd.github.v3+json" \
$REPO_URL/collaborators/$USERNAME_ME
