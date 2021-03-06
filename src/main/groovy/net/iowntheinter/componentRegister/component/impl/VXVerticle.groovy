package net.iowntheinter.componentRegister.component.impl

import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.Handler
import io.vertx.core.Vertx
import net.iowntheinter.componentRegister.component.componentType

/**
 * Created by grant on 4/10/16.
 */
class VXVerticle implements componentType {
    Vertx vertx
    String ImplementationID  // example com.this.that or js:myVerticle.js
    String name //human readable name of this instance
    String id; //launch
    DeploymentOptions ops
    Map deps = [:]
    String tasktype

    VXVerticle(Vertx vertx, DeploymentOptions opts, String ImplementationID) {
        this.vertx = vertx
        this.ImplementationID = ImplementationID
        this.ops = opts
        deps = opts.getConfig().getMap()
    }

    @Override
    String getId() {
        return this.id
    }

    @Override
    void start(cb) {
        this.name = name

        vertx.deployVerticle(ImplementationID, ops, { AsyncResult result ->
            cb(result)
        } as Handler)
    }

    @Override
    void stop(cb) {
        vertx.undeploy(id as String, cb as Handler)
    }

    @Override
    void backup(cb) {

    }

    void registrationEvent(Map peerNotification, Closure cb) {
        //send a message to the verticles personal channel
    }


}
