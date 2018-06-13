import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.DeliveryOptions

@Slf4j
class WorkerVerticle extends AbstractVerticle{


    @Override
    void start(){
        def workerExecutor = vertx.createSharedWorkerExecutor("worker", 1)
        log.info("started worker")
        vertx.eventBus().consumer('worker.add', {msg->
            def sendTimeStamp = msg.body() as Long
            def now = System.currentTimeMillis()
            log.info("worker.add received after {}", now - sendTimeStamp)

            workerExecutor.executeBlocking({future->
                def thread = Thread.currentThread()
                try {
                    log.info("thread: {}, alive: {}, msg:", thread, thread.isAlive(), msg.body())
                    def timerId = vertx.setTimer(5 * 1000, { tid ->
                        log.info("Alarm fired msg: {} thread: {}", msg.body(), Thread.currentThread().getId())
                        thread.interrupt()
                    })
                    log.info("Timer {} set for check timeout", timerId)

                    log.info("workerExecutor blocking start for msg: {} thread: {}", msg.body(), Thread.currentThread().getId())
                    Thread.sleep(60 * 1000)
                    log.info("Timer {} canceled", timerId)
                    vertx.cancelTimer(timerId)
                    log.info("workerExecutor wakeup after {}", System.currentTimeMillis() - now)
                    future.complete()
                }
                catch(InterruptedException e){
                    log.error("workerExecutor exception",e)
                    thread.isInterrupted()
                    log.info("thread: {}, alive: {}", thread, thread.isAlive())
                    future.fail(e)
                }
            }, false, {res->
                if(res.succeeded()){
                    log.info("workerExecutor res: {}", res.result())
                }else{
                    log.error("workerExecutor Failed", res.cause())
                }
            })

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
