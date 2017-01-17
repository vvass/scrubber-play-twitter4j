package configurations

import javax.inject.Inject

import play.api.Configuration

/**
  * Configuration for TwitterListener implementation in TwitterListener controller.
  * @param config
  */
final class TwitterListenerConfiguration @Inject()(config: Configuration) {

  //Main root location of config, application.conf file
  lazy val root = this.config
  //The filter object that will be used when listening to twitter stream
  lazy val tweetFilter = this.root.getConfig("tweet").get
}
