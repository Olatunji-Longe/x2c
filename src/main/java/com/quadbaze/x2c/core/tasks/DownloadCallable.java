package com.quadbaze.x2c.core.tasks;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.quadbaze.x2c.common.Download;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.MimeConfig;
import tech.blueglacier.email.Attachment;
import tech.blueglacier.email.Email;
import tech.blueglacier.parser.CustomContentHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Olatunji O. Longe on 05/05/2018 6:51 PM
 */
public class DownloadCallable implements Callable<Download> {

    private AmazonS3 s3Client;
    private String sourceBucketName;
    private String s3Key;
    private MimeConfig mimeConfig;

    public DownloadCallable(AmazonS3 s3Client, String sourceBucketName, String s3Key){
        this.s3Client = s3Client;
        this.sourceBucketName = sourceBucketName;
        this.s3Key = s3Key;
        this.mimeConfig = getMimeConfig();
    }

    @Override
    public Download call() {
        return new Download(s3Key, extractAttachments(s3Client.getObject(new GetObjectRequest(sourceBucketName, s3Key))));
    }

    /**
     * Mime Config used for reading the email s3Object
     * @return
     */
    private MimeConfig getMimeConfig(){
        return MimeConfig.custom()
                .setCountLineNumbers(false)
                .setHeadlessParsing(null)
                .setMalformedHeaderStartsBody(false)
                .setMaxContentLen(-1)
                .setMaxHeaderCount(1000)
                .setMaxHeaderLen(10000)
                .setMaxLineLen(100000)
                .setStrictParsing(false)
                .build();
    }

    /**
     * Extracts attachments form the email s3Object
     * @param s3Object
     * @return
     */
    public List<Attachment> extractAttachments(S3Object s3Object) {
        try{
            ContentHandler contentHandler = new CustomContentHandler();
            BodyDescriptorBuilder bodyDescriptorBuilder = new DefaultBodyDescriptorBuilder();
            MimeStreamParser mime4jParser = new MimeStreamParser(mimeConfig, DecodeMonitor.SILENT, bodyDescriptorBuilder);
            mime4jParser.setContentDecoding(true);
            mime4jParser.setContentHandler(contentHandler);
            mime4jParser.parse(s3Object.getObjectContent());
            Email email = ((CustomContentHandler) contentHandler).getEmail();
            return email.getAttachments();
        } catch (MimeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            closeS3Object(s3Object);
        }
        return null;
    }

    /**
     * Safely closes the s3Object
     * @param s3Object
     */
    private void closeS3Object(S3Object s3Object){
        try{
            if(s3Object != null){
                s3Object.close();
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

}
