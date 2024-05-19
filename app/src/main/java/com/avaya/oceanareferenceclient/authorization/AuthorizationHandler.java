/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.authorization;

import com.avaya.oceanareferenceclient.authorization.TokenPojos.AAWGTokenRequest;
import com.avaya.oceanareferenceclient.authorization.TokenPojos.AAWGTokenResponse;
import com.avaya.oceanareferenceclient.settings.AbstractSettingsService;
import com.avaya.oceanareferenceclient.settings.pojos.AvayaPlatformPreferences;
import com.avaya.oceanareferenceclient.settings.pojos.CustomerPreferences;
import com.avaya.oceanareferenceclient.settings.pojos.TokenServicePreferences;
import com.avaya.oceanareferenceclient.settings.pojos.WebGatewayPreferences;
import com.avaya.oceanareferenceclient.utils.Constants;
import com.avaya.oceanareferenceclient.utils.HttpUtil;
import com.avaya.oceanareferenceclient.utils.Logger;
import com.avaya.ocs.Base.Rest.JsonConvertor;
import com.fasterxml.jackson.core.JsonProcessingException;


import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthorizationHandler {

    private static final String TAG = AuthorizationHandler.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    private CustomerPreferences preferences;
    private AvayaPlatformPreferences oceanaPreferences;
    private WebGatewayPreferences webGatewayPreferences;
    private TokenServicePreferences tokenServicePreferences;

    public AuthorizationHandler(AbstractSettingsService settingsService) {
        preferences = settingsService.retrieveAuthorizationPreferences();
        oceanaPreferences = settingsService.retrievePreferences();
        webGatewayPreferences = settingsService.retrieveWebGatewayPreferences();
        tokenServicePreferences = settingsService.retrieveTokenServicePreferences();
    }

    public AuthorizationResponse getToken() {
        AuthorizationResponse response = null;

        if (webGatewayPreferences.isAvailable()) {
            // Normally, this will be null and the token will be valid for any destination,
            // but if an explicit destination is set, then the token will be fetched for only that destination
            String destinationAddress = oceanaPreferences.getDestination();
            response = getAawgToken(destinationAddress, preferences.getFromAddress(), preferences.getDisplayName());
        }

        if (response == null) {
            mLogger.e("Failed to get token");
            response = new AuthorizationResponse(false);
        }

        return response;
    }

    private AuthorizationResponse getAawgToken(String destinationAddress, String fromAddress, String displayName) {
        AuthorizationResponse response = null;
        try {
            String url = buildAawgTokenUrl();

            if (url != null) {
                JsonConvertor jsonConvertor = new JsonConvertor();

                AAWGTokenRequest request = new AAWGTokenRequest(destinationAddress, fromAddress, displayName);

                String tokenBody = jsonConvertor.toJson(request);

                mLogger.i("Requesting AAWG token from " + url + " with body:\n " + tokenBody);

                Response httpResponse = doPostRequest(url, tokenBody, Constants.AAWG_TOKEN_MEDIATYPE);
                if (httpResponse != null && httpResponse.isSuccessful()) {
                    AAWGTokenResponse tokenResponse = jsonConvertor.fromJson(httpResponse.body().string(), AAWGTokenResponse.class);
                    String token = tokenResponse.getEncryptedToken();

                    mLogger.i("AAWG Authorization request successful - " + token);
                    response = new AuthorizationResponse(true, token);
                } else {
                    mLogger.e("AAWG Authorization request failed: " + httpResponse.code() + " " + httpResponse.message());
                }
            }
        } catch (JsonProcessingException e) {
            mLogger.e("Error with AAWG create token request, code: " + e.getMessage(), e);
        } catch (IOException e) {
            mLogger.e("Exception getting AAWG authorization token: " + e.getMessage(), e);
        }

        return response;
    }

    private Response doPostRequest(String url, String bodyData, MediaType mediaType) throws IOException {
        OkHttpClient client = HttpUtil.getInstance().getHttpClient();

        RequestBody body = RequestBody.create(mediaType, bodyData);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        return client.newCall(request).execute();
    }

    private String buildAawgTokenUrl() {
        String url = null;

        if (tokenServicePreferences.isAvailable()) {

            if (tokenServicePreferences.isSecure()) {
                url = Constants.AAWG_RETRIEVE_TOKEN_URL;
            } else {
                url = Constants.AAWG_RETRIEVE_TOKEN_INSECURE_URL;
            }

            url = url.replace(Constants.SERVER_PLACEHOLDER, tokenServicePreferences.getTokenServer());
            url = url.replace(Constants.PORT_PLACEHOLDER, Integer.toString(tokenServicePreferences.getTokenPort()));
            url = url.replace(Constants.AAWG_TOKEN_URL_PATH_PLACEHOLDER, tokenServicePreferences.getRestUrlPath());
        }

        mLogger.d("Token service URL: " + url);

        return url;
    }
}
