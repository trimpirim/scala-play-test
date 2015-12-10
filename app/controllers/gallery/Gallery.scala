package controllers.gallery

import javax.inject.Inject

import scala.concurrent.Future

import play.api.Logger
import play.api.mvc.{ Action, Controller }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._

import reactivemongo.api.Cursor

import play.modules.reactivemongo.{ // ReactiveMongo Play2 plugin
  MongoController,
  ReactiveMongoApi,
  ReactiveMongoComponents
}

import models.gallery.Album, Album._

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._


class Gallery @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller
	with MongoController with ReactiveMongoComponents {

	def collection: JSONCollection = db.collection[JSONCollection]("albums")

	def index = Action.async {
		val future = collection.find(Json.obj()).cursor[Album].collect[List]()
		future.map { albums => 
			Ok(views.html.Gallery.index(albums))
		}
	}
}