# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Leverage layer caching
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package && \
    cp target/*.jar /app/app.jar

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
#ENV JAVA_OPTS=""
COPY --from=build /app/app.jar ./app.jar

# Expose Spring Boot default port
EXPOSE 8080

CMD java -jar app.jar

