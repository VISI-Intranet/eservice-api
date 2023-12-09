package Routing

import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, Formats, jackson}
import Repository._
import Model._

class EServicesRoutes(implicit val eServicesRepository: EServicesRepository) extends Json4sSupport {
  implicit val serialization = jackson.Serialization
  implicit val formats: Formats = DefaultFormats

  val route =
    pathPrefix("eservices") {
      concat(
        pathEnd {
          concat(
            get {
              complete(eServicesRepository.getAllEServices())
            },
            post {
              entity(as[EServices]) { eService =>
                complete(eServicesRepository.addEService(eService))
              }
            }
          )
        },
        path(IntNumber) { id =>
          concat(
            get {
              complete(eServicesRepository.getEServiceById(id))
            },
            put {
              entity(as[EServices]) { updatedEService =>
                complete(eServicesRepository.updateEService(id, updatedEService))
              }
            },
            delete {
              complete(eServicesRepository.deleteEServiceById(id))
            }
          )
        }
      )
    }
}
