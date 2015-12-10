package controllers

import play.api._
import play.api.mvc._

class Statics extends Controller {

	def index = Action {
		Ok(views.html.Statics.index())
	}

	def about = Action {
		Ok(views.html.Statics.about())
	}

	def contacts = Action {
		Ok(views.html.Statics.contacts())
	}

}