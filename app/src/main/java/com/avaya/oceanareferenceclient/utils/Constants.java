/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.utils;

import okhttp3.MediaType;

public interface Constants {

    long TIMER_INTERVAL = 1000L;
    long STATS_UPDATE_INTERVAL = 3000L;
    String CALL_TIME_ELAPSED_FORMAT = "%02d:%02d";
    String CALL_TIME_ELAPSED_SEPARATOR = "(";
    String CALL_TIME_ELAPSED_END = ")";

    String PLACEHOLDER1 = "%1";
    String REPORT_ISSUE_SUBJECT = "Reporting issue " + PLACEHOLDER1;

    String HTTP = "http://";
    String HTTPS = "https://";
    String SERVER_PLACEHOLDER = "{server}";
    String AAWG_TOKEN_URL_PATH_PLACEHOLDER = "{tokenUrlPath}";

    String PORT_PLACEHOLDER = "{port}";

    String AAWG_RETRIEVE_TOKEN_URL = HTTPS + SERVER_PLACEHOLDER + ":" + PORT_PLACEHOLDER + "/" + AAWG_TOKEN_URL_PATH_PLACEHOLDER;
    String AAWG_RETRIEVE_TOKEN_INSECURE_URL = HTTP + SERVER_PLACEHOLDER + ":" + PORT_PLACEHOLDER + "/" + AAWG_TOKEN_URL_PATH_PLACEHOLDER;

    String DATA_KEY_TOKEN = "_token";
    String DATA_KEY_VIDEO_RESOLUTION = "_videoResolution";
    String DATA_KEY_VIDEO_ORIENTATION = "_videoOrientation";


    // MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    MediaType AAWG_TOKEN_MEDIATYPE = MediaType.parse("application/vnd.avaya.csa.tokens.v1+json");

}
