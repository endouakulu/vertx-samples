package com.endouakulu.vertx.microservice.setting;

import java.time.Instant;

public interface Audit {

    public Instant getDateCreated();
    public Instant getLastUpdated();
    public String getCreatedBy();
    public String getUpdatedBy();
}
