package services

import java.net.URLEncoder
import javax.inject.Inject

import configurations.CouchClientConfiguration
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.Future
import scala.util.{Failure, Success}

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
  
  def runQuery(tweet: Tweet) = {
    val request: WSRequest = ws
      .url(s"http://$CCHost:$CCPort/$CCPath/${tweet.id}/${tweet.user}/${URLEncoder.encode(tweet.text.toString, "UTF-8").replaceAll("\\+","%20")}")
      .withHeaders("Accept" -> "application/json")
    
    val r = request.get()
  
    r onComplete {
      case Success(response) => {
        println(response.body.toString())
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