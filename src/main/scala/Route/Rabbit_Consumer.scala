package Route

import Connection.Mongodbcollection
import Model.EServices
import Repository.EServicesRepository

import scala.util.{Failure, Random, Success}
import akka.actor.ActorSystem
import akka.pattern.ask

import akka.util.Timeout
import amqp._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, Formats, jackson}
import org.mongodb.scala.bson.{BsonDocument, BsonDouble, BsonString}
import reactor.core.publisher.Signal.complete

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.matching.Regex
import io.circe.syntax._
import io.circe.generic.auto._

class Rabbit_Consumer(implicit val system:ActorSystem) extends Json4sSupport{
  implicit val ex:ExecutionContext = system.dispatcher
  implicit val serialization = jackson.Serialization
  implicit val formats: Formats = DefaultFormats
  implicit val timeout = Timeout(3 seconds)
  implicit val eServicesRepository = new EServicesRepository()



  val amqpActor = system.actorSelection("user/amqpActor")
  def handle(message:Message):Unit={
    message.routingKey match {

      case "univer.eservice-api.getStudyCertificatStudent" =>

        val e = new EServicesRepository()
        val result = e.getEServiceByObjId(message.body)


        result.onComplete {
          case Success(res) =>
            val jsonString: String = res.map(_.asJson.noSpaces).getOrElse("")
            println(jsonString)
            amqpActor ! RabbitMQ.Answer(message.replyTo,message.correlationId,jsonString)

        }


      case "univer.eservice-api.createStudyCertificate" =>

        val eServiceDocument = BsonDocument(
          "service" -> BsonString("StudyCertificate"),
          "title" -> BsonString("Справка с место учебы"),
          "text" -> BsonString(s"Данная справка подтверждает что студент ${message.body} обучаеться в Казну"),
          "price" -> BsonDouble(0),
          "statusUslugi" -> BsonString("Забирайте")
        )
        var insertedId = ""

        Mongodbcollection.eServicesCollection.insertOne(eServiceDocument).toFuture().map(result => {
          insertedId = result.getInsertedId.asObjectId().getValue.toString
          println(s"Услуга по айди ${insertedId} добавлена в базу данных.")
          amqpActor ! RabbitMQ.Tell(s"univer.student-api.eserviceCreatedOption" , s"Ваша справка с место учебы уже готово по id ${insertedId}!!!")
        })





      case "univer.eservice_api.CheckRoomForStudent"=>{
        println(message)
        val id=Random.nextInt(20000)
        val service = EServices(
          service = "obshaga",
          title = "jbshaga beru",
          text = message.body,
          price = 0,
          statusUslugi = "В обработке")
        eServicesRepository.addEService(service)
        println("Сервис добавлен в базу данных!")
        (amqpActor ? RabbitMQ.Ask("univer.facultet_api.zaprosNaObshagu", message.body)).onComplete {
          case Success(value: String) => {
            val service = EServices(
              service = "obshaga",
              title = "jbshaga beru",
              text = message.body,
              price = 0,
              statusUslugi = value)
            eServicesRepository.updateEService(id, service)
            println("Статус сервиса обновлен в базе!")
          }
          case Failure(ex) => {
            println(s"Не удалось получить ответ от amqpActor: ${ex.getMessage}")
          }
        }
      }

      case "univer.eservice_api.UpdateRequest" => {
        val studentJson = JsonMethods.parse(message.body)
        val ballEnt = (studentJson \ "ball_ent").extract[Int]
        val email = (studentJson \ "address").extract[String]

        val emailPattern: Regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".r

        val isValidEmail: Boolean = emailPattern.findFirstMatchIn(email).isDefined

        val isValidBall = ballEnt >= 0 && ballEnt<=140

        isValidEmail && isValidBall match {
          case true => amqpActor ! RabbitMQ.Answer(message.replyTo,message.correlationId,true.toString)
          case false => amqpActor ! RabbitMQ.Answer(message.replyTo,message.correlationId,false.toString)
        }
      }

      case "" => {
      }

    }
  }
}
