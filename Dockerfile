FROM eclipse-temurin:17-jre
WORKDIR /app

ENV JAVA_OPTS=""
COPY target/onboarding-springboot-demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
