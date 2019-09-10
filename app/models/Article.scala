package models

import org.joda.time.DateTime

import play.api.data._
import play.api.data.Forms.{ text, longNumber, mapping, nonEmptyText, optional }
import play.api.data.validation.Constraints.pattern

case class Article(
  id: Option[String],
  groupId: String,
  artifactId: String,
  location: String,
  category: String,
  content: String,
  publisher: String,
  creationDate: Option[DateTime] 
)

object Article {
  import play.api.libs.json._

  implicit object ArticleWrites extends OWrites[Article] {
    def writes(article: Article): JsObject = Json.obj(
      "_id" -> article.id,
      "groupId" -> article.groupId,
      "artifactId" -> article.artifactId,
      "location" -> article.location,
      "category" -> article.category,
      "content" -> article.content,
      "publisher" -> article.publisher,
      "creationDate" -> article.creationDate.fold(-1L)(_.getMillis)
      )
  }

  implicit object ArticleReads extends Reads[Article] {
    def reads(json: JsValue): JsResult[Article] = json match {
      case obj: JsObject => try {
        val id = (obj \ "_id").asOpt[String]
        val groupId = (obj \ "groupId").as[String]
        val artifactId = (obj \ "artifactId").as[String]
        val location = (obj \ "location").as[String]
        val category = (obj \ "category").as[String]
        val content = (obj \ "content").as[String]
        val publisher = (obj \ "publisher").as[String]
        val creationDate = (obj \ "creationDate").asOpt[Long]
        

        JsSuccess(Article(id, groupId, artifactId, 
          location, category, content,publisher,
          creationDate.map(new DateTime(_))
          ))
        
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }

  val form = Form(
    mapping(
      "id" -> optional(text verifying pattern(
        """[a-fA-F0-9]{24}""".r, error = "error.objectId")),
      "groupId" -> nonEmptyText,
      "artifactId" -> nonEmptyText,
      "location" -> text,
      "category" -> text,
      "content" -> text,
      "publisher" -> nonEmptyText,
      "creationDate" -> optional(longNumber)
      ) {
      (id, groupId, artifactId, location, category, content, publisher, creationDate) =>
      Article(
        id,
        groupId,
        artifactId,
        location,
        category,
        content,
        publisher,
        creationDate.map(new DateTime(_))
        )
    } { article =>
      Some(
        (article.id,
          article.groupId,
          article.artifactId,
          article.location,
          article.category,
          article.content,
          article.publisher,
          article.creationDate.map(_.getMillis)))       
    })
}
