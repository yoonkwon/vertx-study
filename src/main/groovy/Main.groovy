import groovy.util.logging.Slf4j
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject

@Slf4j
class Main {
    private static final MAX_WORKER_EXEC_TIME_SEC = 1
    static void main(String[] args){
        def config = new JsonObject()
        def options = new VertxOptions()
            .setClustered(true)
            .setClusterHost(getDefaultAddress())
            .setClusterPort(0)
        def deployOptions = new DeploymentOptions()
            .setConfig(config)
            .setInstances(1)
            .setWorker(false)
        def deployWorkerOption = new DeploymentOptions()
            .setConfig(config)
            .setInstances(1)
            .setWorker(true)
            .setMaxWorkerExecuteTime(MAX_WORKER_EXEC_TIME_SEC * 1000 * 1000)
        Vertx.clusteredVertx(options, { res->
            if(res.succeeded()){
                def vertx = res.result()
                log.info("vetx cluster created")
                vertx.deployVerticle(StandardVerticle.class, deployOptions, { deployRes->
                    if(deployRes.failed()){
                        log.error("Failed to deploy StandardVerticle", deployRes.cause())
                    }
                    else{
                        log.info("deploy Main done")
                    }
                })
                vertx.deployVerticle(WorkerVerticle.class, deployWorkerOption, {deployRes->
                    if(deployRes.failed()){
                        log.error("Failed to deploy WorkerVerticle", deployRes.cause())
                    }else{
                        log.info("deploy Worker done")
                    }
                })
            }
        })

    }
    private static String getDefaultAddress() {
        Enumeration<NetworkInterface> nets
        try {
            nets = NetworkInterface.getNetworkInterfaces()
        } catch (SocketException e) {
            return null
        }
        NetworkInterface netinf
        while (nets.hasMoreElements()) {
            netinf = nets.nextElement()

            Enumeration<InetAddress> addresses = netinf.getInetAddresses()

            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement()
                if (!address.isAnyLocalAddress() && !address.isMulticastAddress() && !(address instanceof Inet6Address)) {
                    return address.getHostAddress()
                }
            }
        }
        return null
    }
}
