package com.avaya.oceanareferenceclient.authorization.TokenPojos;

import androidx.annotation.Keep;

@Keep
public class AAWGTokenResponse {
    private String encryptedToken;

    public String getEncryptedToken() {
        return encryptedToken;
    }

    public void setEncryptedToken(String encryptedToken) {
        this.encryptedToken = encryptedToken;
    }
}
