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

/**
 *
 * @author BCL Technologies
 */
public class UrlInfo {
    private final String oauth2BaseUrl;
    private final String apiBaseUrl;

    public UrlInfo()
    {
        this.oauth2BaseUrl = "https://www.easypdfcloud.com/oauth2";
        this.apiBaseUrl = "https://api.easypdfcloud.com/v1";
    }

    public UrlInfo(String oauth2BaseUrl, String apiBaseUrl)
    {
        this.oauth2BaseUrl = oauth2BaseUrl;
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getOAuth2BaseUrl() {
        return this.oauth2BaseUrl;
    }
    
    public String getApiBaseUrl() {
        return this.apiBaseUrl;
    }
}
