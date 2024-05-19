/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.settings.pojos;

public class LoggingPreferences {

    boolean logToDisk;
    String logFileName;

    public boolean isLogToDiskEnabled() {
        return logToDisk;
    }

    public void setLogToDiskEnabled(boolean logToDisk) {
        this.logToDisk = logToDisk;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }
}
