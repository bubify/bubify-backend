#!/bin/bash
source /home/bubify/.bashrc

cd /home/bubify
sudo chown -R bubify:bubify .m2
cd backend

export TERM=xterm

rm -rf target

# Start tests and stores a copy of results to a file
mvn test -Dspring.profiles.active=application-test -Dmaven.test.failure.ignore=true | tee /home/bubify/backend/test_results.txt
