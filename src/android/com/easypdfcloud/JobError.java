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
public class JobError {
    private final String taskName;
    private final String fileName;
    private final String message;
    private final String detail;
    private final String extraDetail;
    
    public JobError(String taskName, String fileName, String message, String detail, String extraDetail) {
        this.taskName = taskName;
        this.fileName = fileName;
        this.message = message;
        this.detail = detail;
        this.extraDetail = extraDetail;        
    }
    
    public String getTaskName() {
        return this.taskName;
    }
    
    public String getFileName() {
        return this.fileName;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public String getDetail() {
        return this.detail;
    }
    
    public String getExtraDetail() {
        return this.extraDetail;
    }    
}
