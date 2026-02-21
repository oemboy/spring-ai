#!/bin/bash
#mvn spring-boot:build-image -Pnative -Dspring.profiles.active=dev
mvn -Pnative clean native:compile -Dspring.profiles.active=dev -DskipTests