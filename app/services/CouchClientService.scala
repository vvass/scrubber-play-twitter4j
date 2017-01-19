package services

import java.net.{InetSocketAddress, URLEncoder}
import javax.inject.Inject

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}
import akka.io.{IO, Udp}
import com.google.inject.ImplementedBy
import configurations.CouchClientConfiguration
import play.api.libs.ws.{WSClient, WSRequest}

import scala.util.{Failure, Success}

@ImplementedBy(classOf[CouchClientServiceImp])
trait CouchClientService {
  val CCHost: String
  val CCPort: String
  val CCPath: String
}

/**
  * Couch Client service that work to query couchbase-client-main
  */
class CouchClientServiceImp @Inject()(ws: WSClient, config: CouchClientConfiguration) extends CouchClientService {
  import models.TweetModel._
  
  override val CCHost = this.config.couchclient.getString("host").get
  override val CCPort = this.config.couchclient.getString("port").get
  override val CCPath = this.config.couchclient.getString("path").get
  
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  
  def runQuery(startTimestamp: Long, tweet: Tweet) = {
    val request = ws
      .url(s"http://$CCHost:$CCPort/$CCPath/${tweet.id}/${tweet.user}/${URLEncoder.encode(tweet.text.toString, "UTF-8").replaceAll("\\+","%20")}")
      .withHeaders("Accept" -> "application/json")
      .get() // Perfrom request
  
    val displayCompleteTimestamp = s"Completed in ${ System.currentTimeMillis() - startTimestamp } millis. - "
    
    request onComplete {
      case Success(response) => {
        println(s"Completed in ${ System.currentTimeMillis() - startTimestamp } millis. - ")
//        println(math.max(Runtime.getRuntime.availableProcessors(), 1))
//        println(response.body.toString())
        if(response.body.contains("Trump")){
          println(tweet.text)
        }
      
      }
      case Failure(error) => {
        println(error)
      }
    }
  }
}

class CouchClientQueryListener(nextActor: ActorRef) extends Actor with ActorLogging {
  import context.system
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress("localhost", 0))
  
  def receive = {
    case Udp.Bound(local) => context.become(ready(sender()))
  }
  
  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, remote) =>
      val processed = // parse data etc., e.g. using PipelineStage
        socket ! Udp.Send(data, remote) // example server echoes back
      nextActor ! processed
    case Udp.Unbind  => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }
}

