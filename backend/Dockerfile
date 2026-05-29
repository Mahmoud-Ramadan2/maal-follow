FROM eclipse-temurin:21-jre-jammy AS runtime


RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

 # a non-root user
ENV APP_USER=appuser
RUN addgroup --system $APP_USER && adduser --system --ingroup $APP_USER $APP_USER

WORKDIR /app

# App jar
COPY target/maal-follow-be.jar app.jar

# Use non-root
USER $APP_USER

EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-Xms256m", "-Xmx1024m", "-jar", "/app/app.jar"]
