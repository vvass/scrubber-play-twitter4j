package controllers

import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc._
import services._

class CouchClientController(wsClient: WSClient,
                            couchClientServiceImp: CouchClientServiceImp)
    extends Controller {

  val url = couchClientServiceImp.CCHost
  val port = couchClientServiceImp.CCPort
  val path = couchClientServiceImp.CCPath

  val request: WSRequest = wsClient.url(s"http://$url:$port/$path/....")
}
