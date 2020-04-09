package com.biztoi.web.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.biztoi.model.BizToiUser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.biztoi.web.utils.PrincipalUtils.*;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AWSCognitoUserPoolService {

    @NonNull
    Environment env;

    private static final Logger log = LoggerFactory.getLogger(AWSCognitoUserPoolService.class);

    // TODO application 変数のConst化
    // TODO ユーザ情報の整理
    public BizToiUser getUser(final String userId) {
        if (userId == null) {
            return null;
        }
        var credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(env.getProperty("aws.accessKeyId"), env.getProperty("aws.secretKey")));
        var client = AWSCognitoIdentityProviderClientBuilder.standard().withCredentials(credentialsProvider).withRegion(Regions.AP_NORTHEAST_1).build();
        com.amazonaws.services.cognitoidp.model.AdminGetUserRequest adminGetUserRequest = new AdminGetUserRequest();
        adminGetUserRequest.withUserPoolId(env.getProperty("application.aws.pool")).withUsername(userId);
        var response = client.adminGetUser(adminGetUserRequest);
        log.info(response.getUserAttributes().toString());

        var attribute = response.getUserAttributes();
        var user = new BizToiUser();

        user.setPictureUrl(filter(attribute, PICTURE));
        user.setEmail(filter(attribute, EMAIL));
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

}
