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
public class ApiAuthorizationException extends RuntimeException {
    private final int statusCode;
    private final String error;
    private final String errorDescription;
    
    public ApiAuthorizationException(int statusCode, String error, String errorDescription)
    {
        super(ApiAuthorizationException.toString(statusCode, error, errorDescription));

        this.statusCode = statusCode;
        this.error = error;
        this.errorDescription = errorDescription;
    }
    
    private static String toString(int statusCode, String error, String errorDescription) {
        StringBuilder stringBuilder = new StringBuilder();

        if (errorDescription != null && !errorDescription.isEmpty()) {
            stringBuilder.append(errorDescription);
        } else {
            stringBuilder.append(error);
        }

        stringBuilder.append(" (HTTP status code: ").append(statusCode).append(")");

        return stringBuilder.toString();
    }
    
    public int getStatusCode() {
        return this.statusCode;
    }
    
    public String getError() {
        return this.error;
    }
    
    public String getErrorDescription() {
        return this.errorDescription;
    }
}
