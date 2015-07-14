package codacy.dockerApi

import java.nio.file.{Files, Path, Paths}
import play.api.data.validation.ValidationError
import play.api.libs.json._
import scala.util.{Failure, Success, Try}

trait DockerEnvironment{

  def config(implicit spec: Spec): Try[Option[FullConfig]] = Try(Files.readAllBytes(configFilePath)).transform(
    raw => Try(Json.parse(raw)).flatMap(
      _.validate[FullConfig].fold(
        asFailure,
        conf => Success(Option(conf))
      )),
    _ => Success(Option.empty[FullConfig])
  )

  lazy val spec: Try[Spec] = {
    Try(
      Files.readAllBytes(Paths.get(getClass.getResource("/docs/patterns.json").toURI))
    ).flatMap{ case bytes =>
      Try( Json.parse(bytes) ).flatMap(_.validate[Spec].fold(
        asFailure,
        Success.apply
      ))
    }
  }

  private[this] def asFailure(error: Seq[(JsPath, Seq[ValidationError])]) =
    Failure(new Throwable(Json.stringify(JsError.toFlatJson(error))))

  private[this] lazy val configFilePath = sourcePath.resolve(".codacy.json")

  private[this] lazy val sourcePath = Paths.get("/src")
}