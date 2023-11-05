FROM openjdk:21

LABEL name="memoiry"

RUN microdnf update \
 && microdnf install --nodocs wget unzip \
 && microdnf clean all \
 && rm -rf /var/cache/yum

RUN mkdir -p /root/.postgresql && \
    wget "https://storage.yandexcloud.net/cloud-certs/CA.pem" \
        --output-document ~/.postgresql/root.crt && \
    chmod 0600 /root/.postgresql/root.crt

ARG JAR_FILE=./build/libs/memoiry-bot.jar

WORKDIR /opt/memoiry

COPY $JAR_FILE memoiry-bot.jar

ENTRYPOINT ["java", "-jar", "memoiry-bot.jar"]
