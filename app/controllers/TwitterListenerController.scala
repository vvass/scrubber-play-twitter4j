package controllers

import java.util.concurrent.Executors
import javax.inject.Inject

import akka.NotUsed
import akka.actor.{ActorRef, Props}
import akka.stream.Materializer
import akka.stream.scaladsl._
import com.google.inject.ImplementedBy
import kamon.annotation.{Count, EnableKamon}
import play.api.mvc._
import services.TwitterListenerServiceImp

import scala.concurrent.ExecutionContext

@ImplementedBy(classOf[TwitterListenerController])
trait TwitterListenerController {}

/**
  * Controller interacting with twitter listener
  */
@EnableKamon
class TwitterListenerControllerImp @Inject()(materializer: Materializer, tlsi: TwitterListenerServiceImp)
  extends Controller with TwitterListenerController {
  import models.TweetModel._

  val MAX_TWEETS = tlsi.maxTweets

  implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(tlsi.executionThreadPoolNumber))
  // TODO Metrics work
  
  /**
    * Displays tweets from TwitterListener tweet stream
    */
  def stream = Action {

    /**
      * Main source is twitter streamer from twitter4j, start listening to it
      */
    val source: Source[Tweet, NotUsed] = tlsi.listen
    
    /**
      * This is how we send data to the browser for display. We take the source
      * and send it in chunks to the browser for display as @content. See views.
      */
    Ok.chunked(
      source.map { tweet =>
        "ID: " + tweet.id + " Text: " + tweet.text + " User: " + tweet.user + "\n"
      }.limit(MAX_TWEETS) //Stops after 1000 displayed tweets
    )
  }
}

