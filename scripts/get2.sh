#!/usr/bin/zsh
curl --silent --header "Content-Type: application/json" --header "token: ${token}" --request GET --data $2 http://localhost:8900/$1
