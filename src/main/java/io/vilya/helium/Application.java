/**
 * 
 */
package io.vilya.helium;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.file.impl.FileResolver;
import io.vertx.ext.web.common.WebEnvironment;
import io.vilya.helium.verticle.WebVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhukuanxin <cafedada@vilya.io>
 *
 */
public class Application extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        System.setProperty(WebEnvironment.SYSTEM_PROPERTY_NAME, "dev");
        System.setProperty(FileResolver.DISABLE_FILE_CACHING_PROP_NAME, "true");
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(WebVerticle::new, new DeploymentOptions().setInstances(8));
    }

}
