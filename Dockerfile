FROM eclipse-temurin:21-jdk AS build
ARG MODULE
WORKDIR /src
COPY . .
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon ":$(echo ${MODULE} | tr / :):bootJar" -x test \
 && cp $(ls ${MODULE}/build/libs/*.jar | grep -v plain) /app.jar

FROM eclipse-temurin:21-jdk
COPY --from=build /app.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]