#!/usr/bin/zsh
curl --silent http://127.0.0.1:8900/login/$1 | cut -d \" -f 4

