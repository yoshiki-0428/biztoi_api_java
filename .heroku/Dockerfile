FROM registry.gitlab.com/rrrtcdish2/biztoi_api_java/feature/cognito:latest
ENV SPRING_PROFILES_ACTIVE='heroku' \
    SPRING_DATASOURCE_URL='jdbc:postgresql://ec2-3-213-192-58.compute-1.amazonaws.com:5432/dakc80irsitdn7' \
    SPRING_DATASOURCE_USERNAME='nuvkalnteozjei' \
    SPRING_DATASOURCE_PASSWORD='310e9b44e58545ebc10e907eac269615276e0b239a4ff4b40e3410eafb3a70d8' \
    SPRING_DATASOURCE_DRIVER_CLASS_NAME='org.postgresql.Driver' \
    RAKUTEN_APP-ID="1035252894012359396" \
    SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_BIZTOI-APP-CLIENT_CLIENT-ID="7o2r51udt31kp738eumcvkj7eg" \
    SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_BIZTOI-APP-CLIENT_CLIENT-SECRET="1674uobc1lvpio2v8n9qvifecljiimce9dga7drf9amrq55i8bs4" \
    SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_BIZTOI-APP-CLIENT_CLIENT-NAME="biztoi-app-client-dev" \
    SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_BIZTOI-APP-CLIENT_PROVIDER="cognito" \
    SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_BIZTOI-APP-CLIENT_SCOPE="openid, email, profile, aws.cognito.signin.user.admin" \
    SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_BIZTOI-APP-CLIENT_REDIRECT-URI="https://biztoi-api-java.herokuapp.com/login/oauth2/code/cognito" \
    SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_BIZTOI-APP-CLIENT_AUTHORIZATION-GRANT-TYPE="authorization_code" \
    SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_COGNITO_AUTHORIZATION-URI="https://biztoi-app-dev.auth.ap-northeast-1.amazoncognito.com/oauth2/authorize" \
    SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_COGNITO_TOKEN-URI="https://biztoi-app-dev.auth.ap-northeast-1.amazoncognito.com/oauth2/token" \
    SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_COGNITO_USER-INFO-URI="https://biztoi-app-dev.auth.ap-northeast-1.amazoncognito.com/oauth2/userInfo" \
    SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_COGNITO_JWK-SET-URI="https://cognito-idp.ap-northeast-1.amazonaws.com/ap-northeast-1_ccxqYNJs0/.well-known/jwks.json" \
    SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_COGNITO_USER-NAME-ATTRIBUTE="username" \
    APPLICATION_FRONT-URL="http://hoge-hoge.biztoi.com/top"
CMD echo "The application will start in ${JHIPSTER_SLEEP}s..." && \
    sleep ${JHIPSTER_SLEEP} && \
    java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.war --server.port=$PORT
