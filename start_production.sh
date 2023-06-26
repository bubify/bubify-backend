#!/bin/bash
source /home/bubify/.bashrc

cd /home/bubify
sudo chown -R bubify:bubify .m2
cd backend

mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=production -Dspring-boot.run.jvmArguments=-XX:+UseG1GC
