#!/bin/bash
source /home/bubify/.bashrc

cd /home/bubify
sudo chown -R bubify:bubify .m2
cd backend

export TERM=xterm

rm -rf target

# Start tests and store a copy of results to file
mvn verify -Dspring.profiles.active=test -Dmaven.test.failure.ignore=true | tee /home/bubify/backend/test_results.txt

# Create JaCoCo report
mvn jacoco:report

# Copy and overwrite test reports in home directory
mkdir -p /home/bubify/backend/test-reports
cp -rf target/failsafe-reports /home/bubify/backend/test-reports
cp -rf target/surefire-reports /home/bubify/backend/test-reports
cp -rf target/site/jacoco /home/bubify/backend/test-reports