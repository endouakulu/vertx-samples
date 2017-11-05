package com.endouakulu.microservice.currency;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;


public class CurrencyMicroservice extends AbstractVerticle {

    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=utf-8";
    private static Logger logger = LoggerFactory.getLogger(CurrencyMicroservice.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception{
        super.start(startFuture);
        System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());

        HttpServer server = vertx.createHttpServer();
        WebClient client = WebClient.create(vertx);
        final Router settingRestApiRouter = Router.router(vertx);
        settingRestApiRouter.route().handler(BodyHandler.create());

        settingRestApiRouter.get("/currency/rates").handler(routingContext -> {
            client.get(80,"api.fixer.io","/latest")
                    .addQueryParam("symbols","USD")
                    .send(ar ->{
                        if(ar.succeeded()) {
                            HttpResponse<Buffer> response = ar.result();
                            logger.info(response.bodyAsString());
                            routingContext.response()
                                    .setStatusCode(200)
                                    .putHeader("content-type", APPLICATION_JSON_CHARSET_UTF_8)
                                    .end(Json.encodePrettily(response.bodyAsJsonObject()));
                        }
                    });
        });
        server.requestHandler(settingRestApiRouter::accept).listen(config().getInteger("http.port", 9999));

    }
}
