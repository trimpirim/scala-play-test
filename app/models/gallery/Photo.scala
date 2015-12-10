package models.gallery

import java.util.Date
import java.util.UUID
import java.util.UUID._

import play.api.data._
import play.api.data.Forms.{ text, longNumber, mapping, nonEmptyText, optional }
import play.api.data.validation.Constraints.pattern

import reactivemongo.bson.{
  BSONDateTime, BSONDocument, BSONObjectID
}

case class Photo(
	id: Option[String] = Some(BSONObjectID.generate.stringify),
	title: String,
  filename: Option[String],
  path: Option[String],
	description: Option[String] = None,
	added: Option[Date] = None,
	updated: Option[Date] = None,
	deleted: Option[Date] = None,
  album: Option[String])

object Photo {
  import play.api.libs.json._

  implicit object PhotoWrites extends OWrites[Photo] {
    def writes(photo: Photo): JsObject = Json.obj(
      "_id" -> photo.id,
      "title" -> photo.title,
      "filename" -> photo.filename,
      "path" -> photo.path,
      "description" -> photo.description,
      "added" -> photo.added,
      "updated" -> photo.updated,
      "deleted" -> photo.deleted,
      "album" -> photo.album)
  }

  implicit object PhotoReads extends Reads[Photo] {
    def reads(json: JsValue): JsResult[Photo] = json match {
      case obj: JsObject => try {
      	val id = (obj \ "_id").asOpt[String]
        val title = (obj \ "title").as[String]
        val description = (obj \ "description").asOpt[String]
        val added = (obj \ "added").asOpt[Long]
        val updated = (obj \ "updated").asOpt[Long]
        val deleted = (obj \ "deleted").asOpt[Long]
        val filename = (obj \ "filename").asOpt[String]
        val path = (obj \ "path").asOpt[String]
        val album = (obj \ "album").asOpt[String]

        JsSuccess(Photo(id, title, filename, path, description,
          added.map(new Date(_)),
          updated.map(new Date(_)),
          deleted.map(new Date(_)),
          album))
        
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }
}