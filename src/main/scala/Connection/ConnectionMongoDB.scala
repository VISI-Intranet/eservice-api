package Connection
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._


object Mongodbcollection {
  private val mongoClient = MongoClient("mongodb://localhost:27017")
  val database: MongoDatabase = mongoClient.getDatabase("UniverPractice")
  val eServicesCollection: MongoCollection[Document] = database.getCollection("EService")
}

