#!/bin/bash
source ~/.bashrc

# Set environment variables
export AU_BACKEND_HOST="http://localhost:8900"
export AU_FRONTEND_HOST="http://localhost:3000"
export AU_APP_API="http://localhost:8900"
export AU_APP_WEBSOCKET="http://localhost:8900/portfolio"
export AU_DB_PORT="3306"
export AU_DB_USER="ioopm"
export AU_DB_PASSWORD="ioopm"
export AU_GITHUB_ORG="CHANGE_ME"
export AU_GITHUB_USER="CHANGE_ME"
export AU_GITHUB_USER_SECRET="CHANGE_ME"
export AU_GITHUB_CLIENT_ID="CHANGE_ME"
export AU_GITHUB_CLIENT_SECRET="CHANGE_ME"

export AU_KEY_ALIAS="tomcat"
export AU_KEY_STORE="$(pwd)/keystore/keystore.p12"
export AU_KEY_STORE_PASSWORD="password"
export AU_KEY_STORE_TYPE="PKCS12"
export AU_PROFILE_PICTURE_DIR="$(pwd)/profile_pictures/"
export AU_BACKUP_DIR="$(pwd)/au_backups/"
export TERM=xterm

# Create directories
mkdir -p au_backups
mkdir -p profile_pictures
mkdir -p keystore
mkdir -p logs

cd backend
rm -rf target

# Start tests and store a copy of results to file
# mvn verify -Dspring.profiles.active=test -Dmaven.test.failure.ignore=true | tee test_results.txt

# Create JaCoCo report (stored in target/site/jacoco/)
# mvn jacoco:report

# Clean up
# rm -rf ../au_backups
# rm -rf ../profile_pictures
# rm -rf ../keystore
# rm -rf ../logs