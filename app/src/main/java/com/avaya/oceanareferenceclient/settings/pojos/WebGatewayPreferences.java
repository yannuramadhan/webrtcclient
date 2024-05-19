/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.settings.pojos;

public class WebGatewayPreferences {

    private boolean available;

    private String aawgServer;
    private int aawgPort;
    private boolean secure;
    private String aawgUrlPath;

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getAawgServer() {
        return aawgServer;
    }

    public void setAawgServer(String aawgServer) {
        this.aawgServer = aawgServer;
    }

    public int getAawgPort() {
        return aawgPort;
    }

    public void setAawgPort(int aawgPort) {
        this.aawgPort = aawgPort;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getAawgUrlPath() {
        return aawgUrlPath;
    }

    public void setAawgUrlPath(String aawgUrlPath) {
        this.aawgUrlPath = aawgUrlPath;
    }
}
