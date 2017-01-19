package models

import javax.inject._

@Singleton
object TweetModel {

  case class Tweet(id: Long, text: String, user: String)

}
