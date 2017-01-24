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
import java.util.logging.Level;

/**
 *
 * @author BCL Technologies
 */
public class Job implements Closeable {
    private RestApi restApi;
    private String jobId;
    
    public Job(RestApi restApi, String jobId) {
        this.restApi = restApi;
        this.jobId = jobId;
    }

    @Override
    public void close() throws IOException {
        try {
            if (this.restApi != null && (this.jobId != null && !this.jobId.isEmpty())) {
                try {
                    restApi.deleteJob(this.jobId);
                } catch (Exception e) {
                    LogUtils.log(Job.class.getName(), Level.INFO, null, e);
                }
            }
        } finally {
            this.restApi = null;
            this.jobId = null;
        }
    }
    
    public String getJobId() {
        return this.jobId;
    }
    
    public JobExecutionResult waitForJobExecutionCompletion() throws IOException {
        JobInfo jobInfo;

        while (true) {
            jobInfo = this.restApi.waitForJobEvent(this.jobId);
            if (jobInfo.getFinished()) {
                break;
            }
        }

        JobInfo.JobStatus status = jobInfo.getStatus();
        if (status != JobInfo.JobStatus.Completed) {
            throw new JobExecutionException(jobInfo);
        }

        FileData fileData = this.restApi.downloadOutput(jobId);
        
        return new JobExecutionResult(jobInfo, fileData);
    }
    
    public void cancelJobExecution() throws IOException {
        this.restApi.stopJob(this.jobId);
    }
}
