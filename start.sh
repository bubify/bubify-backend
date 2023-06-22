#!/bin/bash
source /home/bubify/.bashrc

cd /home/bubify/backend

while true; do
  mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=development -Dspring-boot.run.jvmArguments=-XX:+UseG1GC
  exit_code=$?
  if [ $exit_code -eq 0 ]; then
    echo "Spring application exited gracefully."
    break
  else
    echo "Spring application exited with error. Restarting..."
    sleep 2s
  fi
done
