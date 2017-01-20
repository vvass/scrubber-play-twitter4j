package services

import java.net.{DatagramPacket, DatagramSocket, InetAddress, InetSocketAddress}
import javax.inject.Inject

import akka.NotUsed
import akka.actor.{Actor, ActorSystem, Props}
import akka.io.Udp.SimpleSender
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.google.inject.ImplementedBy
import configurations.TwitterListenerConfiguration
import exceptions.ListenerException
import play.api.Logger
import play.api.libs.ws.WSClient
import twitter4j._
import twitter4j.json.DataObjectFactory

@ImplementedBy(classOf[TwitterListenerServiceImp])
trait TwitterListenerService {
  val tweetLanguage: String
  val tweetPrintBody: Boolean
  val maxTweets: Int
  val bufferSizeConfig: Int
  val publisherAsSingleSubscription: Boolean
}

/**
  * Listener that "Akka"-fies and listens to Twitter4j twitter stream
  * This is how we implement akka streams.
  */
class TwitterListenerServiceImp @Inject()(config: TwitterListenerConfiguration,  ws: WSClient, ccsi: CouchClientServiceImp)
    extends TwitterListenerService {
  import models.TweetModel._

  // -- Configs --
  override val tweetLanguage = this.config.tweetFilter.getString("listener.language").get
  override val tweetPrintBody = this.config.tweetFilter.getBoolean("print.body").get
  override val maxTweets = this.config.tweetFilter.getInt("max.tweets").get
  override val bufferSizeConfig = this.config.tweetFilter.getInt("buffer.size.config").get
  override val publisherAsSingleSubscription = this.config.tweetFilter.getBoolean("publisher.as.single.subscription").get
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
  implicit val materializer = ActorMaterializer() // TODO Metrics set up throttle configuration for buffers

  /**
    * Creates a singleton object for TwitterStream. This is
    * in combination with twitter4j. This is how you access the
    * twitter stream based of the url provided.
    */
  val twitterStream: TwitterStream = TwitterStreamFactory.getSingleton

  //Number of Jobs that will be processed
  val bufferSize: Int = bufferSizeConfig
  
  /**
    * The strategy used when we reach buffer size, currently drop oldest in queue if
    * taking to longThe strategy used when we reach buffer size, currently drop oldest
    * in queue if taking to long. There are other options if this doesn't work well.
    * {droptail, dropbuffer, dropnew, backpressure, fail}
    */
  def overflowStrategy = OverflowStrategy.dropHead
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
      .toMat(Sink.asPublisher(publisherAsSingleSubscription))(Keep.both)
      .run()
  
    val remote = new InetSocketAddress("127.0.0.1", 8136) // TODO configure
  
    val sender = system.actorOf(Props(new ccsi.SimpleSender(remote)))

    val statusListener: StatusListener = new StatusListener {
      override def onStallWarning(stallWarning: StallWarning): Unit = {
      }

      override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {
      }

      override def onScrubGeo(l: Long, l1: Long): Unit = {
      }

      //Statuses will be asynchronously sent to publisher actor
      override def onStatus(status: Status): Unit = {
        val startTimestamp = System.currentTimeMillis()
        if (tweetPrintBody) println(status.toString)
        val q = "\""
        sender ! s"{${q}id_str${q}:${q}${status.getId}${q}," +
          s"${q}text${q}:${q}${status.getText}${q}," +
          s"${q}screen_name${q}:${q}${status.getUser.getScreenName}${q}}"
  
      }

      override def onTrackLimitationNotice(i: Int): Unit = {
      }

      override def onException(e: Exception): Unit = {
//        throw new ListenerException
        e.printStackTrace()
      }
    }

    // Tie our listener to the TwitterStream and start listening
    twitterStream.addListener(statusListener)

    // This makes sure that you are only processing english typed texts
    twitterStream.sample(tweetLanguage)

    // Return Akka source of Tweets we just defined
    Source.fromPublisher(publisher)
  }

}