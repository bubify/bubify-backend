#!/bin/bash
source /home/bubify/.bashrc

cd /home/bubify
sudo chown -R bubify:bubify .m2
cd backend

export TERM=xterm

rm -rf target

# Start tests and stores a copy of results to a file
mvn test -Dspring.profiles.active=test -Dmaven.test.failure.ignore=true | tee /home/bubify/backend/test_results.txt

# mvn surefire:test -Dspring.profiles.active=test -Dmaven.test.failure.ignore=true > surefire_output.txt 2>&1

# mvn failsafe:integration-test -Dspring.profiles.active=test -Dmaven.test.failure.ignore=true > failsafe_output.txt 2>&1

# Create and copy JaCoCo report to the local directory
mvn jacoco:report
cp -r target/site/jacoco /home/bubify/backend/jacoco