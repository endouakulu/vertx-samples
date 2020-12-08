package com.endouakulu.helloworld;

import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx(); // (1)
        vertx.deployVerticle(new MainVerticle()); // (2)
    }
}
