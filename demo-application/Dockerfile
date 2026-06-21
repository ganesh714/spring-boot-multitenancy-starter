FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY multitenancy-spring-boot-starter/pom.xml multitenancy-spring-boot-starter/
COPY demo-application/pom.xml demo-application/
# Copy the rest of the source code
COPY multitenancy-spring-boot-starter/src multitenancy-spring-boot-starter/src
COPY demo-application/src demo-application/src

RUN mvn clean install -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/demo-application/target/*.jar app.jar
RUN apk add --no-cache curl
ENTRYPOINT ["java", "-jar", "app.jar"]
