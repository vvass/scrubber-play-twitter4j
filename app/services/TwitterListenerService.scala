package services

import java.net.URLEncoder
import javax.inject.Inject

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import configurations.TwitterListenerConfiguration
import models.TweetModel.Tweet
import play.api.Logger
import twitter4j._

import scala.util.Success

trait TwitterListenerService {
  val tweetLanguage: String
  val tweetPrintBody: Boolean
}

/**
  * Listener that "Akka"-fies and listens to Twitter4j twitter stream
  * This is how we implement akka streams.
  */
class TwitterListenerServiceImp @Inject()(config: TwitterListenerConfiguration, ccsi: CouchClientServiceImp)
    extends TwitterListenerService {
  import models.TweetModel._

  // --  Configs --
  override val tweetLanguage = this.config.tweetFilter.getString("listener.language").get
  override val tweetPrintBody = this.config.tweetFilter.getBoolean("print.body").get
  // -- End Of Configs --

  //Akka Actor system and materializer must be initialized
  implicit val system = ActorSystem("TwitterListener")

  /**
    * Normally here you would use FlowMaterializer but in our
    * case we want to use ActorMaterializer. This is passed along
    * with the actorSystem ref to the controller. We can also set
    * certain buffer settings:
    *
    *   val materializer = ActorMaterializer(
    *     ActorMaterializerSettings(system)
    *       .withInputBuffer(
    *         initialSize = 64,
    *         maxSize = 64))
    *
    * http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0/scala/stream-rate.html
    */
  implicit val materializer = ActorMaterializer() // TODO set up throttle configuration for buffers

  /**
    * Creates a singleton object for TwitterStream. This is
    * in combination with twitter4j. This is how you access the
    * twitter stream based of the url provided.
    */
  val twitterStream: TwitterStream = TwitterStreamFactory.getSingleton

  //Number of Jobs that will be processed
  val bufferSize: Int = 100 // TODO add this to config, we will need to test this

  //The strategy used when we reach buffer size, currently drop oldest in queue if taking to long
  def overflowStrategy = OverflowStrategy.dropHead
  // TODO will need to be configered somehow
  // TODO Metrics testing for drop Head, we need to inc and dec the buffer size
  // TODO Metrics need to test this against WSCLIENT to see if there is a speed boost
  
  /**
    * Registers listener to twitterStream and starts listening to all english tweets
    *
    * @return Akka Source of Tweets taken from publisher
    */
  def listen: Source[Tweet, NotUsed] = {

    Logger.info("Started listening to twitter stream api.")

    // Create ActorRef Source producing Tweet events
    val (actorRef, publisher) = Source
      .actorRef[Tweet](bufferSize, overflowStrategy) // TODO Metrics add buffersize metrics
      /**
        * A publisher that is created with Sink.asPublisher(false) supports only a single subscription
        * Keeping both will take both parts of a sink and not just the materialized portion (right)
        */
      .toMat(Sink.asPublisher(false))(Keep.both) //TODO false needs to be in a configuration
      .run()

    val statusListener: StatusListener = new StatusListener {
      override def onStallWarning(stallWarning: StallWarning): Unit = { //TODO something here
      }

      override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice)
        : Unit = { //TODO something here
      }

      override def onScrubGeo(l: Long, l1: Long): Unit = { //TODO something here
      }

      //Statuses will be asynchronously sent to publisher actor
      override def onStatus(status: Status): Unit = {
        if (tweetPrintBody) println(status.toString)
        
        ccsi.runQuery(new Tweet(status.getId, status.getText, status.getUser.getScreenName))
        
        
      }

      override def onTrackLimitationNotice(i: Int): Unit = { //TODO something here
      }

      override def onException(e: Exception): Unit = {
        e.printStackTrace() // TODO custome exception here and log debug
      }
    }

    // Tie our listener to the TwitterStream and start listening
    twitterStream.addListener(statusListener)

    // This makes sure that you are only processing english typed texts
    twitterStream.sample(tweetLanguage) // TODO add this to configs

    // Return Akka source of Tweets we just defined
    Source.fromPublisher(publisher)
  }

}