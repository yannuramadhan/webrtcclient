/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.settings.pojos;

public class TokenServicePreferences {

    private boolean available;

    private String tokenServer;
    private int tokenPort;
    private boolean secure;
    private String restUrlPath;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getTokenServer() {
        return tokenServer;
    }

    public void setTokenServer(String tokenServer) {
        this.tokenServer = tokenServer;
    }

    public int getTokenPort() {
        return tokenPort;
    }

    public void setTokenPort(int tokenPort) {
        this.tokenPort = tokenPort;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getRestUrlPath() {
        return restUrlPath;
    }

    public void setRestUrlPath(String restUrlPath) {
        this.restUrlPath = restUrlPath;
    }
}
