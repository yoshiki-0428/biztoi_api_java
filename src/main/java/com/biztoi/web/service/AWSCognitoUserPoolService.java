package com.biztoi.web.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.biztoi.model.BizToiUser;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.biztoi.web.config.ApplicationConst.*;
import static com.biztoi.web.utils.PrincipalUtils.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AWSCognitoUserPoolService {

    Environment env;

    AWSCognitoIdentityProvider client;

    public AWSCognitoUserPoolService(Environment env) {
        this.env = env;
        var credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(env.getProperty(AWS_ACCESSKEY), env.getProperty(AWS_SECRETKEY)));
        this.client = AWSCognitoIdentityProviderClientBuilder.standard().withCredentials(credentialsProvider).withRegion(Regions.AP_NORTHEAST_1).build();
    }

    private static final Logger log = LoggerFactory.getLogger(AWSCognitoUserPoolService.class);

    // TODO ユーザ情報の整理
    public BizToiUser getUser(final String userId) {
        if (userId == null) {
            return null;
        }
        var response = client.adminGetUser(new AdminGetUserRequest()
                .withUserPoolId(env.getProperty(AWS_POOL))
                .withUsername(userId));

        var attribute = response.getUserAttributes();
        var user = new BizToiUser();

        user.setPictureUrl(salvage(filter(attribute, PICTURE)));
        user.setNickname(filter(attribute, NICKNAME));
        return user;
    }

    private static String filter(List<AttributeType> attributeTypes, String attributeName) {
        var attribute = attributeTypes.stream().filter(att -> att.getName().equals(attributeName)).findFirst();
        if (attribute.isEmpty()) {
            return "";
        } else {
            return attribute.get().getValue();
        }
    }

    // TODO CognitoUtils化
    private static String salvage(String picture) {
        // json 判定
        ObjectMapper mapper = new ObjectMapper();
        final PictureJson json;
        try {
            json = mapper.readValue(picture, PictureJson.class);
        } catch (JsonProcessingException e) {
            return picture;
        }

        // urlを取り出す
        return json.data.url;
    }

    static class PictureJson {
        @JsonProperty("data")
        private PictureJson.Data data;

        static class Data {
            @JsonProperty("height")
            private String height;
            @JsonProperty("width")
            private String width;
            @JsonProperty("url")
            private String url;
            @JsonProperty("is_silhouette")
            private String is_silhouette;
        }
    }


}
