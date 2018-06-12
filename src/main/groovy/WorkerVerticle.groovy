import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.DeliveryOptions

@Slf4j
class WorkerVerticle extends AbstractVerticle{


    @Override
    void start(){
        def workerExecutor = vertx.createSharedWorkerExecutor("worker", 10, 1500)
        log.info("started worker")
        vertx.eventBus().consumer('worker.add', {msg->
            def sendTimeStamp = msg.body() as Long
            def now = System.currentTimeMillis()
            log.info("worker.add received after {}", now - sendTimeStamp)

            workerExecutor.executeBlocking({future->
                log.info("workerExecutor blocking start for msg: {} thread: {}", msg.body(), Thread.currentThread().getId())
                future.complete()
                log.info("worker.add wakeup after {}", System.currentTimeMillis()-now)
            }, false, {res->
                if(res.succeeded()){
                    log.info("workerExecutor res: {}", res.result())
                }else{
                    log.error("workerExecutor Failed", res.cause())
                }
            })

//            workerExecutor.close()
//            vertx.cancelTimer(tid)

//            sendCallback(System.currentTimeMillis())
//            log.info("worker.add sent after {}", System.currentTimeMillis()-now)
        })
    }

    void sendCallback(long timestamp){
        vertx.eventBus().send("stanadard.callback", System.currentTimeMillis().toString(), new DeliveryOptions().setSendTimeout(1000), {res->
            if(res.succeeded()){
                log.info("worker.send after {}", System.currentTimeMillis() - timestamp)
            }else{
                log.error("worker.send after {}", System.currentTimeMillis() - timestamp, res.cause())
            }
        })
    }

    @Override
    void stop(){
        log.info("stop worker")
    }
}
