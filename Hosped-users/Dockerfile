FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:25-jre
ARG BUILD_DATE
ARG VCS_REF
ARG VERSION=1.0.0
LABEL org.opencontainers.image.title="Microsservico de Usuarios Hosped" \
      org.opencontainers.image.description="Microsservico de usuarios do sistema de hotel desenvolvido por Samuel Gislon, Lucas Jacinto e Nicoli Zimmermann estudantes da UDESC" \
      org.opencontainers.image.source="https://github.com/SamuelGislon/Hotel-microservicos" \
      org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.revision="${VCS_REF}" \
      org.opencontainers.image.version="${VERSION}"
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
