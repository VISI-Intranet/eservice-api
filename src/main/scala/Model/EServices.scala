package Model

case class EServices (
                       _id:Option[String] = None,
                       service: String,
                       title: String,
                       text: String,
                       price: Double,
                       statusUslugi:String
                   )
case class Users (
                 id: Int,
                 username: String,
                 profession: String,
                 email: String,
                 password: String,
                 registration: String,
                 is_active: Boolean,
                 gender: String,
                 age: Int,
                 Koshelek: Double
                 )

