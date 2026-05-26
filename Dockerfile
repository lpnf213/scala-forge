# Build stage: compile and package the fat JAR
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.10_7_1.10.3_2.13.15 AS builder
WORKDIR /workspace

COPY . .
RUN sbt clean assembly

# Runtime stage: ship only JRE + packaged artifact
FROM eclipse-temurin:17-jre-jammy
WORKDIR /opt/scala-forge

COPY --from=builder /workspace/target/scala-2.13/scala-forge.jar /opt/scala-forge/scala-forge.jar

ENTRYPOINT ["java", "-jar", "/opt/scala-forge/scala-forge.jar"]
CMD ["--help"]
