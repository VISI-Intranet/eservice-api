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
  val exchangeName = serviceConfig.getString("service.exchangeName")
  val port = serviceConfig.getString("service.port")


  implicit val system: ActorSystem = ActorSystem(serviceName)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Подключение к базе данных
  private val client = MongoClient("mongodb://mongodb:27017")
  implicit val db = client.getDatabase("UniverService")

  implicit val eServicesRepository = new EServicesRepository()

  val eServicesRoutes = new EServicesRoutes()

  val amqpActor = system.actorOf(Props(new AmqpActor(exchangeName,serviceName)),"amqpActor")
  amqpActor ! RabbitMQ.DeclareListener(
    queue = "Eservice",
    bind_routing_key = "univer.eservice-api.#",
    actorName = "consumerActor_1",
    handle = new Rabbit_Consumer().handle)



  // Старт сервера
//  private val bindingFuture = Http().newServerAt("localhost", port.toInt).bind(eServicesRoutes.route)
private val bindingFuture = Http().newServerAt("0.0.0.0", port.toInt).bind(eServicesRoutes.route)

  println(s"Server online at http://localhost:$port/ - docker creation")

  // Остановка сервера при завершении приложения
  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}