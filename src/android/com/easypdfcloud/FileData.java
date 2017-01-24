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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author BCL Technologies
 */
public class FileData implements Closeable {
    private final String name;
    private final InputStream stream;
    private final int bytes;
    private final String contentType;
    
    public FileData(String name, InputStream stream, int bytes, String contentType) {
        this.name = name;
        this.stream = stream;
        this.bytes = bytes;
        this.contentType = contentType;
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }

    public String getName() {
        return this.name;
    }
    
    public InputStream getStream() {
        return this.stream;
    }

    public int getBytes() {
        return this.bytes;
    }
    
    public String getContentType() {
        return this.contentType;
    }
}
