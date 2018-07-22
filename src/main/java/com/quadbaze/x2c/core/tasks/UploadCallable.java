package com.quadbaze.x2c.core.tasks;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.quadbaze.x2c.core.Converter;
import com.quadbaze.x2c.common.Konstants;
import com.quadbaze.x2c.core.streams.UploadableInputStream;
import tech.blueglacier.email.Attachment;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Olatunji O. Longe on 05/05/2018 6:51 PM
 */
public class UploadCallable implements Callable<Boolean> {

    private AmazonS3 s3Client;
    private List<Attachment> attachments;
    private String s3Key;
    private String destinationBucketName;

    public UploadCallable(AmazonS3 s3Client, List<Attachment> attachments, String s3Key, String destinationBucketName){
        this.s3Client = s3Client;
        this.attachments = attachments;
        this.s3Key = s3Key;
        this.destinationBucketName = destinationBucketName;
    }

    @Override
    public Boolean call() throws Exception {
        Boolean uploaded = false;
        for(Attachment attachment : attachments){
            if(attachment.getBd().getMimeType().equals(Konstants.EXCEL_VENDOR_MIME)){
                String outputFileName = s3Key+Konstants.CSV_EXTENSION;
                uploaded = uploadCsv(Converter.convertToCsv(attachment.getIs()), outputFileName, destinationBucketName);
            }
        }
        return uploaded;
    }

    /**
     * Uploads CSV to s3Bucket naming the object using the supplied key
     * @param csvInputStream
     * @param key
     * @return
     * @throws IOException
     */
    private boolean uploadCsv(UploadableInputStream csvInputStream, String key, String destinationBucket) throws IOException {
        try{
            if(s3Client != null){
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(Konstants.CSV_MIME);
                metadata.setContentLength(csvInputStream.size());
                s3Client.putObject(new PutObjectRequest(destinationBucket, key, csvInputStream, metadata));
            }
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
        }finally{
            closeInputStream(csvInputStream);
        }
        return false;
    }

    /**
     * Safely closes the inputStream
     * @param inputStream
     */
    private void closeInputStream(InputStream inputStream){
        try{
            if(inputStream != null){
                inputStream.close();
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

}
