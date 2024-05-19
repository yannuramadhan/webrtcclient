package com.avaya.oceanareferenceclient.authorization;

public class AuthorizationResponse {
    boolean successful;
    String data;

    public AuthorizationResponse(boolean successful) {
        this.successful = successful;
    }

    public AuthorizationResponse(boolean successful, String data) {
        this.successful = successful;
        this.data = data;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getData() {
        return data;
    }
}
