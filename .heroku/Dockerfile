FROM registry.gitlab.com/rrrtcdish2/biztoi_api_java/feature/openapi:latest
ENV SPRING_PROFILES_ACTIVE='heroku' \
    SPRING_DATASOURCE_URL='jdbc:postgresql://ec2-3-213-192-58.compute-1.amazonaws.com:5432/dakc80irsitdn7' \
    SPRING_DATASOURCE_USERNAME='nuvkalnteozjei' \
    SPRING_DATASOURCE_PASSWORD='310e9b44e58545ebc10e907eac269615276e0b239a4ff4b40e3410eafb3a70d8' \
    SPRING_DATASOURCE_DRIVER_CLASS_NAME='org.postgresql.Driver' \
    RAKUTEN_APP-ID="1035252894012359396"
CMD echo "The application will start in ${JHIPSTER_SLEEP}s..." && \
    sleep ${JHIPSTER_SLEEP} && \
    java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.war --server.port=$PORT
