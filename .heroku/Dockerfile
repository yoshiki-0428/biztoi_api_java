FROM registry.gitlab.com/rrrtcdish2/biztoi_api_java/feature/openapi:latest
ENV SPRING_PROFILES_ACTIVE='heroku'
ENV RAKUTEN_APP-ID="1035252894012359396"
CMD echo "The application will start in ${JHIPSTER_SLEEP}s..." && \
    sleep ${JHIPSTER_SLEEP} && \
    java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.war --server.port=$PORT
