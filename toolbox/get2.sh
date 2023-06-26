#!/usr/bin/zsh

PROTOCOL=http
if [[ $PRODUCTION == "true" ]]; then
  PROTOCOL="https"
fi

curl -k  --header "Content-Type: application/json" --header "token: ${token}" --request GET --data $2 $PROTOCOL://localhost:8900/$1
