package org.terasology.launcher.net

import com.softwaremill.sttp.akkahttp.AkkaHttpBackend
import com.softwaremill.sttp.akkahttp._
import com.softwaremill.sttp.json4s._
import com.softwaremill.sttp._

import scala.util.{Failure, Success}
import scala.compat.java8.FutureConverters._
import scala.concurrent.ExecutionContext.Implicits.global

object Network {
  implicit val sttpBackend = AkkaHttpBackend()
  implicit val serialization =  org.json4s.native.Serialization

  def connect(uri: java.net.URI) = {
    val request = sttp.get(Uri(uri)).response(asString)

    val call = request.send()

    call.onComplete({
      case Failure(exception) => println("ERROR: could not connect!", exception)
      case Success(response) => println(s"SUCCESS: ${response.code}\n${response.unsafeBody}")
    })

    toJava(call)
  }

  case class Build(number: Int, url: String)
  case class Foo(url: String, builds: Seq[Build])

  // list build number
  // retrieve list of succesful builds
  def foo() = {
    sttp
      .get(uri"http://jenkins.terasology.org/job/DistroOmegaRelease/api/json")
      .response(asJson[Foo])
      .send()
      .foreach(response => {
        println(response.unsafeBody.builds.mkString("\n"))
      })
  }
}
