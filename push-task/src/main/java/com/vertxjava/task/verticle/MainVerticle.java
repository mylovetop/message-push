package com.vertxjava.task.verticle;

import com.vertxjava.common.verticle.HttpVerticle;
import com.vertxjava.task.api.TaskApiVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author Jack
 * @create 2017-12-18 16:47
 **/
public class MainVerticle extends HttpVerticle {

    // log
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start();
        deployTaskApiVerticle().setHandler(ar -> {
            if (ar.succeeded()) {
                logger.info("Deploy TaskApiVerticle is successful");
            } else {
                logger.error("Deploy TaskApiVerticle is failed,case:" + ar.cause());
            }
        });

    }

    private Future<String> deployTaskApiVerticle() {
        return Future.future(f -> vertx.deployVerticle(TaskApiVerticle.class.getName(),
                new DeploymentOptions().setConfig(config()), f.completer()));
    }

    @Override
    public void stop(Future<Void> future) {
        super.stop(future);
    }
}
