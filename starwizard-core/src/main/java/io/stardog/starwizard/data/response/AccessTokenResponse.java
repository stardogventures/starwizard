package io.stardog.starwizard.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import io.swagger.annotations.ApiModelProperty;

import java.util.Optional;

@AutoValue
public abstract class AccessTokenResponse {
    @JsonProperty("access_token")
    @ApiModelProperty(name = "access_token", value = "access token value", example = "2YotnFZFEjr1zCsicMWsdv")
    public abstract String getAccessToken();

    @JsonProperty("token_type")
    @ApiModelProperty(name = "token_type", value = "access token type", example = "bearer")
    public abstract String getTokenType();

    @JsonProperty("expires_in")
    @ApiModelProperty(name = "expires_in", value = "expiration time of token, in seconds", example = "86400")
    public abstract int getExpiresIn();

    @JsonProperty("refresh_token")
    @ApiModelProperty(name = "refresh_token", value = "refresh token value", example = "tGzv3JOkF0XG5Qx2TlKWIA")
    public abstract Optional<String> getRefreshToken();

    @JsonProperty("scope")
    @ApiModelProperty(name = "scope", value = "scope of the access token", example = "admin")
    public abstract Optional<String> getScope();

    public static AccessTokenResponse of(String accessToken, String tokenType, int expiresIn) {
        return new AutoValue_AccessTokenResponse(accessToken, tokenType, expiresIn, Optional.empty(), Optional.empty());
    }

    public static AccessTokenResponse of(String accessToken, String tokenType, int expiresIn, String refreshToken, String scope) {
        return new AutoValue_AccessTokenResponse(accessToken, tokenType, expiresIn, Optional.ofNullable(refreshToken), Optional.ofNullable(scope));
    }
}
