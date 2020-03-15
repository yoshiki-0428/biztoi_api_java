FROM registry.gitlab.com/rrrtcdish2/biztoi_api_java/feature/cognito:latest
ENV SPRING_PROFILES_ACTIVE='heroku'
CMD echo "The application will start in ${JHIPSTER_SLEEP}s..." && \
    sleep ${JHIPSTER_SLEEP} && \
    java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.war --server.port=$PORT
