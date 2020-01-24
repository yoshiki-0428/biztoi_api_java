FROM maven:3-jdk-8-alpine
ADD . /code/
RUN echo '{ "allow_root": true }' > /root/.bowerrc && \
    cd /code/ && \
    mvn clean package -DskipTests && \
    mv /code/target/*.jar /app.war

FROM openjdk:8-jre-alpine
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0 \
    JAVA_OPTS="-Xms512m -Xmx512m"
RUN apk update && apk upgrade \
    && apk --update add tzdata nss \
    && apk add --no-cache git
CMD echo "The application will start in ${JHIPSTER_SLEEP}s..." && \
    sleep ${JHIPSTER_SLEEP} && \
    java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.war
COPY --from=0 /app.war .
