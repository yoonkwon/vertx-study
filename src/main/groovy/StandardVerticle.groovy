import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle

@Slf4j
class StandardVerticle extends AbstractVerticle {

    private SEND_PERIOD = 2 * 1000

    @Override
    void start() throws Exception {
        log.info "starting StandardVerticle"

        vertx.eventBus().send('worker.add', System.currentTimeMillis()+"")
        vertx.setTimer(6000, {
            vertx.eventBus().send('worker.add', ""+System.currentTimeMillis())
        })
//        startPeriodicSend(SEND_PERIOD)
        vertx.eventBus().consumer("standard.callback", {msg->
            def timestamp = msg.body() as Long
            def now = System.currentTimeMillis()
            log.info("standard.callback received after {}", now - timestamp)
            Thread.sleep(3000)
            log.info("standard.callback wakeup", now - timestamp)
        })

    }

    void startPeriodicSend(int sendPeriod){
        vertx.setPeriodic(sendPeriod, {timerId->
            def sendTimestamp = System.currentTimeMillis()
            log.info("main.period start send {}", sendTimestamp)
            vertx.eventBus().send('worker.add',sendTimestamp+"")
            log.info("main.period end send after {}", System.currentTimeMillis() - sendTimestamp)
        })
    }



    @Override
    void stop(){
        log.info("stop main")
    }

}
