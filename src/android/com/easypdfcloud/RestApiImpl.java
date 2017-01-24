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
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author BCL Technologies
 */
public class RestApiImpl implements Closeable {
    // OAuth2 manager
    private final OAuth2HttpConnectionManager oauth2Manager;    
    // URL info
    private final UrlInfo urlInfo;
    
    public RestApiImpl(String clientId, String clientSecret, OAuth2TokenManager tokenManager, HttpConnectionManager connectionManager, UrlInfo urlInfo) {
        this.urlInfo = urlInfo;

        this.oauth2Manager = new OAuth2HttpConnectionManager(connectionManager, clientId, clientSecret, tokenManager, urlInfo);
    }

    @Override
    public void close() throws IOException {
    }

    // Workflows API endoint
    private String getWorkflowsEndPoint() {
        return this.urlInfo.getApiBaseUrl() + "/workflows";
    }
    
    // Jobs API endoint
    private String getJobsEndPoint() {
        return this.urlInfo.getApiBaseUrl() + "/jobs";
    }
    
    public List<WorkflowInfo> getWorkflowInfoList() throws IOException {
        String url = getWorkflowsEndPoint();

        HttpURLConnection connection = this.oauth2Manager.getConnection(url);

        try {
            connection.setRequestMethod("GET");
            connection.connect();

            JSONObject jsonObject = HttpResponseUtils.getJsonResponse(connection, true);

            List<WorkflowInfo> workflowInfoList = new ArrayList<>();

            JSONArray workflowsArray = null;
            try {
                workflowsArray = jsonObject.getJSONArray("workflows");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < workflowsArray.length(); ++i) {
                JSONObject workflowObject = null;
                try {
                    workflowObject = workflowsArray.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String workflowId = workflowObject.optString("workflowID", "");
                String workflowName = workflowObject.optString("workflowName", "");
                boolean monitorFolder = workflowObject.optBoolean("monitorFolder", false);
                boolean createdByUser = workflowObject.optBoolean("createdByUser", false);

                WorkflowInfo workflowInfo = new WorkflowInfo(workflowId, workflowName, monitorFolder, createdByUser);
                workflowInfoList.add(workflowInfo);
            }

            return workflowInfoList;
        } finally {
            if (connection != null) {
                this.oauth2Manager.closeConnection(connection);
            }
        }
    }
    
    public WorkflowInfo getWorkflowInfo(String workflowId) throws IOException {
        String url = getWorkflowsEndPoint() + "/" + workflowId;

        HttpURLConnection connection = this.oauth2Manager.getConnection(url);

        try {
            connection.setRequestMethod("GET");
            connection.connect();

            JSONObject jsonObject = HttpResponseUtils.getJsonResponse(connection, true);

            String workflowName = jsonObject.optString("workflowName", "");
            boolean monitorFolder = jsonObject.optBoolean("monitorFolder", false);
            boolean createdByUser = jsonObject.optBoolean("createdByUser", false);

            return new WorkflowInfo(workflowId, workflowName, monitorFolder, createdByUser);
        } finally {
            if (connection != null) {
                this.oauth2Manager.closeConnection(connection);
            }
        }
    }
    
    public String createNewJob(String workflowId, String filePath, boolean start, boolean test) throws IOException {
        File file = new File(filePath);
        String fileName = file.getName();
        
        try (FileInputStream fileStream = new FileInputStream(filePath)) {
            return createNewJob(workflowId, fileStream, fileName, start, test);
        }
    }

    public String createNewJob(String workflowId, String filePath, String fileName, boolean start, boolean test) throws IOException {
        try (FileInputStream fileStream = new FileInputStream(filePath)) {
            return createNewJob(workflowId, fileStream, fileName, start, test);
        }
    }

    public String createNewJob(String workflowId, InputStream fileStream, String fileName, boolean start, boolean test) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(fileStream)) {
            return createNewJobWithBufferedInputStream(workflowId, bis, fileName, start, test);
        }
    }
    
    private String createNewJobWithBufferedInputStream(String workflowId, BufferedInputStream bis, String fileName, boolean start, boolean test) throws IOException {
        String url = getWorkflowsEndPoint() + "/" + workflowId + "/jobs";
        url += "?file=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        url += "&start=" + (start ? "true" : "false");
        url += "&test=" + (test ? "true" : "false");

        HttpURLConnection connection = this.oauth2Manager.getConnection(url);

        try {
            connection.setRequestMethod("PUT");

            String contentType = URLConnection.guessContentTypeFromName(fileName);
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            connection.setRequestProperty("Content-Type", contentType);

            connection.setDoOutput(true);

            try (BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream())) {
                byte[] buffer = new byte[16384];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
            }

            JSONObject jsonObject = HttpResponseUtils.getJsonResponse(connection, true);

            String jobId = jsonObject.optString("jobID", "");
            return jobId;
        } finally {
            if (connection != null) {
                this.oauth2Manager.closeConnection(connection);
            }
        }
    }
    
    public void uploadInput(String jobId, String filePath) throws IOException {
        File file = new File(filePath);
        String fileName = file.getName();
        
        try (FileInputStream fileStream = new FileInputStream(filePath)) {
            uploadInput(jobId, fileStream, fileName);
        }
    }

    public void uploadInput(String jobId, String filePath, String fileName) throws IOException {
        try (FileInputStream fileStream = new FileInputStream(filePath)) {
            uploadInput(jobId, fileStream, fileName);
        }
    }
    
    public void uploadInput(String jobId, InputStream fileStream, String fileName) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(fileStream)) {
            uploadInputWithBufferedInputStream(jobId, bis, fileName);
        }
    }
    
    private void uploadInputWithBufferedInputStream(String jobId, BufferedInputStream bis, String fileName) throws IOException {
        String url = getJobsEndPoint() + "/" + jobId + "/input";
        url += "/" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());

        HttpURLConnection connection = this.oauth2Manager.getConnection(url);

        try {
            connection.setRequestMethod("PUT");

            String contentType = URLConnection.guessContentTypeFromName(fileName);
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            connection.setRequestProperty("Content-Type", contentType);

            connection.setDoOutput(true);

            try (BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream())) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
            }

            int responseCode = connection.getResponseCode();
            boolean isSuccessfulResponseCode = (responseCode < 400);
            if (!isSuccessfulResponseCode) {
                // This should raise an exception
               HttpResponseUtils.getJsonResponse(connection, false);
            }
        } finally {
            if (connection != null) {
                this.oauth2Manager.closeConnection(connection);
            }
        }
    }
    
    public FileMetadata getOutputInfo(String jobId, String fileName) throws IOException {
        String url = getJobsEndPoint() + "/" + jobId + "/output";
        if (fileName != null && !fileName.isEmpty()) {
            url += "/" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        }
        url += "?type=metadata";
        
        HttpURLConnection connection = this.oauth2Manager.getConnection(url);

        try {
            connection.setRequestMethod("GET");
            connection.connect();

            JSONObject jsonObject = HttpResponseUtils.getJsonResponse(connection, true);
            return getFileInfoFromJsonResponse(jsonObject);
        } finally {
            if (connection != null) {
                this.oauth2Manager.closeConnection(connection);
            }
        }
    }
    
    private FileMetadata getFileInfoFromJsonResponse(JSONObject jsonObject) {
        boolean isFolder = jsonObject.optBoolean("isFolder", false);
        String name = jsonObject.optString("name", "");
        int bytes = jsonObject.optInt("bytes", 0);
        String mime = jsonObject.optString("mime", "application/octet-stream");
        String modifiedDateString = jsonObject.optString("modifiedDate", "");

        Date modifiedDate = getDateFromIso8601DateString(modifiedDateString);

        ArrayList<FileMetadata> contents = null;
        
        if (isFolder) {
            contents = new ArrayList<>();
            
            JSONArray contentsJson = jsonObject.optJSONArray("contents");
            if (contentsJson != null) {
                for (int i = 0; i < contentsJson.length(); ++i) {
                    JSONObject contentJson = null;
                    try {
                        contentJson = contentsJson.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    boolean isFolderInner = contentJson.optBoolean("isFolder", false);
                    String nameInner = contentJson.optString("name", "");
                    int bytesInner = contentJson.optInt("bytes", 0);
                    String mimeInner = contentJson.optString("mime", "application/octet-stream");
                    String modifiedDateStringInner = contentJson.optString("modifiedDate", "");

                    Date modifiedDateInner = getDateFromIso8601DateString(modifiedDateStringInner);
                                        
                    FileMetadata metadataInner = new FileMetadata(
                            isFolderInner,
                            nameInner,
                            bytesInner,
                            mimeInner,
                            modifiedDateInner,
                            null
                    );
                    
                    contents.add(metadataInner);
                }
            }
        }
        
        return new FileMetadata(
                isFolder,
                name,
                bytes,
                mime,
                modifiedDate,
                contents
        );
    }
    
    private Date getDateFromIso8601DateString(String dateString) {
        if (dateString != null && !dateString.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
                return sdf.parse(dateString);
            } catch (ParseException e) {
                LogUtils.log(RestApiImpl.class.getName(), Level.INFO, null, e);
            }
        }
        
        return new Date(Long.MIN_VALUE);
    }
    
    public FileData downloadOutput(String jobId, String fileName) throws IOException {
        String url = getJobsEndPoint() + "/" + jobId + "/output";
        if (fileName != null && !fileName.isEmpty()) {
            url += "/" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        }
        url += "?type=file";
        
        HttpURLConnection connection = this.oauth2Manager.getConnection(url);

        try {
            connection.setRequestMethod("GET");
            connection.connect();
            
            int responseCode = connection.getResponseCode();
            boolean isSuccessfulResponseCode = (responseCode < 400);
            if (!isSuccessfulResponseCode) {
                // This should raise an exception
               HttpResponseUtils.getJsonResponse(connection, false);
               return null;
            }
            
            String contentType = connection.getContentType();
            InputStream inputStream = connection.getInputStream();
            byte[] fileBytes;

            try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
                try (ByteArrayOutputStream bao = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[16384];
                    int bytesRead;        
                    while((bytesRead = bis.read(buffer)) != -1) {
                        bao.write(buffer, 0, bytesRead);
                    }

                    fileBytes = bao.toByteArray();
                }
            }
            
            String outputFileName = fileName;
            if (outputFileName == null || outputFileName.isEmpty()) {
                outputFileName = HttpResponseUtils.getFileNameFromContentDispositionResponse(connection);

                if (outputFileName == null || outputFileName.isEmpty()) {
                    outputFileName = "output";
                }
            }
            
            int contentLength = connection.getContentLength();
            if (contentLength == 0) {
                contentLength = fileBytes.length;
            }
            
            if (contentLength != fileBytes.length) {
                throw new EasyPdfCloudApiException(0, "Failed to download all bytes!");
            }

            ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
            return new FileData(outputFileName, bai, contentLength, contentType);
        } finally {
            if (connection != null) {
                this.oauth2Manager.closeConnection(connection);
            }
        }
    }
    
    public JobInfo getJobInfo(String jobId) throws IOException {
        String url = getJobsEndPoint() + "/" + jobId;
        
        HttpURLConnection connection = this.oauth2Manager.getConnection(url);

        try {
            connection.setRequestMethod("GET");
            connection.connect();

            JSONObject jsonObject = HttpResponseUtils.getJsonResponse(connection, true);
            return getJobInfoFromJsonResponse(jsonObject);
        } finally {
            if (connection != null) {
                this.oauth2Manager.closeConnection(connection);
            }
        }
    }
    
    public JobInfo getJobInfoFromJsonResponse(JSONObject jsonObject) {
        String jobId = jsonObject.optString("jobID", "");
        String workflowId = jsonObject.optString("workflowID", "");
        boolean finished = jsonObject.optBoolean("finished", false);
        int progress = jsonObject.optInt("progress", 0);

        String statusString = jsonObject.optString("status", "unknown");
        JobInfo.JobStatus status = getJobInfoStatusFromString(statusString);
        
        JobInfoDetail jobInfoDetail = null;

        JSONObject detailObject = jsonObject.optJSONObject("detail");        
        if (detailObject != null) {
            CreditsInfo apiCredits = null;
            CreditsInfo ocrCredits = null;
            ArrayList<JobError> jobErrors = null;
            
            JSONObject apiCreditsObject = detailObject.optJSONObject("apiCredits");
            if (apiCreditsObject != null) {
                int creditsRemaining = apiCreditsObject.optInt("creditsRemaining", 0);
                boolean notEnoughCredits = apiCreditsObject.optBoolean("notEnoughCredits", false);
                apiCredits = new CreditsInfo(creditsRemaining, notEnoughCredits);
            }
            
            JSONObject ocrCreditsObject = detailObject.optJSONObject("ocrCredits");
            if (ocrCreditsObject != null) {
                int creditsRemaining = ocrCreditsObject.optInt("creditsRemaining", 0);
                boolean notEnoughCredits = ocrCreditsObject.optBoolean("notEnoughCredits", false);
                ocrCredits = new CreditsInfo(creditsRemaining, notEnoughCredits);
            }
            
            JSONArray errorsObject = detailObject.optJSONArray("errors");
            if (errorsObject != null) {
                jobErrors = new ArrayList<>();
                
                for (int i = 0; i < errorsObject.length(); ++i) {
                    JSONObject errorObject = null;
                    try {
                        errorObject = errorsObject.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String taskName = errorObject.optString("taskName", "");
                    String fileName = errorObject.optString("fileName", "");
                    String message = errorObject.optString("message", "");
                    String detail = errorObject.optString("detail", "");
                    String extraDetail = errorObject.optString("extraDetail", "");
                    
                    JobError jobError = new JobError(
                            taskName,
                            fileName,
                            message,
                            detail,
                            extraDetail
                    );
                    
                    jobErrors.add(jobError);
                }
            }

            jobInfoDetail = new JobInfoDetail(
                    apiCredits,
                    ocrCredits,
                    jobErrors
            );
        }
        
        return new JobInfo(
                jobId,
                workflowId,
                finished,
                status,
                progress,
                jobInfoDetail
        );
    }
    
    private JobInfo.JobStatus getJobInfoStatusFromString(String statusString) {
        if (statusString != null && !statusString.isEmpty()) {
            switch (statusString) {
                case "waiting":
                    return JobInfo.JobStatus.Waiting;
                case "completed":
                    return JobInfo.JobStatus.Completed;
                case "failed":
                    return JobInfo.JobStatus.Failed;
                case "cancelled":
                    return JobInfo.JobStatus.Cancelled;
            }
        }

        return JobInfo.JobStatus.Unknown;
    }

    public void startJob(String jobId) throws IOException {
        startOrStopJob(jobId, true);
    }

    public void stopJob(String jobId) throws IOException {
        startOrStopJob(jobId, false);
    }

    private void startOrStopJob(String jobId, boolean start) throws IOException {
        String url = getJobsEndPoint() + "/" + jobId;
        
        HttpURLConnection connection = this.oauth2Manager.getConnection(url);

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String postData = "operation=" + URLEncoder.encode((start ? "start" : "stop"), StandardCharsets.UTF_8.name());

            connection.setDoOutput(true);

            try (OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream())) {
                osw.write(postData);
                osw.flush();
            }

            int responseCode = connection.getResponseCode();
            boolean isSuccessfulResponseCode = (responseCode < 400);
            if (!isSuccessfulResponseCode) {
                // This should raise an exception
               HttpResponseUtils.getJsonResponse(connection, false);
            }
        } finally {
            if (connection != null) {
                this.oauth2Manager.closeConnection(connection);
            }
        }
    }
    
    public void deleteJob(String jobId) throws IOException {
        String url = getJobsEndPoint() + "/" + jobId;
        
        HttpURLConnection connection = this.oauth2Manager.getConnection(url);

        try {
            connection.setRequestMethod("DELETE");
            connection.connect();

            int responseCode = connection.getResponseCode();
            boolean isSuccessfulResponseCode = (responseCode < 400);
            if (!isSuccessfulResponseCode) {
                // This should raise an exception
               HttpResponseUtils.getJsonResponse(connection, false);
            }
        } finally {
            if (connection != null) {
                this.oauth2Manager.closeConnection(connection);
            }
        }
    }

    public JobInfo waitForJobEvent(String jobId) throws IOException {
        String url = getJobsEndPoint() + "/" + jobId + "/event";
        
        HttpURLConnection connection = this.oauth2Manager.getConnection(url);

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setDoOutput(true);

            try (OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream())) {
                osw.write("");
                osw.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 202) {
                return new JobInfo(jobId, "", false, JobInfo.JobStatus.Waiting, 0, null);
            }

            JSONObject jsonObject = HttpResponseUtils.getJsonResponse(connection, true);
            return getJobInfoFromJsonResponse(jsonObject);
        } finally {
            if (connection != null) {
                this.oauth2Manager.closeConnection(connection);
            }
        }
    }
}
