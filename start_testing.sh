#!/bin/bash
source /home/bubify/.bashrc

cd /home/bubify
sudo chown -R bubify:bubify .m2
cd backend

export TERM=xterm

rm -rf target

while true; do
  # TODO: Change the profile to test, should run the tests and then terminate
  mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=development -Dspring-boot.run.jvmArguments=-XX:+UseG1GC -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" &

  while true; do 
    watch -d -t -g "ls -lR src | sha1sum" && mvn compile && curl http://localhost:8900/internal/restart
  done

done
