#!/bin/bash
cd "$(dirname "$0")"
echo "Starting Backend - mvnw spring-boot:run -pl open-admin-flowable-app"
./mvnw spring-boot:run -pl open-admin-flowable-app
