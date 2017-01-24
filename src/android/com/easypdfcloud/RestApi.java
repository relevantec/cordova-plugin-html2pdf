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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author BCL Technologies
 */
public class RestApi implements Closeable {
    private RestApiImpl impl;

    public RestApi(String clientId, String clientSecret) {
        this(
                clientId,
                clientSecret,
                new LocalFileTokenManager(clientId),
                HttpConnectionManager.getSharedHttpConnection(),
                new UrlInfo()
        );
    }

    public RestApi(String clientId, String clientSecret, OAuth2TokenManager tokenManager) {
        this(
                clientId,
                clientSecret,
                tokenManager,
                HttpConnectionManager.getSharedHttpConnection(),
                new UrlInfo()
        );
    }

    public RestApi(String clientId, String clientSecret, HttpConnectionManager connectionManager) {
        this(
                clientId,
                clientSecret,
                new LocalFileTokenManager(clientId),
                connectionManager,
                new UrlInfo()
        );
    }

    public RestApi(String clientId, String clientSecret, UrlInfo urlInfo) {
        this(
                clientId,
                clientSecret,
                new LocalFileTokenManager(clientId),
                HttpConnectionManager.getSharedHttpConnection(),
                urlInfo
        );
    }

    public RestApi(String clientId, String clientSecret, OAuth2TokenManager tokenManager, HttpConnectionManager connectionManager) {
        this(clientId,
                clientSecret,
                tokenManager,
                connectionManager,
                new UrlInfo()
        );
    }

    public RestApi(String clientId, String clientSecret, OAuth2TokenManager tokenManager, UrlInfo urlInfo) {
        this(
                clientId,
                clientSecret,
                tokenManager,
                HttpConnectionManager.getSharedHttpConnection(),
                urlInfo
        );
    }

    public RestApi(String clientId, String clientSecret, HttpConnectionManager connectionManager, UrlInfo urlInfo) {
        this(clientId,
                clientSecret,
                new LocalFileTokenManager(clientId),
                connectionManager,
                urlInfo
        );
    }

    public RestApi(String clientId, String clientSecret, OAuth2TokenManager tokenManager, HttpConnectionManager connectionManager, UrlInfo urlInfo) {
        this.impl = new RestApiImpl(
                clientId,
                clientSecret,
                tokenManager,
                connectionManager,
                urlInfo
        );
    }

    @Override
    public void close() throws IOException {
        if (this.impl != null) {
            this.impl.close();
            this.impl = null;
        }
    }

    private boolean needToRefreshToken(ApiAuthorizationException e) {
        int statusCode = e.getStatusCode();

        if (statusCode == 400 || statusCode == 401) {
            String error = e.getError();
            String errorLC = error.toLowerCase();

            if (errorLC.compareTo("invalid_token") == 0 || errorLC.compareTo("expired_token") == 0) {
                // try again
                return true;
            }
        }
        
        return false;
    }
    
    public List<WorkflowInfo> getWorkflowInfoList() throws IOException {
        try {
            return impl.getWorkflowInfoList();
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                return impl.getWorkflowInfoList();
            }
            
            throw e;
        }
    }
    
    public WorkflowInfo getWorkflowInfo(String workflowId) throws IOException {
        try {
            return impl.getWorkflowInfo(workflowId);
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                return impl.getWorkflowInfo(workflowId);
            }
            
            throw e;
        }
    }
    
    public String createNewJob(String workflowId, String filePath, boolean start, boolean test) throws IOException {
        try {
            return impl.createNewJob(workflowId, filePath, start, test);
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                return impl.createNewJob(workflowId, filePath, start, test);
            }
            
            throw e;
        }
    }

    public String createNewJob(String workflowId, String filePath, String fileName, boolean start, boolean test) throws IOException {
        try {
            return impl.createNewJob(workflowId, filePath, fileName, start, test);
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                return impl.createNewJob(workflowId, filePath, fileName, start, test);
            }
            
            throw e;
        }
    }
    
    public String createNewJob(String workflowId, InputStream fileStream, String fileName, boolean start, boolean test) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        byte[] buffer = new byte[16384];
        int bytesRead;        
        while((bytesRead = fileStream.read(buffer)) != -1) {
            bao.write(buffer, 0, bytesRead);
        }

        byte[] fileBytes = bao.toByteArray();

        try {
            ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
            return impl.createNewJob(workflowId, bai, fileName, start, test);
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
                return impl.createNewJob(workflowId, bai, fileName, start, test);
            }
            
            throw e;
        }
    }

    public void uploadInput(String jobId, String filePath) throws IOException {
        try {
            impl.uploadInput(jobId, filePath);
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                impl.uploadInput(jobId, filePath);
            }
            
            throw e;
        }
    }
    
    public void uploadInput(String jobId, String filePath, String fileName) throws IOException {
        try {
            impl.uploadInput(jobId, filePath, fileName);
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                impl.uploadInput(jobId, filePath, fileName);
            }
            
            throw e;
        }
    }
    
    public void uploadInput(String jobId, InputStream fileStream, String fileName) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        byte[] buffer = new byte[16384];
        int bytesRead;        
        while((bytesRead = fileStream.read(buffer)) != -1) {
            bao.write(buffer, 0, bytesRead);
        }

        byte[] fileBytes = bao.toByteArray();

        try {
            ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
            impl.uploadInput(jobId, bai, fileName);
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
                impl.uploadInput(jobId, bai, fileName);
            }
            
            throw e;
        }
    }
    
    public FileMetadata getOutputInfo(String jobId) throws IOException {
        return getOutputInfo(jobId, null);
    }

    public FileMetadata getOutputInfo(String jobId, String fileName) throws IOException {
        try {
            return impl.getOutputInfo(jobId, fileName);
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                return impl.getOutputInfo(jobId, fileName);
            }

            throw e;
        }
    }
        
    public FileData downloadOutput(String jobId) throws IOException {
        return downloadOutput(jobId, null);
    }
    
    public FileData downloadOutput(String jobId, String fileName) throws IOException {
        try {
            return impl.downloadOutput(jobId, fileName);
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                return impl.downloadOutput(jobId, fileName);
            }

            throw e;
        }
    }
    
    public JobInfo getJobInfo(String jobId) throws IOException {
        try {
            return impl.getJobInfo(jobId);
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                return impl.getJobInfo(jobId);
            }

            throw e;
        }
    }
    
    public void startJob(String jobId) throws IOException {
        try {
            impl.startJob(jobId);
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                impl.startJob(jobId);
            }

            throw e;
        }
    }

    public void stopJob(String jobId) throws IOException {
        try {
            impl.stopJob(jobId);
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                impl.stopJob(jobId);
            }

            throw e;
        }
    }

    public void deleteJob(String jobId) throws IOException {
        try {
            impl.deleteJob(jobId);
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                impl.deleteJob(jobId);
            }

            throw e;
        }
    }
    
    public JobInfo waitForJobEvent(String jobId) throws IOException {
        try {
            return impl.waitForJobEvent(jobId);
        } catch (ApiAuthorizationException e) {
            LogUtils.log(RestApi.class.getName(), Level.INFO, null, e);
            if (needToRefreshToken(e)) {
                // try again
                return impl.waitForJobEvent(jobId);
            }

            throw e;
        }
    }
}
