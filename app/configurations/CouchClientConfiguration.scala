package configurations

import javax.inject.Inject

import play.api.Configuration

/**
  * Configuration for CouchClient connection. Configure host, ports, paths ... etc
  * @param config
  */
final class CouchClientConfiguration @Inject()(config: Configuration) {

  //Main root location of config, application.conf file
  lazy val root = this.config
  //The filter object that will be used when listening to twitter stream
  lazy val couchclient = this.root.getConfig("couchclient").get
}
