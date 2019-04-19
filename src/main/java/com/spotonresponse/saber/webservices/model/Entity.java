package com.spotonresponse.saber.webservices.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.spotonresponse.saber.webservices.config.DynamoDBConfig;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;

import java.util.Map;
import java.util.UUID;

@DynamoDBTable(tableName = "SaberData")
public class Entity {

    @Id
    private EntityKey key;

    @DynamoDBAttribute(attributeName = "item")
    private Map<String, AttributeValue> item;

    private Entity(EntityKey key, Map<String, AttributeValue> item) {
        this.key = key;
        this.item = item;
    }

    public Entity(String title, String md5hash, Map<String, AttributeValue> item) {
        this(new EntityKey(title, md5hash), item);
    }

    public Entity() {
    }

    @DynamoDBHashKey(attributeName = "title")
    public String getTitle() {
        return (key != null) ? key.getTitle() : null;
    }

    public void setTitle(String title) {
        if (key == null) {
            key = new EntityKey();
        }
        key.setTitle(title);
    }

    @DynamoDBRangeKey(attributeName = "md5hash")
    public String getMd5hash() {
        return (key != null) ? key.getMd5hash() : null;
    }

    public void setMd5hash(String md5hash) {
        if (key == null) {
            key = new EntityKey();
        }
        key.setMd5hash(md5hash);
    }

    public Map<String, AttributeValue> getItem() {
        return item;
    }

    public void setItem(Map<String, AttributeValue> item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return "Entity[" +
                "key=" + key +
                ", item='" + item + '\'' +
                ']';
    }

    public JSONObject getEntityJson() {
        Map<String, AttributeValue> entityItem = this.item;
        Item i = ItemUtils.toItem(entityItem);
        JSONObject itemJO = new JSONObject(i.toJSON());

        JSONObject jo = new JSONObject();
        jo.put("title", this.getTitle());
        jo.put("md5hash", this.getMd5hash());
        jo.put("item", itemJO);

        return jo;
    }
}
