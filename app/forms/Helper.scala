package forms

object Helper {
	import views.html.helper.FieldConstructor
  implicit val myFields = FieldConstructor(views.html.form.input.f)    
}