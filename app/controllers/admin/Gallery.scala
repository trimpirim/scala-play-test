package controllers.admin

import javax.inject.Inject

import scala.concurrent.Future
import scala.concurrent.Promise

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

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

import forms.admin.AlbumForm
import forms.admin.AlbumForm._

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._

import java.util.Date
import java.util.UUID
import java.util.UUID._

import org.joda.time.DateTime

import play.api.Play.current
import play.api.i18n.{ I18nSupport, MessagesApi }

import reactivemongo.bson._

class Gallery @Inject() (val messagesApi: MessagesApi, val reactiveMongoApi: ReactiveMongoApi) extends Controller
	with MongoController with ReactiveMongoComponents {

	def collection: JSONCollection = db.collection[JSONCollection]("albums")
	def photosCollection: JSONCollection = db.collection[JSONCollection]("photos")

	def list = Action.async {
		val future = collection.find(Json.obj()).cursor[Album].collect[List]()
		future.map { albums =>
			Ok(views.html.Admin.Gallery.list(albums))
		}
	}

	def createForm = Action { request =>
		implicit val messages = messagesApi.preferred(request)
		
		Ok(views.html.Admin.Gallery.create(None, AlbumForm.form))
	}

	def editForm(id: String) = Action.async { request => 
		implicit val messages = messagesApi.preferred(request)
		val objectId = BSONObjectID(id)
    val cursor = collection.find(BSONDocument("_id" -> id)).cursor[Album]

    for {
      maybeAlbum <- cursor.headOption
      result <- maybeAlbum.map { implicit album =>

      	val photos = photosCollection.find(BSONDocument("album" -> id)).cursor[models.gallery.Photo].collect[List]()
				photos.map { photos =>
	        Ok(views.html.Admin.Gallery.edit(Some(id), AlbumForm.form.fill(album), Some(photos)))
				}
      }.getOrElse(Future(NotFound))
    } yield result
	}

	def create = Action.async { implicit request =>
		implicit val messages = messagesApi.preferred(request)

		AlbumForm.form.bindFromRequest.fold(
			errors => Future.successful(
				Ok(views.html.Admin.Gallery.create(None, errors))),
			album => {
				collection.insert(
					album.copy(
						added = Some(new Date()),
						updated = Some(new Date())
					)
				).map(_ => Redirect(routes.Gallery.list))
				Future.successful(Redirect(routes.Gallery.list))
			}
		)
	}

	def edit(id: String) = Action.async { implicit request =>
		implicit val messages = messagesApi.preferred(request)
		import reactivemongo.bson.BSONDateTime

		AlbumForm.form.bindFromRequest.fold(
			errors => Future.successful(
				Ok(views.html.Admin.Gallery.edit(Some(id), errors, None))
			),
			album => {
				val objectId = BSONObjectID(id)
				val modifier = Json.obj(
					"$set" -> Json.obj(
						"updated" -> Some(new Date()),
						"title" -> album.title,
						"description" -> album.description
					)
				)

				collection.update(BSONDocument("_id" -> objectId), modifier).
					map { _ => Redirect(routes.Gallery.list) }
			}
		)


	}

}