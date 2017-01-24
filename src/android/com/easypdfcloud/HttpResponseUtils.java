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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author BCL Technologies
 */
public class HttpResponseUtils {
    private static String convertStreamToString(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8.name()))) {
            char[] buffer = new char[16384];
            int charsRead;
            while ((charsRead = br.read(buffer)) != -1) {
                sb.append(buffer, 0, charsRead);
            }
        }
        
        return sb.toString();
    }
    
    public static JSONObject getJsonObject(InputStream stream) throws IOException {
        String text = convertStreamToString(stream);
        if (text.isEmpty()) {
            text = "{}"; // set to empty JSON object
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    
    private static Map<String, List<String>> getCaseInsensitiveHeaderFields(HttpURLConnection connection) {
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        Map<String, List<String>> caseInsensitiveHeaderFields = new HashMap<>();
            
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isEmpty()) {
                continue;
            }

            List<String> value = entry.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }

            String keyLC = key.toLowerCase();
            caseInsensitiveHeaderFields.put(keyLC, value);
        }
        
        return caseInsensitiveHeaderFields;
    }
    
    private static Map<String, String> parseWwwAuthenticateHeaderValue(String wwwAuthenticateValue) throws IOException {
        HashMap<String, String> map = new HashMap<>();
        
        String[] spaceSeparatedArray = wwwAuthenticateValue.trim().split(" ");
        for (String spaceSeparatedItem : spaceSeparatedArray) {
            String[] commaSeparatedArray = spaceSeparatedItem.trim().split(",");
            if (commaSeparatedArray.length >= 2) {
                for (String commaSeparatedItem : commaSeparatedArray) {
                    String[] equalSeparatedArray = commaSeparatedItem.trim().split("=", 2);
                    if (equalSeparatedArray.length >= 2) {
                        String name = equalSeparatedArray[0].trim();
                        String value = equalSeparatedArray[1].trim();

                        // ltrim("\"");
                        while (value.startsWith("\"")) {
                            if (value.length() == 1) {
                                value = "";
                            } else {
                                value = value.substring(1);
                            }
                        }
                        
                        // rtrim("\"");
                        while (value.endsWith("\"")) {
                            if (value.length() == 1) {
                                value = "";
                            } else {
                                value = value.substring(0, value.length() - 1);
                            }
                        }

                        String nameLC = name.toLowerCase();
                        String valueDecoded = URLDecoder.decode(value, StandardCharsets.UTF_8.name());

                        map.put(nameLC, valueDecoded);
                    }
                }
            }
        }
        
        return map;
    }
    
    public static JSONObject getJsonResponse(HttpURLConnection connection, boolean failIfNotJsonResponse) throws IOException {
        String contentType = connection.getContentType();
        int responseCode = connection.getResponseCode();
        String responseMessage = connection.getResponseMessage();        
        boolean isSuccessfulResponseCode = (responseCode < 400);
        
        JSONObject jsonObject = null;
        
        if (contentType != null) {
            String contentTypeLC = contentType.toLowerCase();
            if (contentTypeLC.startsWith("application/json")) {
                InputStream inputStream = (isSuccessfulResponseCode ? connection.getInputStream() : connection.getErrorStream());
                jsonObject = getJsonObject(inputStream);
            }
        }
        
        if (!isSuccessfulResponseCode) {
            if (responseCode == 400 || responseCode == 401) {
                Map<String, List<String>> headerFields = getCaseInsensitiveHeaderFields(connection);
                
                if (headerFields.containsKey("www-authenticate")) {
                    List<String> wwwAuthenticate = headerFields.get("www-authenticate");
                    if (wwwAuthenticate != null && !wwwAuthenticate.isEmpty()) {
                        String wwwAuthenticateValue = wwwAuthenticate.get(0);
                        Map<String, String> wwwAuthenticateMap = parseWwwAuthenticateHeaderValue(wwwAuthenticateValue);
                        
                        if (wwwAuthenticateMap.containsKey("error")){
                            String error = wwwAuthenticateMap.get("error");
                            String errorDescription = "";
                            
                            if (wwwAuthenticateMap.containsKey("error_description")) {
                                errorDescription = wwwAuthenticateMap.get("error_description");
                            }
                            
                            throw new ApiAuthorizationException(responseCode, error, errorDescription);
                        }
                    }
                }
            }
            
            if (jsonObject != null) {
                if (jsonObject.has("error")) {
                    String error = null;
                    try {
                        error = jsonObject.getString("error");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    throw new EasyPdfCloudApiException(responseCode, responseMessage, error);
                } else if (jsonObject.has("message")) {
                    String message = null;
                    try {
                        message = jsonObject.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    throw new EasyPdfCloudApiException(responseCode, responseMessage, message);
                }
            }
            
            throw new EasyPdfCloudApiException(responseCode, responseMessage);
        } else {
            if (jsonObject == null) {
                if (failIfNotJsonResponse) {
                    // Unsupported data format for successful result (only JSON is supported)

                    throw new EasyPdfCloudApiException(0, "Unsupported response data format (only JSON is supported)");
                }
            }

            return jsonObject;
        }
    }

    private static Map<String, String> parseContentDispositionHeaderValue(String contentDispositionValue) {
        HashMap<String, String> map = new HashMap<>();
        
        String[] colonSeparatedArray = contentDispositionValue.trim().split(";");
        for (String colonSeparatedItem : colonSeparatedArray) {
            String[] equalSeparatedArray = colonSeparatedItem.trim().split("=", 2);
            if (equalSeparatedArray.length >= 2) {
                String name = equalSeparatedArray[0].trim();
                String value = equalSeparatedArray[1].trim();

                // ltrim("\"");
                while (value.startsWith("\"")) {
                    if (value.length() == 1) {
                        value = "";
                    } else {
                        value = value.substring(1);
                    }
                }

                // rtrim("\"");
                while (value.endsWith("\"")) {
                    if (value.length() == 1) {
                        value = "";
                    } else {
                        value = value.substring(0, value.length() - 1);
                    }
                }

                String nameLC = name.toLowerCase();
                map.put(nameLC, value);
            }
        }
        
        return map;
    }

    public static String getFileNameFromContentDispositionResponse(HttpURLConnection connection) throws IOException {
        Map<String, List<String>> headerFields = getCaseInsensitiveHeaderFields(connection);
        if (!headerFields.containsKey("content-disposition")) {
            return null;
        }
        
        List<String> contentDisposition = headerFields.get("content-disposition");
        if (contentDisposition == null || contentDisposition.isEmpty()) {
            return null;
        }
        
        String contentDispositionValue = contentDisposition.get(0);
        Map<String, String> contentDispositionMap = parseContentDispositionHeaderValue(contentDispositionValue);

        if (contentDispositionMap.containsKey("filename*")){
            String fileName = contentDispositionMap.get("filename*");
            String utf8Prefix = "utf-8''";

            if (fileName.indexOf(utf8Prefix) == 0) {
                int prefixLength = utf8Prefix.length();
                fileName = fileName.substring(prefixLength);
            }

            fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());
            return fileName;
        } else if (contentDispositionMap.containsKey("filename")){
            String fileName = contentDispositionMap.get("filename");
            String base64Prefix = "=?utf-8?B?";
            String encodingPostfix = "?=";

            if (fileName.indexOf(base64Prefix) == 0) {
                int prefixLength = base64Prefix.length();
                fileName = fileName.substring(prefixLength);

                int fileNameLength = fileName.length();
                int postfixLength = encodingPostfix.length();
                int postfixIndex = fileNameLength - postfixLength;
                if (fileName.lastIndexOf(encodingPostfix) == postfixIndex) {
                    fileName = fileName.substring(0, postfixIndex);
                }

                byte[] base64Decoded = Base64Decode.decode(fileName);
                fileName = new String(base64Decoded, StandardCharsets.UTF_8.name());
                return fileName;
            }

            fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());
            return fileName;
        }
        
        return null;
    }
}
