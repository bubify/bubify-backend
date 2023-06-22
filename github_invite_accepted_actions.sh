#!/bin/sh

SECRET_TOKEN=<TODO>
GITHUB_NAME=$1
UU_NAME=$2
USERNAME_ME=<TODO> (e.g. TobiasWrigstad)

REPO_URL="https://api.github.com/repos/IOOPM-UU/$UU_NAME"

# Create repo from template
curl \
-u "$USERNAME_ME:$SECRET_TOKEN" \
-X POST \
-H "Accept: application/vnd.github.baptiste-preview+json" \
https://api.github.com/repos/IOOPM-UU/Vanilla.Student.Repo/generate \
-d "{\"name\":\"$UU_NAME\", \"owner\":\"IOOPM-UU\", \"private\" : true }"

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
