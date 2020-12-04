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
import io.netty.handler.codec.http.HttpStatusClass;
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
public class Application extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final String DATA_KEY_RESPONSE = "response";

    private static final String DATA_KEY_ERROR_REROUTED = "error_rerouted";

    @Override
    public void start() throws Exception {
        Router tRouter = Router.router(vertx);
        tRouter.getWithRegex("/\\d+.html").handler(new PostRequestHandler());

        doStart(getVertx(), router -> {
            router.mountSubRouter("/post", tRouter);
        });
    }

    private static void doStart(Vertx vertx, Consumer<Router> consumer) {
        Router rootRouter = Router.router(vertx);
        rootRouter.routeWithRegex("[\\/\\w]+.[js|css]").handler(createStaticHandler());
        rootRouter.routeWithRegex("[\\/a-zA-Z]+.html")
                .last()
                .handler(createTemplateHandler(vertx));
        rootRouter.routeWithRegex("[\\/\\w]+.json").last().handler(createJSONHandler());
        rootRouter.errorHandler(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), createInternalServerErrorHandler());
        rootRouter.errorHandler(HttpResponseStatus.NOT_FOUND.code(), createNotFoundHandler());

        consumer.accept(rootRouter);

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(rootRouter);
        httpServer.listen(8080);
    }

    private static Handler<RoutingContext> createInternalServerErrorHandler() {
        return context -> {
            if (context.failed()) {
                log.error("INTERNAL_SERVER_ERROR", context.failure());
                if (WebEnvironment.development()) {
                    context.put("stackTrace", ThrowableUtil.stackTraceToString(context.failure()));
                }
            }

            context.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            context.put("message", "SERVICE UNAVAILABLE.");
            context.reroute("/error.html");
        };
    }
    
    private static Handler<RoutingContext> createNotFoundHandler() {
        return context -> {
            context.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
            context.put("message", "PAGE NOT FOUND.");
            context.reroute("/error.html");
        };
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

            // context.reroute("/error.html");
        };
    }

    private static Handler<RoutingContext> createJSONHandler() {
        return context -> {
            Object response = context.get(DATA_KEY_RESPONSE);
            if (response == null) {
                response = Response.failed("No data.");
            } else if (!(response instanceof Response)) {
                response = Response.succeeded(response);
            }
            context.json(response);
        };
    }

    public static void main(String[] args) {
        System.setProperty(WebEnvironment.SYSTEM_PROPERTY_NAME, "dev");
        Vertx.vertx().deployVerticle(new Application());
    }

}
