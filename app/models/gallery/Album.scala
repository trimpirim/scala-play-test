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

case class Album(
	id: Option[String] = Some(BSONObjectID.generate.stringify),
	title: String,
	description: Option[String] = None,
	added: Option[Date] = None,
	updated: Option[Date] = None,
	deleted: Option[Date] = None,
  photos: Option[List[Photo]])

object Album {
  import play.api.libs.json._

  implicit object AlbumWrites extends OWrites[Album] {
    def writes(album: Album): JsObject = {
      Json.obj(
        "_id" -> album.id,
        "title" -> album.title,
        "description" -> album.description,
        "added" -> album.added,
        "updated" -> album.updated,
        "deleted" -> None,
        "photos" -> album.photos)
    }
  }

  implicit object AlbumReads extends Reads[Album] {
    def reads(json: JsValue): JsResult[Album] = json match {
      case obj: JsObject => try {
      	val oid = (obj \ "_id").asOpt[String]
        //val id = (oid \ "$oid").asOpt[String]
        val title = (obj \ "title").as[String]
        val description = (obj \ "description").asOpt[String]
        val added = (obj \ "added").asOpt[Long]
        val updated = (obj \ "updated").asOpt[Long]
        val deleted = (obj \ "deleted").asOpt[Long]
        val photos = (obj \ "photos").asOpt[List[Photo]]

        JsSuccess(Album(oid, title, description,
          added.map(new Date(_)),
          updated.map(new Date(_)), 
          deleted.map(new Date(_)),
          photos))
        
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }
}
/*object JsonFormats {
  import play.api.libs.json.Json
  import play.api.data._
  import play.api.data.Forms._

  // Generates Writes and Reads for Feed and User thanks to Json Macros
  implicit val albumFormat = Json.format[Album]
}*/