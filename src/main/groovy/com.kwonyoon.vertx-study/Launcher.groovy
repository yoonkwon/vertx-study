import io.vertx.core.Vertx
import io.vertx.core.VertxOptions

class Launcher{

    Vertx vertx;

    static void main(String[] args){
        def options = new VertxOptions()
       Vertx.clusteredVertx(options, {res->
           vertx = res.result()
       })
    }
}
