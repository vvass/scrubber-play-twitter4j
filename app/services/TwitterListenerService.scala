package services

import javax.inject.Inject

import akka.NotUsed
import akka.actor.{Actor, ActorSystem}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import configurations.TwitterListenerConfiguration
import twitter4j._

trait TwitterListenerService {
  val tweetLanguage: String
}

/**
  * Listener that "Akka"-fies and listens to Twitter4j twitter stream
  *
  * This is how we implement akka streams.
  */
class TwitterListenerServiceImp @Inject()(config: TwitterListenerConfiguration)
    extends TwitterListenerService {
  import models.TweetModel._

  override val tweetLanguage =
    this.config.tweetFilter.getString("listener.language").get

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

  /**
    * Registers listener to twitterStream and starts listening to all english tweets
    *
    * @return Akka Source of Tweets taken from publisher
    */
  def listen: Source[Tweet, NotUsed] = {

    // Create ActorRef Source producing Tweet events
    val (actorRef, publisher) = Source
      .actorRef[Tweet](bufferSize, overflowStrategy)
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
        println(status.toString)
        actorRef ! new Tweet(status.getText, status.getUser.getName)
      }

      override def onTrackLimitationNotice(i: Int): Unit = { //TODO something here
      }

      override def onException(e: Exception): Unit =
        e.printStackTrace() // TODO custome exception here and log debug
    }

    // Tie our listener to the TwitterStream and start listening
    twitterStream.addListener(statusListener)

    // This makes sure that you are only processing english typed texts
    twitterStream.sample(tweetLanguage) // TODO add this to configs

    // Return Akka source of Tweets we just defined
    Source.fromPublisher(publisher)
  }

  /**
    * Filters tweet stream for those containing hashtags
    *
    * @return Future of Seq containing set of hashtags in each tweet
    */
  def hashTags = {

    val source: Source[Tweet, NotUsed] = this.listen

    source
      .filter(_.hashTags.nonEmpty)
      .take(100)
      .map(_.hashTags)
      .runWith(Sink.seq)
  }

}

class HelloActor extends Actor {

  /**
    * This is used to test that the app is up and running. This
    * passes to a route that processes the actor and displayes
    * the message.
    */
  def receive = {
    case msg: String => sender ! s"Hello, $msg"
  }
}
