package at.modalog.cordova.plugin.html2pdf;

import com.easypdfcloud.ApiAuthorizationException;
import com.easypdfcloud.Client;
import com.easypdfcloud.CreditsInfo;
import com.easypdfcloud.EasyPdfCloudApiException;
import com.easypdfcloud.FileData;
import com.easypdfcloud.Job;
import com.easypdfcloud.JobExecutionException;
import com.easypdfcloud.JobExecutionResult;
import com.easypdfcloud.JobInfo;
import com.easypdfcloud.JobInfoDetail;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.lukhnos.nnio.file.Files;
import org.lukhnos.nnio.file.Path;
import org.lukhnos.nnio.file.Paths;
import org.lukhnos.nnio.file.StandardCopyOption;
import java.text.NumberFormat;

public class Program {
    //////////////////////////////////////////////////////////////////////
    //
    // !!! VERY IMPORTANT !!!
    //
    // You must configure these variables before running this sample code.
    //
    //////////////////////////////////////////////////////////////////////

    // Client ID. You can get your client ID from the developer page
    // (for registered user, you must sign in before visiting this page)
    // https://www.easypdfcloud.com/developer
    //
    // To test the sample code quickly for demo purpose, you can use
    // the following demo client ID:
    //
    //   05ee808265f24c66b2b8e31d90c31ab1
    //
    static final String clientId = "6e67ed44c557434b9265a036b06fa1e5";

    // Client secret. You can get your client secret from the developer page
    // (for registered user, you must sign in before visiting this page)
    // https://www.easypdfcloud.com/developer
    //
    // To test the sample code quickly for demo purpose, you can use
    // the following demo client secret:
    //
    //   16ABE87E052059F147BB2A491944BF7EA7876D0F843DED105CBA094C887CBC99
    //
    static final String clientSecret = "C472413C38938DAF0B44BB68529E34D712A93236AE491082494C1C49FDC81890";

    // Workflow ID. You can get your workflow ID from the developer page
    // (for registered user, you must sign in before visiting this page)
    // https://www.easypdfcloud.com/developer
    //
    // To test the sample code quickly for demo purpose, you can use
    // one of the following demo workflow IDs:
    //
    //  00000000048585C1 : "Convert Files to PDF" workflow
    //  00000000048585C2 : "Convert PDF to Word" workflow
    //  00000000048585C3 : "Combine Files to PDF" workflow
    //
    static final String workflowId = "0000000004E09A1C";

    // A flag indicating if the specified workflow contains the "Combine PDFs" task
    //
    // true:  The specified workflow contains the "Combine PDFs" task
    // false: The specified workflow does not contain the "Combine PDFs" task
    //
    static boolean workflowIncludesCombinePdfsTask = false;

    // A flag to specify whether to enable test mode for job execution.
    //
    // true:  Enable test mode (do not use API credits)
    // false: Do not enable test mode (use API credits)
    //
    // If true is specified, then the job is executed as a test mode and
    // your API credit will not be used.
    //
    static final boolean enableTestMode = false;
    
    // Full path of the input file which you want to upload and process
    // This value is used only if the workflowIncludesCombinePdfsTask == false
    //
    // To test the sample code quickly for demo purpose, you can use
    // one of the following files bundled with this sample:
    //
    // samples/input/sample.docx : For converting Word to PDF
    // samples/input/sample.pdf  : For converting PDF to Word
    //
    static String inputFilePath = "samples/input/sample.docx";

    // List of full path of the input file which you want to upload and process
    // This value is used only if the workflowIncludesCombinePdfsTask == true
    //
    // To test the sample code quickly for demo purpose, you can use
    // the following files bundled with this sample:
    //
    // samples/input/sample.docx : A sample Word file
    // samples/input/sample.pdf  : A sample PDF file
    //
    static String[] inputFilePaths =
    {
        "samples/input/sample.docx",
        "samples/input/sample.pdf",
    };

    // Output directory used for saving output file
    static String outputDir = "samples/output";
    
    //////////////////////////////////////////////////////////////////////

    static boolean isNullOrEmpty(String string) {
        return (string == null || string.isEmpty());
    }

    //////////////////////////////////////////////////////////////////////
    
    static void checkParameters() throws IOException {
        System.out.println("--------------------------------------------------------");
        System.out.println("App parameters:");
        System.out.println();
        System.out.println("clientId: " + Program.clientId);
        System.out.println("clientSecret: " + (isNullOrEmpty(Program.clientSecret) ? "" : "********"));
        System.out.println("workflowId: " + Program.workflowId);
        System.out.println("enableTestMode: " + (Program.enableTestMode ? "true" : "false"));
            
        if (!workflowIncludesCombinePdfsTask) {
            System.out.println("inputFilePath: " + Program.inputFilePath);
        } else {
            for (String filePath : Program.inputFilePaths) {
                System.out.println("    " + filePath);
            }
        }
        System.out.println("outputDir: " + Program.outputDir);
        System.out.println("--------------------------------------------------------");
        System.out.println();

        if (isNullOrEmpty(Program.clientId)) {
            throw new IllegalArgumentException("clientId is not specified in the code!");
        }

        if (isNullOrEmpty(Program.clientSecret)) {
            throw new IllegalArgumentException("clientSecret is not specified in the code!");
        }

        if (!workflowIncludesCombinePdfsTask) {
            if (isNullOrEmpty(Program.inputFilePath)) {
                throw new IllegalArgumentException("inputFilePath is not specified in the code!");
            }

            File inputFile = new File(Program.inputFilePath);
            if (!inputFile.exists()) {
                throw new FileNotFoundException(Program.inputFilePath + " does not exist!");
            }
        } else {
            if (Program.inputFilePaths.length == 0) {
                throw new IllegalArgumentException("inputFilePaths is not specified in the code!");
            }

            for (String filePath : Program.inputFilePaths) {
                File file = new File(filePath);
                if (!file.exists()) {
                    throw new FileNotFoundException(filePath + " does not exist!");
                }
            }
        }

        if (isNullOrEmpty(Program.outputDir)) {
            throw new IllegalArgumentException("outputDir is not specified in the code!");
        }

        File outputDirectory = new File(Program.outputDir);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
    }

    //////////////////////////////////////////////////////////////////////

    static void writeException(String message, Exception e) {
        System.out.println();
        System.out.println(message);
        System.out.println("Message: " + e.getMessage());
        System.out.println();
        System.out.println("Exception: " + e.getClass().getName());
        System.out.println();
        System.out.println("Stack trace:");
        e.printStackTrace(System.out);
        System.out.println();
    }

    //////////////////////////////////////////////////////////////////////

    static void checkCreditsInfo(JobInfo jobInfo) {
        // Check API/OCR credits

        JobInfoDetail jobInfoDetail = jobInfo.getDetail();
        
        if (jobInfoDetail != null)
        {
            CreditsInfo apiCredits = jobInfoDetail.getApiCredits();
            CreditsInfo ocrCredits = jobInfoDetail.getOcrCredits();
            
            if ((apiCredits != null) || (ocrCredits != null))
            {
                System.out.println();

                if (apiCredits != null)
                {
                    // Check API credits

                    System.out.println("API credits remaining: " + apiCredits.getCreditsRemaining());

                    if (apiCredits.getNotEnoughCredits())
                    {
                        System.out.println("Not enough API credits!");
                    }

                    System.out.println();
                }

                if (ocrCredits != null)
                {
                    // Check OCR credits

                    System.out.println("OCR credits remaining: " + ocrCredits.getCreditsRemaining());

                    if (ocrCredits.getNotEnoughCredits())
                    {
                        System.out.println("Not enough OCR credits!");
                    }

                    System.out.println();
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////

    static String saveOutput(FileData fileData, String outputDir) throws IOException {
        String outputFileName = fileData.getName();
        int outputFileSize = fileData.getBytes();
        Path outputFilePath = Paths.get(outputDir, outputFileName);

        System.out.println("Job execution completed");
        System.out.println("Output file name: " + outputFileName);
        System.out.println("Output file size: " + NumberFormat.getIntegerInstance().format(outputFileSize) + " bytes");

        // Save output to file
        System.out.println("Saving to output directory...");
        if (outputFilePath.toFile().exists()) {
            outputFilePath.toFile().delete();
        }
        Files.copy(fileData.getStream(), outputFilePath/*, StandardCopyOption.REPLACE_EXISTING*/);

        return outputFilePath.toAbsolutePath().toString();
    }

    //////////////////////////////////////////////////////////////////////

    static String executeNewJob(String clientId, String clientSecret, String workflowId, boolean enableTestMode, String inputFilePath, String outputDir) throws IOException {
        // Create easyPDF Cloud client object
        try (Client client = new com.easypdfcloud.Client(clientId, clientSecret)) {
            // Upload input file and start new job
            try (Job job = client.startNewJob(workflowId, inputFilePath, enableTestMode)) {
                System.out.println("New job started (job ID: " + job.getJobId() +  ")");
                System.out.println("Waiting for job execution completion...");

                // Wait until job execution is completed
                try (JobExecutionResult jobExecutionResult = job.waitForJobExecutionCompletion()) {
                    // Check API/OCR credits info
                    checkCreditsInfo(jobExecutionResult.getJobInfo());

                    // Save output to file
                    String outputFilePath = saveOutput(jobExecutionResult.getFileData(), outputDir);

                    // Return output file path
                    return outputFilePath;
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////
    
    static String executeNewJobForMergeTask(String clientId, String clientSecret, String workflowId, boolean enableTestMode, String[] inputFilePaths, String outputDir) throws IOException {
        // Create easyPDF Cloud client object
        try (Client client = new com.easypdfcloud.Client(clientId, clientSecret)) {
            // Upload input file and start new job
            try (Job job = client.startNewJobForMergeTask(workflowId, inputFilePaths, enableTestMode)) {
                System.out.println("New job started (job ID: " + job.getJobId() +  ")");
                System.out.println("Waiting for job execution completion...");

                // Wait until job execution is completed
                try (JobExecutionResult jobExecutionResult = job.waitForJobExecutionCompletion()) {
                    // Check API/OCR credits info
                    checkCreditsInfo(jobExecutionResult.getJobInfo());
                    
                    // Save output to file
                    String outputFilePath = saveOutput(jobExecutionResult.getFileData(), outputDir);

                    // Return output file path
                    return outputFilePath;
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////
    
    public static String FilesToPdf(String[] args) throws IOException {
        String outputFilePath = null;
        System.out.println("Working directory: " + System.getProperty("user.dir"));
        System.out.println();
 
        try {
            Program.checkParameters();
        } catch (Exception e) {
            Program.writeException("Parameter check failed!", e);
            return null;
        }

        try {
            System.out.println("Executing new job...");

            if (!Program.workflowIncludesCombinePdfsTask) {
                // The workflow does not include the "Combine PDFs" task

                // Start job execution
                outputFilePath = Program.executeNewJob(
                        Program.clientId,
                        Program.clientSecret,
                        Program.workflowId,
                        Program.enableTestMode,
                        Program.inputFilePath,
                        Program.outputDir
                );

                System.out.println("Output saved to: " + outputFilePath);
            } else {
                // The workflow includes the "Combine PDFs" task

                // Start job execution
                outputFilePath = Program.executeNewJobForMergeTask(
                        Program.clientId,
                        Program.clientSecret,
                        Program.workflowId,
                        Program.enableTestMode,
                        Program.inputFilePaths,
                        Program.outputDir
                );

                System.out.println("Output saved to: " + outputFilePath);
            }
        } catch (EasyPdfCloudApiException e) {
            Program.writeException("API execution failed!", e);
        } catch (JobExecutionException e) {
            Program.writeException("Job execution failed!", e);
            // Check API/OCR credits info
            checkCreditsInfo(e.getJobInfo());
        } catch (ApiAuthorizationException e) {
            Program.writeException("API Authorization failed!", e);
        } catch (Exception e) {
            Program.writeException("Uncaught exception!", e);
        }

        System.out.println("Done");
        return outputFilePath;
    }
}
