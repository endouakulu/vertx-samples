package com.endouakulu.vertx.microservice.setting.domain;

import com.endouakulu.vertx.microservice.setting.Audit;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.UUID;

public class Setting implements Audit {

    private UUID uuid;
    private Instant create;
    private String type;
    private String category;
    private String entityId;
    private String code;
    private String value;
    private String updatedBy;
    private String createdBy;
    private Instant dateCreated;
    private Instant lastUpdated;

    public Setting(){

    }

    public Setting(JsonObject obj) {
        uuid = UUID.fromString(obj.getString("UUID"));
        value = obj.getString("VAALUE");
        code = obj.getString("CODE");
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Instant getCreate() {
        return create;
    }

    public void setCreate(Instant create) {
        this.create = create;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
