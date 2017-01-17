package models

object TweetModel {

  case class Tweet(id: Long, text: String, user: String)

}
