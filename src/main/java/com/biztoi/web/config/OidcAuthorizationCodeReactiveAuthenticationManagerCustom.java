package com.biztoi.web.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.ReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeReactiveAuthenticationManager;
import org.springframework.security.oauth2.client.oidc.authentication.ReactiveOidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoderFactory;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;

/**
 * @see OidcAuthorizationCodeReactiveAuthenticationManager
 */
public class OidcAuthorizationCodeReactiveAuthenticationManagerCustom extends OidcAuthorizationCodeReactiveAuthenticationManager {

    private static final String INVALID_STATE_PARAMETER_ERROR_CODE = "invalid_state_parameter";
    private static final String INVALID_REDIRECT_URI_PARAMETER_ERROR_CODE = "invalid_redirect_uri_parameter";
    private static final String INVALID_ID_TOKEN_ERROR_CODE = "invalid_id_token";
    private static final String INVALID_NONCE_ERROR_CODE = "invalid_nonce";

    private final ReactiveOAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient;

    private final ReactiveOAuth2UserService<OidcUserRequest, OidcUser> userService;

    private GrantedAuthoritiesMapper authoritiesMapper = (authorities -> authorities);

    private ReactiveJwtDecoderFactory<ClientRegistration> jwtDecoderFactory = new ReactiveOidcIdTokenDecoderFactory();

    public OidcAuthorizationCodeReactiveAuthenticationManagerCustom(ReactiveOAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient,
                                                                    ReactiveOAuth2UserService<OidcUserRequest, OidcUser> userService) {
        super(accessTokenResponseClient, userService);
        this.accessTokenResponseClient = accessTokenResponseClient;
        this.userService = userService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.defer(() -> {
            OAuth2AuthorizationCodeAuthenticationToken authorizationCodeAuthentication = (OAuth2AuthorizationCodeAuthenticationToken) authentication;

            // Section 3.1.2.1 Authentication Request - https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest
            // scope REQUIRED. OpenID Connect requests MUST contain the "openid" scope value.
            if (!authorizationCodeAuthentication.getAuthorizationExchange()
                    .getAuthorizationRequest().getScopes().contains("openid")) {
                // This is an OpenID Connect Authentication Request so return empty
                // and let OAuth2LoginReactiveAuthenticationManager handle it instead
                return Mono.empty();
            }


            OAuth2AuthorizationRequest authorizationRequest = authorizationCodeAuthentication
                    .getAuthorizationExchange().getAuthorizationRequest();
            OAuth2AuthorizationResponse authorizationResponse = authorizationCodeAuthentication
                    .getAuthorizationExchange().getAuthorizationResponse();

            if (authorizationResponse.statusError()) {
                throw new OAuth2AuthenticationException(
                        authorizationResponse.getError(), authorizationResponse.getError().toString());
            }

            if (!authorizationResponse.getState().equals(authorizationRequest.getState())) {
                OAuth2Error oauth2Error = new OAuth2Error(INVALID_STATE_PARAMETER_ERROR_CODE);
                throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
            }

            final String responseRedirectUri = authorizationResponse.getRedirectUri().split("://")[1];
            final String requestRedirectUri = authorizationRequest.getRedirectUri().split("://")[1];
            if (!responseRedirectUri.equals(requestRedirectUri)) {
                OAuth2Error oauth2Error = new OAuth2Error(INVALID_REDIRECT_URI_PARAMETER_ERROR_CODE);
                throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
            }

            OAuth2AuthorizationCodeGrantRequest authzRequest = new OAuth2AuthorizationCodeGrantRequest(
                    authorizationCodeAuthentication.getClientRegistration(),
                    authorizationCodeAuthentication.getAuthorizationExchange());

            return this.accessTokenResponseClient.getTokenResponse(authzRequest)
                    .flatMap(accessTokenResponse -> authenticationResult(authorizationCodeAuthentication, accessTokenResponse))
                    .onErrorMap(OAuth2AuthorizationException.class, e -> new OAuth2AuthenticationException(e.getError(), e.getError().toString()))
                    .onErrorMap(JwtException.class, e -> {
                        OAuth2Error invalidIdTokenError = new OAuth2Error(INVALID_ID_TOKEN_ERROR_CODE, e.getMessage(), null);
                        throw new OAuth2AuthenticationException(invalidIdTokenError, invalidIdTokenError.toString(), e);
                    });
        });
    }

    private Mono<OAuth2LoginAuthenticationToken> authenticationResult(OAuth2AuthorizationCodeAuthenticationToken authorizationCodeAuthentication, OAuth2AccessTokenResponse accessTokenResponse) {
        OAuth2AccessToken accessToken = accessTokenResponse.getAccessToken();
        ClientRegistration clientRegistration = authorizationCodeAuthentication.getClientRegistration();
        Map<String, Object> additionalParameters = accessTokenResponse.getAdditionalParameters();

        if (!additionalParameters.containsKey(OidcParameterNames.ID_TOKEN)) {
            OAuth2Error invalidIdTokenError = new OAuth2Error(
                    INVALID_ID_TOKEN_ERROR_CODE,
                    "Missing (required) ID Token in Token Response for Client Registration: " + clientRegistration.getRegistrationId(),
                    null);
            throw new OAuth2AuthenticationException(invalidIdTokenError, invalidIdTokenError.toString());
        }

        return createOidcToken(clientRegistration, accessTokenResponse)
                .doOnNext(idToken -> validateNonce(authorizationCodeAuthentication, idToken))
                .map(idToken -> new OidcUserRequest(clientRegistration, accessToken, idToken, additionalParameters))
                .flatMap(this.userService::loadUser)
                .map(oauth2User -> {
                    Collection<? extends GrantedAuthority> mappedAuthorities =
                            this.authoritiesMapper.mapAuthorities(oauth2User.getAuthorities());

                    return new OAuth2LoginAuthenticationToken(
                            authorizationCodeAuthentication.getClientRegistration(),
                            authorizationCodeAuthentication.getAuthorizationExchange(),
                            oauth2User,
                            mappedAuthorities,
                            accessToken,
                            accessTokenResponse.getRefreshToken());
                });
    }

    private Mono<OidcIdToken> createOidcToken(ClientRegistration clientRegistration, OAuth2AccessTokenResponse accessTokenResponse) {
        ReactiveJwtDecoder jwtDecoder = this.jwtDecoderFactory.createDecoder(clientRegistration);
        String rawIdToken = (String) accessTokenResponse.getAdditionalParameters().get(OidcParameterNames.ID_TOKEN);
        return jwtDecoder.decode(rawIdToken)
                .map(jwt -> new OidcIdToken(jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), jwt.getClaims()));
    }

    private static Mono<OidcIdToken> validateNonce(OAuth2AuthorizationCodeAuthenticationToken authorizationCodeAuthentication, OidcIdToken idToken) {
        String requestNonce = authorizationCodeAuthentication.getAuthorizationExchange()
                .getAuthorizationRequest().getAttribute(OidcParameterNames.NONCE);
        if (requestNonce != null) {
            String nonceHash;
            try {
                nonceHash = createHash(requestNonce);
            } catch (NoSuchAlgorithmException e) {
                OAuth2Error oauth2Error = new OAuth2Error(INVALID_NONCE_ERROR_CODE);
                throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
            }
            String nonceHashClaim = idToken.getNonce();
            if (nonceHashClaim == null || !nonceHashClaim.equals(nonceHash)) {
                OAuth2Error oauth2Error = new OAuth2Error(INVALID_NONCE_ERROR_CODE);
                throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
            }
        }

        return Mono.just(idToken);
    }

    static String createHash(String nonce) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(nonce.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

}
