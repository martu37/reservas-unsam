# Compila el jar con Gradle y lo corre con una imagen solo con Java (más liviana)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
# Primero solo lo de Gradle, así las dependencias quedan cacheadas entre builds
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q > /dev/null
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
