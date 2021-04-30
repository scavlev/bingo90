FROM adoptopenjdk/openjdk16:alpine AS build

COPY ./ ./

RUN ./gradlew build

FROM adoptopenjdk/openjdk16:alpine-jre

COPY --from=build /build/libs/bingo-all.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]