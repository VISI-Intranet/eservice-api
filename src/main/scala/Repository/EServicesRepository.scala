package Repository

import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonDocument, BsonDouble, BsonInt32, BsonString}
import scala.concurrent.{ExecutionContext, Future}
import Connection._
import Model._

class EServicesRepository(implicit ec: ExecutionContext) {

  def getAllEServices(): Future[List[EServices]] = {
    val futureEServices = Mongodbcollection.eServicesCollection.find().toFuture()

    futureEServices.map { docs =>
      Option(docs).map(_.map { doc =>
        EServices(
          Some(doc.getObjectId("_id").toHexString),
          service = doc.getString("service"),
          title = doc.getString("title"),
          text = doc.getString("text"),
          price = doc.getDouble("price"),
          statusUslugi = doc.getString("statusUslugi")
        )
      }.toList).getOrElse(List.empty)
    }
  }

  def getEServiceById(id: Int): Future[Option[EServices]] = {
    val serviceDocument = Document("id" -> id)

    Mongodbcollection.eServicesCollection.find(serviceDocument).headOption().map {
      case Some(doc) =>
        Some(
          EServices(
            Some(doc.getObjectId("_id").toHexString),
            service = doc.getString("service"),
            title = doc.getString("title"),
            text = doc.getString("text"),
            price = doc.getDouble("price"),
            statusUslugi = doc.getString("statusUslugi")
          )
        )
      case None => None
    }
  }

  def addEService(eService: EServices): Future[String] = {
    val eServiceDocument = BsonDocument(
      "service" -> BsonString(eService.service),
      "title" -> BsonString(eService.title),
      "text" -> BsonString(eService.text),
      "price" -> BsonDouble(eService.price),
      "statusUslugi" -> BsonString(eService.statusUslugi)
    )

    Mongodbcollection.eServicesCollection.insertOne(eServiceDocument).toFuture().map(result =>
      {
        val insertedId = result.getInsertedId.asObjectId().getValue
        s"Услуга по айди ${insertedId} добавлена в базу данных."
      }
    )
  }

  def deleteEServiceById(id: Int): Future[String] = {
    val serviceDocument = Document("id" -> id)
    Mongodbcollection.eServicesCollection.deleteOne(serviceDocument).toFuture().map(_ => s"Услуга с ID $id удалена из базы данных.")
  }

  def updateEService(id: Int, updatedEService: EServices): Future[String] = {
    val filter = Document("id" -> id)

    val eServiceDocument = BsonDocument(
      "$set" -> BsonDocument(
        "service" -> BsonString(updatedEService.service),
        "title" -> BsonString(updatedEService.title),
        "text" -> BsonString(updatedEService.text),
        "price" -> BsonDouble(updatedEService.price),
        "statusUslugi"-> BsonString(updatedEService.statusUslugi)
      )
    )

    Mongodbcollection.eServicesCollection.updateOne(filter, eServiceDocument).toFuture().map { updatedResult =>
      if (updatedResult.wasAcknowledged() && updatedResult.getModifiedCount > 0) {
        s"Информация об услуге с ID $id успешно обновлена."
      }
      else {
        s"Обновление информации об услуге с ID $id не выполнено. Возможно, услуга не найдена или произошла ошибка в базе данных."
      }
    }
  }
}
