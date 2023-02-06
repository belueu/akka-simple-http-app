package io.belueu.akkahttp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._

object AkkaSimpleHttp extends DefaultJsonProtocol {

  implicit val system = ActorSystem() // Akka Actors
  implicit val materializer = ActorMaterializer() // Akka Streams

  import system.dispatcher // Thread pool

  case class Post(
    userId: Int,
    id: Int,
    title: String,
    body: String
  )

  implicit val postFormat: RootJsonFormat[Post] = DefaultJsonProtocol.jsonFormat4(Post)

  val myPost: Post = Post(
    userId = 1,
    id = 100,
    title = "Mys super title",
    body = "My super body"
  )

  val source: String =
    """
      |{
      |  "title": "this is my simple title",
      |  "body": "this is my simple body"
      |}
      |""".stripMargin

  val request: HttpRequest = HttpRequest(
    method = HttpMethods.POST,
    uri = "https://jsonplaceholder.typicode.com/posts",
    entity = HttpEntity(
      ContentTypes.`application/json`, // application/json in most cases
      myPost.toJson.toString() // the actual data you want to send
    )
  )

  def sendRequest: Future[String] = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
    val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(response => response.entity.toStrict(2.seconds))
    entityFuture.map(entity => entity.data.utf8String)

    Http().singleRequest(request).flatMap { response =>
      response.entity.toStrict(2.seconds).map { entity =>
        entity.data.utf8String
      }
    }
  }

  def main(args: Array[String]): Unit = {
    //    println("Hello Akka -> Http")
    sendRequest.foreach(println)
  }
}
