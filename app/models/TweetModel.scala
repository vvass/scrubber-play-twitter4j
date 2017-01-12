package models

import play.api.libs.json.Json

object TweetModel {

  final case class Hashtag(name: String)

  /**
    * This collects hashtags, would be great for processing tweets with
    * couchbase-client //TODO makes this process couch client requests
    */
  case class Tweet(body: String, user: String) {

    def hashTags: Set[Hashtag] = //TODO remove
      body
        .split(" ")
        .collect { case t if t.startsWith("#") => Hashtag(t) }
        .toSet
    // If you want to print the body to system.out
    if (false) { // TODO add to configuration
      println(body)
    }
    hashTags
  }

}
