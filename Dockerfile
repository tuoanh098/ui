FROM maven:3.9.5-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy source and build
COPY . /workspace
RUN mvn -DskipTests package -q

FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /workspace/target/backend-0.0.1-SNAPSHOT.jar /app/backend.jar

ENV JAVA_OPTS=""
EXPOSE 8081
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/backend.jar"]

