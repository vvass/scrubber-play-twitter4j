package controllers

import javax.inject.Inject

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl._
import com.google.inject.ImplementedBy
import kamon.annotation.EnableKamon
import play.api.mvc._
import services.TwitterListenerServiceImp

@ImplementedBy(classOf[TwitterListenerController])
trait TwitterListenerController {}

/**
  * Controller interacting with twitter listener
  */
@EnableKamon
class TwitterListenerControllerImp @Inject()(implicit system: ActorSystem, materializer: Materializer, tlsi: TwitterListenerServiceImp)
  extends Controller with TwitterListenerController {
  import models.TweetModel._
  
  /**
    * Displays tweets from TwitterListener tweet stream
    */
  def stream = Action {

    /**
      * Main source is twitter streamer from twitter4j, start listening to it
      */
    val source: Source[Tweet, NotUsed] = tlsi.listen
    
    Ok("It's on")
    
    /**
      * This is how we send data to the browser for display. We take the source
      * and send it in chunks to the browser for display as @content. See views.
      */
//    Ok.chunked(
//      source.map { tweet =>
//        "ID: " + tweet.id + " Text: " + tweet.text + " User: " + tweet.user + "\n"
//      }.limit(tlsi.maxTweets) //Stops after configured number of tweets
//    )
  }
}

