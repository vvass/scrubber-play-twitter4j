package services

import java.net.URLEncoder
import javax.inject.Inject

import akka.NotUsed
import akka.stream.scaladsl.Source
import configurations.CouchClientConfiguration

trait CouchClientService {
  val CCHost: String
  val CCPort: String
  val CCPath: String
}

/**
  * Couch Client service that work to query couchbase-client-main
  */
class CouchClientServiceImp @Inject()(config: CouchClientConfiguration)
    extends CouchClientService {
  override val CCHost = this.config.couchclient.getString("host").get
  override val CCPort = this.config.couchclient.getString("port").get
  override val CCPath = this.config.couchclient.getString("path").get

  case class Request(id: Long, text: String, user: String) {
    id.toString + "/" + user + "/" + URLEncoder
      .encode(text.toString, "UTF-8")
      .replaceAll("\\+", "%20")
  }

}
