FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .

COPY services/common-dtos/pom.xml services/common-dtos/pom.xml
COPY services/common-dtos/src services/common-dtos/src

COPY services/main-api/pom.xml services/main-api/pom.xml

COPY services/notification-service/pom.xml services/notification-service/pom.xml
COPY services/notification-service/src services/notification-service/src

RUN mvn clean package -DskipTests -pl services/notification-service -am

FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=builder /app/services/notification-service/target/*.jar app.jar
ENV JAVA_OPTS="-XX:TieredStopAtLevel=1 -XX:+UseSerialGC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]