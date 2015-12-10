package forms.admin

import play.api.data._
import play.api.data.Forms.{ text, longNumber, mapping, nonEmptyText, optional, date }
import play.api.data.validation.Constraints.pattern

import models.gallery._
import models.gallery.Photo._

import java.util.Date

import reactivemongo.bson.{
	BSONObjectID
}

object PhotoForm {
	val form = Form(
		mapping(
			"id" -> optional(text verifying pattern(
				"""[a-fA-F0-9]{24}""".r, error = "error.objectId")),
			"title" -> nonEmptyText,
			"description" -> optional(text),
			"added" -> optional(date),
			"updated" -> optional(date)) {
			(id, title, description, added, updated) =>
				Photo(
					Some(BSONObjectID.generate.stringify),
					title,
					None,
					None,
					description,
					added.map(d => new Date()),
					updated.map(d => new Date()),
					None,
					None)
			} { photo =>
					Some(
						(photo.id,
						photo.title,
						photo.description,
						photo.added,
						photo.updated))
			}
		)
}
