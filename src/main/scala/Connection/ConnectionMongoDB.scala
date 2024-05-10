package Connection
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._


object Mongodbcollection {
  private val mongoClient = MongoClient("mongodb://mongodb:27017")
  val database: MongoDatabase = mongoClient.getDatabase("MicroserviceSystemDB")
  val eServicesCollection: MongoCollection[Document] = database.getCollection("EService")
}

