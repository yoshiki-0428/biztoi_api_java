# BizToi

## Setups

### Docker

```
docker-compose up -d
```

### Local

```
git submodule sync
git submodule update --init
docker-compose up -d db
mvn install
mvn exec java
```

### Deploys (Heroku)

```
cd biztoi_api_java
docker-compose pull
cd .heroku
heroku container:push web -a biztoi-api-java && heroku container:release web -a biztoi-api-java
heroku logs --tail -a biztoi-api-java
```

## Skills

- Java11
- OpenAPI
- SpringBoot 2.2.1
- Spring Security OAuth2 client
- Spring Cloud Hoxton.SR1
- Spring Cloud Feign

