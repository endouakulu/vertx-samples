package com.endouakulu.vertx.microservice.setting;

import com.endouakulu.vertx.microservice.setting.domain.Setting;
import com.endouakulu.vertx.microservice.setting.domain.SettingRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class SettingsMicroservice extends AbstractVerticle {


    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=utf-8";
    private static         Logger logger = LoggerFactory.getLogger(SettingsMicroservice.class);


    /**
     * Convenience method so you can run it in your IDE
     */
    public static void main(String[] args) {

        System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
        Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
                new DropwizardMetricsOptions()
                        .setEnabled(true)
                        .setJmxEnabled(true)
                        .addMonitoredHttpServerUri(new Match().setValue("/settings.*").setType(MatchType.REGEX))
        ));

        vertx.deployVerticle(new SettingsMicroservice(), event -> {
            if (event.succeeded()) {
                logger.info("Your Vert.x application is started!");
            } else {
                logger.error("Unable to start your application", event.cause());
            }
        });
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);
        MetricsService metricsService = MetricsService.create(vertx);
        HttpServer server = vertx.createHttpServer();
        JDBCClient client = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", "jdbc:hsqldb:file:settings.db")
                .put("driver_class", "org.hsqldb.jdbcDriver")
                .put("user", "SA")
                .put("password", "")
                .put("max_pool_size", 30));

        final Router settingRestApiRouter = Router.router(vertx);
        settingRestApiRouter.route().failureHandler(routingContext -> {
            routingContext.response()
                    .setStatusCode(routingContext.statusCode())
                    .putHeader("content-type", APPLICATION_JSON_CHARSET_UTF_8)
                    .end(Json.encodePrettily("Something wrong ...."));
        });

        settingRestApiRouter.route().handler(BodyHandler.create());

        settingRestApiRouter.get("/metrics/vertx").handler(routingContext -> {
            JsonObject metrics = metricsService.getMetricsSnapshot(vertx);
            routingContext.response()
                          .setStatusCode(200)
                          .putHeader("content-type", APPLICATION_JSON_CHARSET_UTF_8)
                          .end(Json.encodePrettily(metrics));
        });

        settingRestApiRouter.get("/metrics/http").handler(routingContext -> {
            JsonObject metrics = metricsService.getMetricsSnapshot(server);
            routingContext.response()
                          .setStatusCode(200)
                          .putHeader("content-type", APPLICATION_JSON_CHARSET_UTF_8)
                          .end(Json.encodePrettily(metrics));
        });

        settingRestApiRouter.get("/settings").handler(routingContext -> {
            client.getConnection(conn -> {
                if (conn.failed()) {
                    logger.error(conn.cause().getMessage());
                    throw new RuntimeException("NoSQL");
                }
                SQLConnection connection = conn.result();
                connection.query("SELECT * FROM settings", result -> {
                    if (result.succeeded()) {
                        List<Setting> settings = result.result().getRows().stream().map(Setting::new).collect(Collectors.toList());
                        routingContext.response()
                                      .setStatusCode(200)
                                      .putHeader("content-type", APPLICATION_JSON_CHARSET_UTF_8)
                                      .end(Json.encodePrettily(settings));
                    }
                    connection.close();
                });
            });
        });


        settingRestApiRouter.get("/settings/:settingId").handler(routingContext -> {
            client.getConnection(conn -> {
                if (conn.failed()) {
                    System.err.println(conn.cause().getMessage());
                    return;
                }
                String id = routingContext.request().getParam("settingId");
                JsonArray queryParams = new JsonArray().add(id);
                SQLConnection connection = conn.result();
                connection.queryWithParams("SELECT * FROM settings WHERE UUID=?", queryParams, result -> {
                if (result.result().getRows().isEmpty()) {
                    routingContext.response()
                                  .setStatusCode(404)
                                  .putHeader("content-type", APPLICATION_JSON_CHARSET_UTF_8)
                                  .end(Json.encodePrettily("Not found"));
                } else {
                    Setting setting = result.result().getRows().stream().map(Setting::new).findFirst().orElse(new Setting());
                    routingContext.response()
                                  .setStatusCode(200)
                                  .putHeader("content-type", APPLICATION_JSON_CHARSET_UTF_8)
                                  .end(Json.encodePrettily(setting));
                }
                connection.close();
                });
            });
        });

        settingRestApiRouter.delete("/settings/:settingId").handler(routingContext -> {
            client.getConnection(conn -> {
                if (conn.failed()) {
                   logger.error(conn.cause().getMessage());
                    return;
                }
                String id = routingContext.request().getParam("settingId");
                JsonArray queryParams = new JsonArray().add(id);
                SQLConnection connection = conn.result();
                connection.queryWithParams("DELETE FROM settings WHERE UUID=?", queryParams, result -> {
                    if (result.succeeded()) {
                        if (result.result().getRows().isEmpty()) {
                            routingContext.response()
                                          .setStatusCode(404)
                                          .putHeader("content-type", APPLICATION_JSON_CHARSET_UTF_8)
                                          .end(Json.encodePrettily("Not found"));
                        } else {
                            routingContext.response()
                                          .setStatusCode(204)
                                          .end();
                        }
                    } else {
                        routingContext.response()
                                      .setStatusCode(500)
                                      .putHeader("content-type", APPLICATION_JSON_CHARSET_UTF_8)
                                      .end(Json.encodePrettily("Something wrong ...."));
                    }
                    connection.close();
                });
            });
        });


        settingRestApiRouter.post("/settings").handler(routingContext -> {
            final SettingRequest setting = Json.decodeValue(routingContext.getBodyAsString(),
                    SettingRequest.class);

            JsonArray parameters = new JsonArray().add(UUID.randomUUID().toString()).add(setting.getCode()).add(setting.getValue());

            client.getConnection(conn -> {
                if (conn.failed()) {
                    System.err.println(conn.cause().getMessage());
                    return;
                }

                SQLConnection connection = conn.result();
                connection.queryWithParams("INSERT INTO settings VALUES (?,?,?)", parameters, result -> {
                    if (result.succeeded()) {
                        routingContext.response()
                                      .setStatusCode(201)
                                      .end(Json.encodePrettily(setting));
                    } else {
                        routingContext.response()
                                      .setStatusCode(500)
                                      .putHeader("content-type", APPLICATION_JSON_CHARSET_UTF_8)
                                      .end(Json.encodePrettily("Something wrong ...."));
                    }
                    connection.close();
                });
            });
        });


        settingRestApiRouter.put("/settings").handler(routingContext -> {
            final SettingRequest setting = Json.decodeValue(routingContext.getBodyAsString(),
                    SettingRequest.class);
            if (Objects.nonNull(setting.getUuid()) && setting.getUuid().isEmpty()) {
                client.getConnection(conn -> {
                    if (conn.failed()) {
                        System.err.println(conn.cause().getMessage());
                        return;
                    }

                    JsonArray parameters = new JsonArray();
                    boolean addColon = false;
                    StringBuilder query = new StringBuilder("UPDATE settings SET");
                    if (Objects.nonNull(setting.getCode())) {
                        addColon = true;
                        query.append(" code = ?");
                        parameters.add(setting.getCode());
                    }

                    if (Objects.nonNull(setting.getValue())) {
                        if (addColon) {
                            query.append(",");
                        }
                        query.append(" value = ?");
                        parameters.add(setting.getValue());
                    }
                    query.append(" WHERE uuid = ?");


                    SQLConnection connection = conn.result();
                    connection.queryWithParams(query.toString(), parameters, result -> {
                        if (result.succeeded()) {
                            routingContext.response()
                                          .setStatusCode(204)
                                          .end();
                        } else {
                            routingContext.response()
                                          .setStatusCode(500)
                                          .putHeader("content-type", APPLICATION_JSON_CHARSET_UTF_8)
                                          .end(Json.encodePrettily("Something wrong ...."));
                        }
                        connection.close();
                    });
                });
            } else {
                routingContext.response()
                              .setStatusCode(400)
                              .putHeader("content-type", APPLICATION_JSON_CHARSET_UTF_8)
                              .end(Json.encodePrettily("Invalid request ...."));
            }
        });
        server.requestHandler(settingRestApiRouter::accept).listen(config().getInteger("http.port", 8888));
    }
}
