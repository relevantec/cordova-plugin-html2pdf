/*
 * The MIT License
 *
 * Copyright 2016 BCL Technologies.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.easypdfcloud;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author BCL Technologies
 */
public class OAuth2HttpConnectionManager {
    // HTTP client
    private final HttpConnectionManager connectionManager;
    
     // Client ID
    private final String clientId;
    // Client secret
    private final String clientSecret;   
    // Token manager
    private final OAuth2TokenManager tokenManager;
    // URL info
    private final UrlInfo urlInfo;
    
    public OAuth2HttpConnectionManager(HttpConnectionManager connectionManager, String clientId, String clientSecret, OAuth2TokenManager tokenManager, UrlInfo urlInfo) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.connectionManager = connectionManager;
        this.tokenManager = tokenManager;
        this.urlInfo = urlInfo;
    }
    
    // Token API endoint
    private String getOAuth2TokenEndPoint() {
        return this.urlInfo.getOAuth2BaseUrl() + "/token";
    }

    private String getNewAccessToken() throws IOException {
        String url = getOAuth2TokenEndPoint();
        HttpURLConnection connection = this.connectionManager.getConnection(url);
        
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String postData =
                    "client_id=" + URLEncoder.encode(this.clientId, StandardCharsets.UTF_8.name()) +
                    "&client_secret=" + URLEncoder.encode(this.clientSecret, StandardCharsets.UTF_8.name()) +
                    "&grant_type=" + URLEncoder.encode("client_credentials", StandardCharsets.UTF_8.name()) +
                    "&scope=" + URLEncoder.encode("epc.api", StandardCharsets.UTF_8.name());

            connection.setDoOutput(true);

            try (OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream())) {
                osw.write(postData);
                osw.flush();
            }

            JSONObject jsonObject = HttpResponseUtils.getJsonResponse(connection, true);

            String accessToken = null;
            try {
                accessToken = jsonObject.getString("access_token");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String tokenType = null;
            try {
                tokenType = jsonObject.getString("token_type");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            long expiresIn = 0;
            try {
                expiresIn = jsonObject.getLong("expires_in");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String scope = null;
            try {
                scope = jsonObject.getString("scope");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!tokenType.equalsIgnoreCase("bearer")) {
                return null;
            }

            if (expiresIn > 120) {
                // we'll try to refresh a bit earlier
                expiresIn -= 60;
            }

            long nowMillis = System.currentTimeMillis();
            Timestamp now = new Timestamp(nowMillis);
            Timestamp expiration = new Timestamp(now.getTime() + (expiresIn * 1000L));
            TokenInfo tokenInfo = new TokenInfo(accessToken, expiration, scope.split(" "));
            this.tokenManager.saveTokenInfo(tokenInfo);

            return tokenInfo.getAccessToken();
        } finally {
            if (connection != null) {
                this.connectionManager.closeConnection(connection);
            }
        }
    }
    
    private String getAccessToken() throws IOException {
        TokenInfo tokenInfo = this.tokenManager.loadTokenInfo();
        if (tokenInfo == null) {
            return getNewAccessToken();
        }
        
        String accessToken = tokenInfo.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            return getNewAccessToken();
        }
        
        Timestamp expiration = tokenInfo.getExpiration();
        long expirationTime = expiration.getTime();
        long currentTime = (new Date()).getTime();
        if (currentTime > expirationTime) {
            return getNewAccessToken();
        }
        
        return accessToken;
    }
    
    public HttpURLConnection getConnection(String url) throws IOException {
        String accessToken = getAccessToken();
        
        HttpURLConnection connection = this.connectionManager.getConnection(url);
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);

        return connection;
    }
    
    public void closeConnection(HttpURLConnection connection) {
        this.connectionManager.closeConnection(connection);
    }
}
