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

import java.util.List;

/**
 *
 * @author BCL Technologies
 */
public class JobExecutionException extends RuntimeException {
    private final JobInfo jobInfo;
    
    public JobExecutionException(JobInfo jobInfo) {
        super(JobExecutionException.toString(jobInfo));
        this.jobInfo = jobInfo;
    }
    
    private static String toString(JobInfo jobInfo) {
        StringBuilder stringBuilder = new StringBuilder();

        JobError jobError = null;

        JobInfoDetail jobInfoDetail = jobInfo.getDetail();
        if (jobInfoDetail != null) {
            List<JobError> jobErrors = jobInfoDetail.getErrors();
            if (jobErrors != null && !jobErrors.isEmpty()) {
                jobError = jobErrors.get(0);
            }
        }
        
        if (jobError != null) {
            String message = jobError.getMessage();
            if (message != null && !message.isEmpty()) {
                String detail = jobError.getDetail();
                if (detail != null && !detail.isEmpty()) {
                    stringBuilder.append(detail);

                    String extraDetail = jobError.getExtraDetail();
                    if (extraDetail != null && !extraDetail.isEmpty()) {
                        stringBuilder.append(" (").append(extraDetail).append(")");
                    }
                } else {
                    stringBuilder.append(message);
                }
            } else {
                switch (jobInfo.getStatus()) {
                    case Failed:
                        stringBuilder.append("Job execution failed");
                        break;
                    case Cancelled:
                        stringBuilder.append("Job execution cancelled");
                        break;
                    default:
                        stringBuilder.append("Job execution failed with unknown error");
                        break;
                }
            }
        } else {
            stringBuilder.append("Job execution failed with unknown error");
        }

        return stringBuilder.toString();
    }

    public JobInfo getJobInfo() {
        return this.jobInfo;
    }
}
