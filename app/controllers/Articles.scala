package controllers

import javax.inject.Inject

import models.Article
import models.Article._
import org.joda.time.DateTime
import play.api.Logger

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.gridfs.{GridFS, ReadFile}
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.Future

class  Articles @Inject() (
  val cc: ControllerComponents,
  val reactiveMongoApi: ReactiveMongoApi,
  implicit val materializer: akka.stream.Materializer)
    extends AbstractController(cc) with MongoController with ReactiveMongoComponents with play.api.i18n.I18nSupport {

  import java.util.UUID
  import MongoController.readFileReads

  override lazy val parse: PlayBodyParsers = cc.parsers

  type JSONReadFile = ReadFile[JSONSerializationPack.type, JsString]

  // get the collection 'articles'
  def collection = reactiveMongoApi.database.
    map(_.collection[JSONCollection]("articles"))

  // a GridFS store named 'attachments'
  //val gridFS = GridFS(db, "attachments")
  private val gridFS = for {
    fs <- reactiveMongoApi.database.map(db =>
      GridFS[JSONSerializationPack.type](db))
    _ <- fs.ensureIndex().map { index =>
      // let's build an index on our gridfs chunks collection if none
      Logger.info(s"Checked index, result is $index")
    }
  } yield fs

  // list all articles and sort them
  val index = Action.async { implicit request =>
    // the cursor of documents
    val found = collection.map(_.find(Json.obj()).cursor[Article]())

    // build (asynchronously) a list containing all the articles
    found.flatMap(_.collect[List]()).map { articles =>
      Ok(views.html.articles(articles))
    }.recover {
      case e =>
        e.printStackTrace()
        BadRequest(e.getMessage())
    }
  }

  def showCreationForm = Action { implicit request =>
//    implicit val messages = messagesApi.preferred(request)

    Ok(views.html.editArticle(None, Article.form))
  }

  def showEditForm(id: String) = Action.async { implicit request =>
//    implicit val messages = messagesApi.preferred(request)
    
    // get the documents having this id (there will be 0 or 1 result)
    def futureArticle = collection.flatMap(
      _.find(Json.obj("_id" -> id)).one[Article])

    // ... so we get optionally the matching article, if any
    // let's use for-comprehensions to compose futures
    for {
      // get a future option of article
      maybeArticle <- futureArticle
      // if there is some article, return a future of result with the article and its attachments
      fs <- gridFS
      result <- maybeArticle.map { article =>
        // search for the matching attachments
        // find(...).toList returns a future list of documents
        // (here, a future list of ReadFileEntry)
        fs.find[JsObject, JSONReadFile](
          Json.obj("article" -> article.id.get)).collect[List]().map { files =>

          @inline def filesWithId = files.map { file => file.id -> file }
//          implicit val messages = messagesApi.preferred(request)

          Ok(views.html.editArticle(Some(id),
            Article.form.fill(article)))
        }
      }.getOrElse(Future.successful(NotFound))
    } yield result
  }

  def create = Action.async { implicit request =>
//    implicit val messages = messagesApi.preferred(request)

    Article.form.bindFromRequest.fold(
      errors => Future.successful(
        Ok(views.html.editArticle(None, errors))),

      // if no error, then insert the article into the 'articles' collection
      article => collection.flatMap(_.insert(article.copy(
        id = article.id.orElse(Some(UUID.randomUUID().toString)),
        creationDate = Some(new DateTime())
        )
      )).map(_ => Redirect(routes.Articles.index))
    )
  }

  def edit(id: String) = Action.async { implicit request =>
//    implicit val messages = messagesApi.preferred(request)

    Article.form.bindFromRequest.fold(
      errors => Future.successful(
        Ok(views.html.editArticle(Some(id), errors))),

      article => {
        // create a modifier document, ie a document that contains the update operations to run onto the documents matching the query
        val modifier = Json.obj(
          // this modifier will set the fields
          // 'updateDate', 'title', 'content', and 'publisher'
          "$set" -> Json.obj(
            
            "groupId" -> article.groupId,
            "artifactId" -> article.artifactId,
            "content" -> article.content,
            "publisher" -> article.publisher))

        // ok, let's do the update
        collection.flatMap(_.update(Json.obj("_id" -> id), modifier).
          map { _ => Redirect(routes.Articles.index) })
      })
  }

  def delete(id: String) = Action.async {
    (for {
      coll <- collection
      _ <- {
        coll.remove(Json.obj("_id" -> id))
      }
    } yield Ok).recover { case _ => InternalServerError }
  }
}
