package com.spotonresponse.saber.webservices.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;

import java.io.Serializable;

public class EntityKey implements Serializable {
    private String title;
    private String md5hash;

    public EntityKey() {

    }

    public EntityKey(String title, String md5hash) {
        this.title = title;
        this.md5hash = md5hash;
    }

    @DynamoDBHashKey
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @DynamoDBRangeKey
    public String getMd5hash() {
        return md5hash;
    }

    public void setMd5hash(String md5hash) {
        this.md5hash = md5hash;
    }

    @Override
    public String toString() {
        return "EntityKey[" +
                "title='" + title + '\'' +
                ", md5hash='" + md5hash + '\'' +
                ']';
    }

}
