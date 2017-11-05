package com.endouakulu.microservice.currency;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Convenience method so you can run it in your IDE
     */
    public static void main(String[] args) {
        System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new CurrencyMicroservice(), event -> {
            if (event.succeeded()) {
                logger.debug("Your Vert.x application is started!");
            } else {
                logger.error("Unable to start your application", event.cause());
            }
        });
    }
}
