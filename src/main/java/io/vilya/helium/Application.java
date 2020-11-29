/**
 * 
 */
package io.vilya.helium;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

/**
 * @author zhukuanxin <cafedada@vilya.io>
 *
 */
public class Application extends AbstractVerticle{

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    
    private static final String RESPONSE_KEY = "response";
    
    @Override
    public void start() throws Exception {
        Router tRouter = Router.router(vertx);
        tRouter.get().handler(context -> {
            context.put(RESPONSE_KEY, "3213");
            context.next();
        });
        
        doStart(getVertx(), router -> {
            router.mountSubRouter("/t", tRouter);
        });
    }

    private static void doStart(Vertx vertx, Consumer<Router> consumer) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();

        Router rootRouter = Router.router(vertx);
        rootRouter.routeWithRegex("[\\/a-zA-Z]+.html").handler(createTemplateHandler(vertx));
        rootRouter.routeWithRegex("[\\/\\w]+.json").last().handler(createJSONHandler(gson));
        consumer.accept(rootRouter);

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(rootRouter);
        httpServer.listen(8080);
    }
    
    private static TemplateHandler createTemplateHandler(Vertx vertx) {
        ThymeleafTemplateEngine thymeleafTemplateEngine = ThymeleafTemplateEngine.create(vertx);
        thymeleafTemplateEngine.getThymeleafTemplateEngine().setCacheManager(null);
        return TemplateHandler.create(thymeleafTemplateEngine, TemplateHandler.DEFAULT_TEMPLATE_DIRECTORY, "text/html; charset=utf-8");
    }

    private static Handler<RoutingContext> createJSONHandler(Gson gson) {
        return context -> {
            Object response = context.get(RESPONSE_KEY);
            if (response == null) {
                response = Response.failed("No data.");
            } else if (!(response instanceof Response)) {
                response = Response.succeeded(response);
            }

            HttpServerResponse httpResponse = context.response();
            httpResponse.putHeader("content-type", "application/json; charset=utf-8");
            httpResponse.end(gson.toJson(response), "utf-8");
        };
    }
    
    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new Application());
    }

}
