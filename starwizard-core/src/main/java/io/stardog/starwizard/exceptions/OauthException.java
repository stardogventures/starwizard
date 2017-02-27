package io.stardog.starwizard.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.BadRequestException;

public class OauthException extends BadRequestException {
    private final String error;
    private final String errorDescription;

    public OauthException(String error, String errorDescription) {
        this.error = error;
        this.errorDescription = errorDescription;
    }

    @JsonProperty("error")
    public String getError() {
        return error;
    }

    @JsonProperty("error_description")
    public String getErrorDescription() {
        return errorDescription;
    }
}
