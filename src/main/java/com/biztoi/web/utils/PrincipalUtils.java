package com.biztoi.web.utils;

import com.biztoi.model.BizToiUser;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.Principal;

public class PrincipalUtils {

    public static String SUB = "sub";

    public static String USERNAME = "username";

    public static String NICKNAME = "nickname";

    public static String EMAIL = "email";

    public static String PICTURE = "picture";

    public static String getCognitoUserName(Principal p) {
        final OAuth2User user = ((OAuth2AuthenticationToken) p).getPrincipal();
        return user.getAttribute(USERNAME);
    }

    public static BizToiUser getBizToiUser(Principal p) {
        final OAuth2User user = ((OAuth2AuthenticationToken) p).getPrincipal();
        return new BizToiUser()
                .id(user.getAttribute(USERNAME))
                .nickname(user.getAttribute(NICKNAME))
                .pictureUrl(salvage(user.getAttribute(PICTURE)))
                .email(user.getAttribute(EMAIL));
    }

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
        private Data data;

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
