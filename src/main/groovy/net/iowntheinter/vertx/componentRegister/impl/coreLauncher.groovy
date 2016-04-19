package net.iowntheinter.vertx.componentRegister.impl;
import groovy.json.JsonSlurper
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.vertx.componentRegister.component.impl.DockerTask
import net.iowntheinter.vertx.componentRegister.tracker.impl.gremlinSystemTracker
import net.iowntheinter.vertx.coreLauncher.impl.waitingLaunchStrategy
import net.iowntheinter.vertx.componentRegister.component.impl.VXVerticle
import io.vertx.core.logging.Log4jLogDelegateFactory
public class coreLauncher extends AbstractVerticle {

    def ct
    JsonObject config;
    Map launchTasks
    def logger = LoggerFactory.getLogger(this.class.getName())

    public void final_shutdown(String topic, String value) {
        vertx.close()
    }

    public void start_manager() {
        ct = new gremlinSystemTracker();
    }


    @Override
    public void start() throws Exception {
        launchTasks = [:]
        logger.info(vertx)
        start_manager()
        JsonObject dps = new JsonObject()
        this.config = vertx.getOrCreateContext().config()
        logger.info("reached CoreLauncher inside vert.x, cofig: ${config}")



        config.getJsonObject('startup').getJsonObject('vx').getMap().each { k, v ->
            logger.info("${k}:${v}")
            def name = k
            def nv = new VXVerticle(vertx, new DeploymentOptions([config: config]), k)
            def nt = new waitingLaunchStrategy(nv, new JsonObject(v as String).getJsonArray('deps').getList())
            nt.start({ result ->
                String id = (result as Future).result()

                logger.info("Started ${id}")
                if (vertx.sharedData().getLocalMap('deployments').get(v)) {
                    dps = vertx.sharedData().getLocalMap('deployments').get(v) as JsonObject
                }

                dps.put(id, [name: "verticle"])
                vertx.sharedData().getLocalMap('deployments').put(v, dps)
                logger.info(vertx.sharedData().getLocalMap('deployments').get(v))
            })
        }

        config.getJsonObject('startup').getJsonObject('ext').getJsonObject('docker').getMap().each { ctr,  cfg ->
            logger.info "ctr: ${ctr} cfg: ${cfg}"
            cfg = cfg as JsonObject
            logger.info "\n total config ${config}\n"
            def cfname = new JsonObject(cfg as String).getString('dkrOptsRef')
            logger.info "cfname ${cfname}"
            Map ctrcfg = (new JsonSlurper().parseText(
                    config.getJsonObject('optionBlocks').getJsonObject(cfname).toString())) as Map

            logger.info "ctrcfg ${ctrcfg}"
            def nd = new DockerTask([name:ctr,tag:'latest',image:cfg.getString('image')],ctrcfg)
            def nt = new waitingLaunchStrategy(nd, new JsonObject(cfg as String).getJsonArray('deps').getList())
            nt.start({ result ->
                logger.info "docker result ${result}"
            })
        }


    }
}
