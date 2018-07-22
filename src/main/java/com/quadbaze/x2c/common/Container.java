package com.quadbaze.x2c.common;

/**
 * Created by Olatunji O. Longe on 06/05/2018 5:50 PM
 */
public enum Container {
    SOURCE("source-bucket-name"),
    DESTINATION("destination-bucket-name");

    private String bucketName;

    Container(String bucketName){
        this.bucketName = bucketName;
    }

    public String bucketName(){
        return this.bucketName;
    }
}
