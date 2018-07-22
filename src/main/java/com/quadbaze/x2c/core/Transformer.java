package com.quadbaze.x2c.core;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.quadbaze.x2c.common.Container;
import com.quadbaze.x2c.common.Konstants;
import com.quadbaze.x2c.common.Download;
import com.quadbaze.x2c.core.tasks.DownloadCallable;
import com.quadbaze.x2c.core.tasks.UploadCallable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Olatunji O. Longe on 04/05/2018 4:58 PM
 */
public class Transformer {

    private final AmazonS3 s3Client;

    public Transformer(){
        this.s3Client = getS3Client();
    }

    /**
     * For this to connect to s3, ensure that a valid AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY
     * are appropriately set up in the system environment variable of the host machine
     * @return
     */
    private final AmazonS3 getS3Client(){
        return AmazonS3ClientBuilder.standard()
                .withRegion(Konstants.CLIENT_REGION)
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .build();
    }

    /**
     * Loads all email objects from the s3 sourceBucket, extracts all excel attachment(s),
     * then converts them to csv(s), and uploads the csv(s) to the destinationBucket
     * @param sourceContainer
     * @param destinationContainer
     * @return
     */
    public boolean extractExcelAndTransformToCsv(Container sourceContainer, Container destinationContainer) {
        if(s3Client != null){
            ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(sourceContainer.bucketName());
            ListObjectsV2Result result = s3Client.listObjectsV2(request);
            List<S3ObjectSummary> summaries = result.getObjectSummaries();

            ExecutorService executorService = Executors.newFixedThreadPool(summaries.size());
            List<FutureTask<Download>> downloadTasks = new ArrayList<>();
            for(S3ObjectSummary summary : summaries){
                FutureTask<Download> downloadTask = new FutureTask<>(new DownloadCallable(s3Client, sourceContainer.bucketName(), summary.getKey()));
                downloadTasks.add(downloadTask);
                executorService.submit(downloadTask);

                //limit the number of objects to convert to 30 for now
                if(summaries.indexOf(summary) > 29){
                    break;
                }
            }

            try{
                List<FutureTask<Boolean>> uploadTasks = new ArrayList<>();
                for(FutureTask<Download> downloadTask : downloadTasks){
                    Download download = downloadTask.get();
                    if(download != null){
                        FutureTask<Boolean> uploadTask = new FutureTask<>(new UploadCallable(s3Client, download.getAttachments(), download.getKey(), destinationContainer.bucketName()));
                        uploadTasks.add(uploadTask);
                        executorService.submit(uploadTask);
                    }
                }

                List<Boolean> uploads = new ArrayList<>();
                for(FutureTask<Boolean> uploadTask : uploadTasks){
                    uploads.add(uploadTask.get());
                }
                return !uploads.contains(false);
            }catch(Exception ex){
                ex.printStackTrace();
            }finally{
                executorService.shutdown();
                System.out.println("All transformations Completed!");
            }
        }
        return false;
    }

}
