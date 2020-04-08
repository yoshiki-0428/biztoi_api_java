package com.biztoi.web.utils;

import com.biztoi.model.BizToiUser;
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
                .pictureUrl(user.getAttribute(PICTURE))
                .email(user.getAttribute(EMAIL));
    }
}
