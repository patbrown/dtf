FROM clojure:openjdk-11-tools-deps-1.10.3.814-buster AS builder

WORKDIR /opt

COPY . .

ARG SALT
ARG BUILD
RUN clojure -A:dev
RUN clj -Sdeps '{:mvn/local-repo "./.m2/repository"}' -X:build uber

FROM openjdk:11-slim-buster AS runtime
COPY --from=builder /opt/target/api-versionless-run.jar /run.jar

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "run.jar", "clojure.main", "-m", "net.drilling.run.api"]