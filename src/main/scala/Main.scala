import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import org.mongodb.scala.MongoClient
import Repository._
import Routing._
import Model._
import Route.Rabbit_Consumer

import scala.concurrent.{ExecutionContextExecutor, Future}
import java.util.Date
import akka.http.scaladsl.Http
import amqp.{AmqpActor, RabbitMQ}
import com.typesafe.config.ConfigFactory
object Main extends App {
  val serviceConfig = ConfigFactory.load("service_app.conf")
  val serviceName = serviceConfig.getString("service.serviceName")

  implicit val system: ActorSystem = ActorSystem(serviceName)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Подключение к базе данных
  val client = MongoClient()
  implicit val db = client.getDatabase("UniverService")

  implicit val eServicesRepository = new EServicesRepository()

  val eServicesRoutes = new EServicesRoutes()

  val amqpActor = system.actorOf(Props(new AmqpActor("X:routing.topic2",serviceName)),"amqpActor")
  amqpActor ! RabbitMQ.DeclareListener(
    queue = "eservice_api_queue",
    bind_routing_key = "univer.eservice_api.#",
    actorName = "consumerActor_1",
    handle = new Rabbit_Consumer().handle)



  // Старт сервера
  private val bindingFuture = Http().bindAndHandle(eServicesRoutes.route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  // Остановка сервера при завершении приложения
  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}