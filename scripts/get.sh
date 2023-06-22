#!/usr/bin/zsh
token=`./scripts/get_token.sh "$1"`
curl --silent --header "Content-Type: application/json" --header "token: ${token}" --request GET --data $3 http://localhost:8900/$2
