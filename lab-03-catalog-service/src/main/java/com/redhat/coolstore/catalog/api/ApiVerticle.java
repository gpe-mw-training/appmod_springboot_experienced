package com.redhat.coolstore.catalog.api;

import java.util.List;

import com.redhat.coolstore.catalog.model.Product;
import com.redhat.coolstore.catalog.verticle.service.CatalogService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class ApiVerticle extends AbstractVerticle {

    private CatalogService catalogService;

    public ApiVerticle(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        Router router = Router.router(vertx);
        router.get("/products").handler(this::getProducts);
        router.get("/product/:itemId").handler(this::getProduct);
        router.route("/product").handler(BodyHandler.create());
        router.post("/product").handler(this::addProduct);

        // Health Checks
        router.get("/health/readiness").handler(rc -> rc.response().end("OK"));
        router.get("/health/liveness").handler(rc -> rc.response().end("OK"));
        
        // Static content for swagger docs
        router.route().handler(StaticHandler.create());
        
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getInteger("catalog.http.port", 8080));
    }

    private void getProducts(RoutingContext rc) {
        catalogService.getProducts(ar -> {
            if (ar.succeeded()) {
                List<Product> products = ar.result();
                JsonArray json = new JsonArray();
                products.stream()
                    .map(p -> p.toJson())
                    .forEach(p -> json.add(p));
                rc.response()
                    .putHeader("Content-type", "application/json")
                    .end(json.encodePrettily());
            } else {
                rc.fail(ar.cause());
            }
        });
    }

    private void getProduct(RoutingContext rc) {
        String itemId = rc.request().getParam("itemid");
        catalogService.getProduct(itemId, ar -> {
            if (ar.succeeded()) {
                Product product = ar.result();
                JsonObject json;
                if (product != null) {
                    json = product.toJson();
                    rc.response()
                        .putHeader("Content-type", "application/json")
                        .end(json.encodePrettily());
                } else {
                    rc.fail(404);
                }
            } else {
                rc.fail(ar.cause());
            }
        });
    }

    private void addProduct(RoutingContext rc) {
        JsonObject json = rc.getBodyAsJson();
        catalogService.addProduct(new Product(json), ar -> {
            if (ar.succeeded()) {
                rc.response().setStatusCode(201).end();
            } else {
                rc.fail(ar.cause());
            }
        });
    }
    
}
