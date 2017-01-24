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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.logging.Level;

/**
 *
 * @author BCL Technologies
 */
public class Client implements Closeable {
    private final RestApi restApi;
    
    public Client(String clientId, String clientSecret) {
        this.restApi = new RestApi(clientId, clientSecret);
    }

    public Client(String clientId, String clientSecret, OAuth2TokenManager tokenManager) {
        this.restApi = new RestApi(clientId, clientSecret, tokenManager);
    }

    public Client(String clientId, String clientSecret, HttpConnectionManager connectionManager) {
        this.restApi = new RestApi(clientId, clientSecret, connectionManager);
    }

    public Client(String clientId, String clientSecret, UrlInfo urlInfo) {
        this.restApi = new RestApi(clientId, clientSecret, urlInfo);
    }

    public Client(String clientId, String clientSecret, OAuth2TokenManager tokenManager, HttpConnectionManager connectionManager) {
        this.restApi = new RestApi(clientId, clientSecret, tokenManager, connectionManager);
    }

    public Client(String clientId, String clientSecret, OAuth2TokenManager tokenManager, UrlInfo urlInfo) {
        this.restApi = new RestApi(clientId, clientSecret, tokenManager, urlInfo);
    }

    public Client(String clientId, String clientSecret, HttpConnectionManager connectionManager, UrlInfo urlInfo) {
        this.restApi = new RestApi(clientId, clientSecret, connectionManager, urlInfo);
    }

    public Client(String clientId, String clientSecret, OAuth2TokenManager tokenManager, HttpConnectionManager connectionManager, UrlInfo urlInfo) {
        this.restApi = new RestApi(clientId, clientSecret, tokenManager, connectionManager, urlInfo);
    }

    @Override
    public void close() throws IOException {
        this.restApi.close();
    }

    public Job startNewJob(String workflowId, String filePath) throws IOException {
        return startNewJob(workflowId, filePath, false);
    }

    public Job startNewJob(String workflowId, String filePath, boolean enableTestMode) throws IOException {
        String jobId = this.restApi.createNewJob(workflowId, filePath, true, enableTestMode);
        return new Job(this.restApi, jobId);
    }

    public Job startNewJob(String workflowId, InputStream fileStream, String fileName) throws IOException {
        return startNewJob(workflowId, fileStream, fileName, false);
    }

    public Job startNewJob(String workflowId, InputStream fileStream, String fileName, boolean enableTestMode) throws IOException {
        String jobId = this.restApi.createNewJob(workflowId, fileStream, fileName, true, enableTestMode);
        return new Job(this.restApi, jobId);
    }

    public Job startNewJobForMergeTask(String workflowId, String[] filePaths) throws IOException {
        return startNewJobForMergeTask(workflowId, filePaths, false);
    }

    public Job startNewJobForMergeTask(String workflowId, String[] filePaths, boolean enableTestMode) throws IOException {
        int filesCount = filePaths.length;

        if (filesCount == 0) {
            throw new IllegalArgumentException("No input files specified");
        }
        
        String filePath = filePaths[0];

        if (filesCount == 1) {
            return this.startNewJob(workflowId, filePath, enableTestMode);
        }
        
        File file = new File(filePath);
        String fileName = file.getName();

        HashSet<String> fileNameMap = new HashSet<>();
        fileNameMap.add(fileName);
        
        String jobId = this.restApi.createNewJob(workflowId, filePath, fileName, false, enableTestMode);
        
        try {
            for (int i = 1; i < filesCount; ++i) {
                filePath = filePaths[i];
                file = new File(filePath);
                fileName = file.getName();
                
                int lastDotIndex = fileName.lastIndexOf(".");

                String fName;
                String fExt;

                if (lastDotIndex == -1) {
                    // file extension not round
                    fName = fileName;
                    fExt = "";
                } else if (lastDotIndex == 0) {
                    /// only file extension is found
                    fName = "";
                    fExt = fileName;
                } else {
                    // file extension is found
                    fName = fileName.substring(0, lastDotIndex - 1);
                    fExt = fileName.substring(lastDotIndex);
                }

                int fNameIndex = 0;
                while (fileNameMap.contains(fileName)) {
                    ++fNameIndex;
                    fileName = fName + " (" + fNameIndex + ")" + fExt;
                }

                fileNameMap.add(fileName);

                this.restApi.uploadInput(jobId, filePath, fileName);
            }

            this.restApi.startJob(jobId);
        } catch (Exception e) {
            try {
                this.restApi.deleteJob(jobId);
            } catch (Exception eInner) {
                LogUtils.log(Client.class.getName(), Level.INFO, null, e);
            }

            throw e;
        }

        return new Job(this.restApi, jobId);
    }
}
