package Model

case class EServices (
                       id: Int,
                       service: String,
                       title: String,
                       text: String,
                       price: Double
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

