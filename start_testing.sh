#!/bin/bash
source /home/bubify/.bashrc

cd /home/bubify
sudo chown -R bubify:bubify .m2
cd backend

export TERM=xterm

rm -rf target

# Start tests based on the argument
if [ "$TEST_TYPE" == "unit" ]; then
    echo "Running only unit tests..."
    mvn test -Dspring.profiles.active=test -Dmaven.test.failure.ignore=true
elif [ "$TEST_TYPE" == "integration" ]; then
    echo "Running only integration tests..."
    mvn verify -Dspring.profiles.active=test -Dmaven.test.failure.ignore=true -DskipUnit=true
else
    echo "Running all tests..."
    mvn verify -Dspring.profiles.active=test -Dmaven.test.failure.ignore=true
fi

# Create JaCoCo report
mvn jacoco:report

# Copy and overwrite test reports in home directory
mkdir -p /home/bubify/backend/test-reports
cp -rf target/failsafe-reports /home/bubify/backend/test-reports
cp -rf target/surefire-reports /home/bubify/backend/test-reports
cp -rf target/site/jacoco /home/bubify/backend/test-reports