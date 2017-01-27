package services

import java.net._
import javax.inject.Inject

import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}
import akka.util.ByteString
import com.google.inject.ImplementedBy
import configurations.CouchClientConfiguration
import play.api.Logger
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

@ImplementedBy(classOf[CouchClientServiceImp])
trait CouchClientService {
  val CCHost: String
  val CCPort: String
  val CCPath: String
}

/**
  * Couch Client service that work to query couchbase-client-main
  */
class CouchClientServiceImp @Inject()(implicit ec: ExecutionContext, ws: WSClient, config: CouchClientConfiguration) extends CouchClientService {
  
  // -- Configs --
  override val CCHost = this.config.couchclient.getString("host").get // TODO need to remove or reuse
  override val CCPort = this.config.couchclient.getString("port").get
  override val CCPath = this.config.couchclient.getString("path").get
  // -- End Configs --
  
  class SimpleSender(remote: InetSocketAddress) extends Actor {
    import context.system
    IO(Udp) ! Udp.SimpleSender
    
    def receive = {
      case Udp.SimpleSenderReady =>
        context.become(ready(sender()))
    }
    
    def ready(send: ActorRef): Receive = {
      case msg: String =>
        Logger.info("Sending " + msg)
        send ! Udp.Send(ByteString(msg, "UTF-8").compact, remote)
    }
  }
}




