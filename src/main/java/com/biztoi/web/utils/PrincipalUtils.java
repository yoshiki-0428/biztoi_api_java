package com.biztoi.web.utils;

import com.biztoi.model.BizToiUser;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.Principal;

public class PrincipalUtils {
    private static String SUB = "sub";

    private static String NAME = "nickname";

    private static String EMAIL = "email";

    private static String PICTURE = "picture";

    public static String getUserId(Principal p) {
        final OAuth2User user = ((OAuth2AuthenticationToken) p).getPrincipal();
        return user.getAttribute(SUB);
    }

    public static BizToiUser getBizToiUser(Principal p) {
        final OAuth2User user = ((OAuth2AuthenticationToken) p).getPrincipal();
        return new BizToiUser()
                .id(user.getAttribute(SUB))
                .nickname(user.getAttribute(NAME))
                .pictureUrl(user.getAttribute(PICTURE))
                .email(user.getAttribute(EMAIL));
    }
}
