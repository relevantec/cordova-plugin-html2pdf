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

import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 * @author BCL Technologies
 */
public final class TokenInfo implements Serializable {
    private final String accessToken;
    private final String refreshToken;
    private final Timestamp expiration;
    private final String[] scope;
    
    public TokenInfo(String accessToken, Timestamp expiration) {
        this(accessToken, expiration, "epc.api".split(" "));
    }

    public TokenInfo(String accessToken, Timestamp expiration, String[] scope) {
        this.accessToken = accessToken;
        this.refreshToken = null;
        this.expiration = expiration;
        this.scope = scope.clone();
    }
    
    public String getAccessToken() {
        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public Timestamp getExpiration() {
        return this.expiration;
    }

    public String[] getScope() {
        return this.scope;
    }
}
