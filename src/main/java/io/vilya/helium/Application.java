/**
 * 
 */
package io.vilya.helium;

import java.net.URL;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.internal.ThrowableUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.WebEnvironment;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

/**
 * @author zhukuanxin <cafedada@vilya.io>
 *
 */
public class Application extends AbstractVerticle{

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    
    private static final String DATA_KEY_RESPONSE = "response";
    
    private static final String DATA_KEY_ERROR_REROUTED = "error_rerouted";
    
    @Override
    public void start() throws Exception {
        Router tRouter = Router.router(vertx);
        tRouter.get().handler(context -> {
            context.put(DATA_KEY_RESPONSE, "3213");
            context.next();
        });
        
        doStart(getVertx(), router -> {
            router.mountSubRouter("/t", tRouter);
        });
    }

    private static void doStart(Vertx vertx, Consumer<Router> consumer) {
        Router rootRouter = Router.router(vertx);
        rootRouter.routeWithRegex("[\\/\\w]+.[js|css]").handler(createStaticHandler());
        rootRouter.routeWithRegex("[\\/a-zA-Z]+.html")
            .handler(createTemplateHandler(vertx))
            .failureHandler(createTemplateErrorHandler());
        rootRouter.routeWithRegex("[\\/\\w]+.json").last().handler(createJSONHandler());
        consumer.accept(rootRouter);

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(rootRouter);
        httpServer.listen(8080);
    }

    private static StaticHandler createStaticHandler() {
        ClassLoader classLoader = Application.class.getClassLoader();
        URL url = classLoader.getResource("");
        return StaticHandler.create(url.getFile());
    }
    
    private static TemplateHandler createTemplateHandler(Vertx vertx) {
        ThymeleafTemplateEngine templateEngine = ThymeleafTemplateEngine.create(vertx);
        TemplateEngine thymeleafTemplateEngine = templateEngine.getThymeleafTemplateEngine();
        thymeleafTemplateEngine.setCacheManager(null);
        return TemplateHandler.create(templateEngine, TemplateHandler.DEFAULT_TEMPLATE_DIRECTORY, "text/html; charset=utf-8");
    }

    private static Handler<RoutingContext> createTemplateErrorHandler() {
        return context -> {
            log.error("template error", context.failure());
            
            // 页面一致404
            HttpServerResponse httpResponse = context.response();
            httpResponse.setStatusCode(HttpResponseStatus.NOT_FOUND.code());
            
            if (context.get(DATA_KEY_ERROR_REROUTED) != null) {
                httpResponse.end("404");
                return;
            }
            
            context.put(DATA_KEY_ERROR_REROUTED, "");
            
            if (WebEnvironment.development()) {
                context.put("stackTrace", ThrowableUtil.stackTraceToString(context.failure()));                
            }
            
            context.reroute("/error.html");
        };
    }
    
    private static Handler<RoutingContext> createJSONHandler() {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();
        
        return context -> {
            Object response = context.get(DATA_KEY_RESPONSE);
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
        System.setProperty(WebEnvironment.SYSTEM_PROPERTY_NAME, "dev");
        Vertx.vertx().deployVerticle(new Application());
    }

}
