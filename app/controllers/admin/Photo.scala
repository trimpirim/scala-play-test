package controllers.admin

import javax.inject.Inject

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

import scala.concurrent.Future
import scala.concurrent.Promise
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
//import play.api.libs.json._

import reactivemongo.api.Cursor

import play.modules.reactivemongo.{ // ReactiveMongo Play2 plugin
  MongoController,
  ReactiveMongoApi,
  ReactiveMongoComponents
}

import models.gallery.Photo._
import models.gallery.Album

import forms.admin.PhotoForm
import forms.admin.PhotoForm._

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._

import play.api.Play._
import play.api.i18n._

import reactivemongo.bson._

import java.io.File

import java.util.UUID
import java.util.Date

import com.sksamuel.scrimage._

class Photo @Inject() (val messagesApi: MessagesApi, val reactiveMongoApi: ReactiveMongoApi) extends Controller
	with MongoController with ReactiveMongoComponents {

	def collection: JSONCollection = db.collection[JSONCollection]("photos")
	def albumCollection: JSONCollection = db.collection[JSONCollection]("albums")

	def makeCreate(album: String) = Action { request =>
		implicit val messages = messagesApi.preferred(request)
		
		Ok(views.html.Admin.Photo.create(None, album, PhotoForm.form))
	}

	def create(albumID: String) = Action.async(parse.multipartFormData) { implicit request => 
		implicit val messages = messagesApi.preferred(request)

    val cursor = albumCollection.find(BSONDocument("_id" -> albumID)).cursor[Album]

    for {
      maybeAlbum <- cursor.headOption
      result <- maybeAlbum.map { album =>

      	PhotoForm.form.bindFromRequest.fold(
					errors => Future.successful(
						Ok(views.html.Admin.Photo.create(None, albumID, errors))),
					photo => {
						request.body.file("photo").map { picture =>
							import java.io.File
							val filename = picture.filename
							val extension = filename.replaceAll("^.*\\.(.*)$", "$1")
							val generated = UUID.randomUUID().toString + '.' + extension
							val fullPath = s"uploads/$generated"

							val copiedPhoto = photo.copy(
								path = Some(fullPath),
								filename = Some(generated),
								added = Some(new Date()),
								updated = Some(new Date()),
								album = Some(albumID)
							)

							collection.insert(copiedPhoto).map(_ => {
								picture.ref.moveTo(new File(s"$fullPath"))
								Redirect(routes.Gallery.edit(albumID))
							})
						}.getOrElse {
							Future.successful(Redirect(routes.Gallery.edit(albumID)))
						}
					}
				)
      }.getOrElse(Future(NotFound))
    } yield result
	}

	def makeEdit(albumID: String, id: String) = Action.async { request => 
		implicit val messages = messagesApi.preferred(request)
    val cursor = collection.find(BSONDocument("_id" -> id)).cursor[models.gallery.Photo]

    for {
      maybePhoto <- cursor.headOption
      result <- maybePhoto.map { photo =>
      	Future.successful(Ok(views.html.Admin.Photo.edit(Some(id), albumID, PhotoForm.form.fill(photo), photo)))
      }.getOrElse(Future(NotFound))
    } yield result
	}

	def preview(id: String) = Action.async { implicit request =>
		val cursor = collection.find(BSONDocument("_id" -> id)).cursor[models.gallery.Photo]

		for {
			maybePhoto <- cursor.headOption
			result <- maybePhoto.map { photo =>
				val file = new java.io.File(photo.path.get)
				Console.println(photo.path.get)
				val image = Image.fromFile(file).fit(400, 400).writer(Format.JPEG)
				val resized = image.resize(400)
				Future.successful(Ok.sendFile(resized.write(Format.JPEG)))
			}.getOrElse(Future(NotFound))
		} yield result
	}

	/*def edit(id: String) = Action.async { implicit request =>
		implicit val messages = messagesApi.preferred(request)
		import reactivemongo.bson.BSONDateTime

		AlbumForm.form.bindFromRequest.fold(
			errors => Future.successful(
				Ok(views.html.Admin.Gallery.edit(Some(id), errors))
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


	}*/

}