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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.Level;

/**
 *
 * @author BCL Technologies
 */
public class HttpConnectionManager {
    private static final HttpConnectionManager sharedInstance = new HttpConnectionManager();

    private final Proxy proxy;
    
    public HttpConnectionManager() {
        this(Proxy.NO_PROXY);
    }

    public HttpConnectionManager(Proxy proxy) {
        this.proxy = proxy;
    }

    public static HttpConnectionManager getSharedHttpConnection() {
        return HttpConnectionManager.sharedInstance;
    }
    
    protected void configureConnection(HttpURLConnection connection) {
    }
    
    public HttpURLConnection getConnection(String url) throws IOException {
        URL urlObject = new URL(url);
        HttpURLConnection connection = (HttpURLConnection)urlObject.openConnection(this.proxy);

        connection.setConnectTimeout(80 * 1000);
        connection.setReadTimeout(80 * 1000);
        connection.setUseCaches(false);
        connection.setAllowUserInteraction(false);
        connection.setInstanceFollowRedirects(true);
        
        configureConnection(connection);
        
        return connection;        
    }

    public void closeConnection(HttpURLConnection connection) {
        try {
            InputStream inputStream;
            
            int responseCode = connection.getResponseCode();
            boolean isSuccessfulResponseCode = (responseCode < 400);
            if (isSuccessfulResponseCode) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }
            
            if (inputStream != null) {
                try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
                    byte[] buffer = new byte[16384];
                    while (bis.read(buffer) != -1) {}
                }
            }
        } catch (IOException e) {
            LogUtils.log(HttpConnectionManager.class.getName(), Level.INFO, null, e);
        }
    }
}
