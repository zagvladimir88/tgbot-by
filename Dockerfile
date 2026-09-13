FROM eclipse-temurin:25-jre AS extractor

WORKDIR /builder
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher

FROM eclipse-temurin:25-jre

RUN groupadd --system tgbot && useradd --system --gid tgbot --home /app tgbot

WORKDIR /app

COPY --from=extractor --chown=tgbot:tgbot /builder/app/dependencies/ ./
COPY --from=extractor --chown=tgbot:tgbot /builder/app/spring-boot-loader/ ./
COPY --from=extractor --chown=tgbot:tgbot /builder/app/snapshot-dependencies/ ./
COPY --from=extractor --chown=tgbot:tgbot /builder/app/application/ ./

USER tgbot

ENV JAVA_TOOL_OPTIONS="-XX:+UseCompactObjectHeaders -XX:MaxRAMPercentage=75 -Djava.awt.headless=true"

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
