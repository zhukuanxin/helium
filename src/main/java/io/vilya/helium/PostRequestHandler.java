/**
 * 
 */
package io.vilya.helium;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author zhukuanxin <cafedada@vilya.io>
 * @created 2020-12-04 07:44:21
 */
public class PostRequestHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext context) {
        context.put("user", "123");
        context.reroute("/post/index.html");
    }

}
