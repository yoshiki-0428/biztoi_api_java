FROM maven:3.6.3-jdk-11-openj9
ADD . /code/
RUN echo '{ "allow_root": true }' > /root/.bowerrc && \
    cd /code/ && \
    mvn clean package -DskipTests && \
    mv /code/target/*.jar /app.war

FROM maven:3.6.3-jdk-11-openj9
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0 \
    JAVA_OPTS="-Xms512m -Xmx512m"
#RUN apk update && apk upgrade \
#    && apk --update add tzdata nss \
#    && apk add --no-cache git
CMD echo "The application will start in ${JHIPSTER_SLEEP}s..." && \
    sleep ${JHIPSTER_SLEEP} && \
    java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.war
COPY --from=0 /app.war .
