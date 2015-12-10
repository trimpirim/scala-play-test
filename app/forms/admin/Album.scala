package forms.admin

import play.api.data._
import play.api.data.Forms.{ text, longNumber, mapping, nonEmptyText, optional, date }
import play.api.data.validation.Constraints.pattern

import models.gallery._
import models.gallery.Album._

import java.util.Date

import java.util.UUID
import java.util.UUID._

import reactivemongo.bson.{
	BSONObjectID
}

object AlbumForm {
	val form = Form(
		mapping(
			"id" -> optional(text verifying pattern(
				"""[a-fA-F0-9]{24}""".r, error = "error.objectId")),
			"title" -> nonEmptyText,
			"description" -> optional(text),
			"added" -> optional(date),
			"updated" -> optional(date)) {
			(id, title, description, added, updated) => {
					Album(
						Some(BSONObjectID.generate.stringify),
						title,
						description,
						added.map(d => new Date()),
						updated.map(d => new Date()),
						None,
						None)
				}
			} { album => {
					Some(
						(album.id,
						album.title,
						album.description,
						album.added,
						album.updated))
				}
			}
		)
}
