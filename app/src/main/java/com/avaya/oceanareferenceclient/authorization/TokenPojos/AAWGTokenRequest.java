package com.avaya.oceanareferenceclient.authorization.TokenPojos;

import androidx.annotation.Keep;

/**
 * Created by ggrings on 3/14/18.
 */
@Keep
public class AAWGTokenRequest {
    private String use = "csaGuest";
    private String calledNumber;
    private String callingNumber;
    private String displayName;
    private String expiration = "120000";

    public AAWGTokenRequest() {
    }

    public AAWGTokenRequest(String contact, String identity, String displayName) {
        this.calledNumber = contact;
        this.callingNumber = identity;
        this.displayName = displayName;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getCalledNumber() {
        return calledNumber;
    }

    public void setCalledNumber(String calledNumber) {
        this.calledNumber = calledNumber;
    }

    public String getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(String callingNumber) {
        this.callingNumber = callingNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }
}
