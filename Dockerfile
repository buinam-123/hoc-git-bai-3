# syntax=docker/dockerfile:1
# Một Dockerfile cho cả 8 ứng dụng Java (gateway + 7 service). Chọn module bằng build-arg:
#   docker build -t calc/add-service:1.0.0 --build-arg MODULE=add-service .
#   docker build -t calc/gateway:1.0.0     --build-arg MODULE=gateway --build-arg SERVER_PORT=3000 .

FROM maven:3.9-eclipse-temurin-17 AS build
ARG MODULE
WORKDIR /src
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl ${MODULE} -am -DskipTests package

FROM eclipse-temurin:17-jre-alpine
ARG MODULE
ARG SERVER_PORT=8080
ENV SERVER_PORT=${SERVER_PORT} \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
RUN addgroup -S -g 1000 app && adduser -S -u 1000 -G app app
USER 1000
WORKDIR /app
COPY --from=build /src/${MODULE}/target/app.jar app.jar
EXPOSE ${SERVER_PORT}
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
