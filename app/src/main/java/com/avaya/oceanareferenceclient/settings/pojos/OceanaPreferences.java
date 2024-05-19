/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.settings.pojos;

public class OceanaPreferences implements AvayaPlatformPreferences {

    private boolean available;
    private boolean secure;
    private String amcServer;
    private int amcPort;
    private String amcUrlPath;

    private String destination;
    private String context;
    private String topic;
    private String priority;
    private String locale;
    private String strategy;
    private String sourceName;
    private String resourceId;

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getAmcServer() {
        return amcServer;
    }

    public void setAmcServer(String amcServer) {
        this.amcServer = amcServer;
    }

    public int getAmcPort() {
        return amcPort;
    }

    public void setAmcPort(int amcPort) {
        this.amcPort = amcPort;
    }

    public String getAmcUrlPath() {
        return amcUrlPath;
    }

    public void setAmcUrlPath(String amcUrlPath) {
        this.amcUrlPath = amcUrlPath;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
