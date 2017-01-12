package controllers

import java.util.concurrent.Executors
import javax.inject.Inject

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Inbox, Props}
import akka.stream.Materializer
import akka.stream.scaladsl._
import play.api.mvc._
import services.{HelloActor, TwitterListenerService}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class TwitterListenerController @Inject()(implicit system: ActorSystem,
                                          materializer: Materializer)
    extends Controller {
  import models.TweetModel._

  /**
    * Controller interacting with twitter listener
    */
  val MAX_TWEETS = 1000

  val twitterListener = new TwitterListenerService

  val actorRef: ActorRef = system.actorOf(Props[HelloActor], "helloActor")

  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(10)) //TODO this needs to be in config
  // TODO Metrics work

  def actor = Action {

    /**
      * Tests actor to actor communication.  Inbox is actor created on the fly to retrieve response from TwitActor
      */
    val inbox = Inbox.create(system)
    inbox.send(actorRef, ", I am alive")

    Ok(inbox.receive(5.seconds).asInstanceOf[String])
  }

  def stream = Action {

    /**
      * Displays tweets from TwitterListener tweet stream
      */
    /**
      * Main source is twitter streamer from twitter4j, start listening to it
      */
    val source: Source[Tweet, NotUsed] = twitterListener.listen

    /**
      * This is how we send data to the broswer for display. We take the source
      * and send it in chunks to the browser for display as @content. See views.
      */
    Ok.chunked(
      source
        .map { tweet =>
          "Text: " + tweet.body + " User: " + tweet.user + "\n"
        }
        .limit(MAX_TWEETS) //Stops after 1000 displayed tweets
    )
  }

  def hashTagSequence = Action {

    /**
      * Obtains Future[Seq[Set[Tweet]] from Twitter listener and displays size and hashtags for each tweet
      */
    val seq = twitterListener.hashTags

    //DS2
    val seqSource = Source.fromFuture(seq)

    //Flatten Source of Seq[Set[Tweet]] to Source of Set[Tweet]
    val setSource = seqSource.mapConcat(identity)

    Ok.chunked(setSource map { se =>
      se.foldLeft("Hashtag count: " + se.size)((acc, hashtag) =>
        acc + " Hashtag: " + hashtag.name.filter(_ >= ' ')) + "\n"
    })
  }
}
