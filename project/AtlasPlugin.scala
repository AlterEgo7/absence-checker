import sbt.*
import complete.Parsers.*
import scala.sys.process.*
import scala.language.postfixOps

import java.nio.file.*

object AtlasPlugin extends AutoPlugin {
  override def trigger = noTrigger

  object autoImport {
    val atlasConfigFile = settingKey[Path]("The config file to be used by atlas")

    val atlasSchemaApply   = inputKey[Unit]("Apply the changes in the atlas schema")
    val atlasSchemaInspect = inputKey[Unit]("Inspect the current database schema")
  }

  import autoImport.*

  override lazy val globalSettings = Seq(
    atlasConfigFile := Paths.get("db/atlas.hcl")
  )

  override lazy val projectSettings = Seq(
    atlasSchemaApply   := {
      val args = spaceDelimited("env to inspect").parsed

      if (args.length != 1)
        sys.error("Please only provide a single env")

      val command =
        s"atlas schema apply --auto-approve --config file://${atlasConfigFile.value.toString} --env ${args.head}"

      println(s"Running command: $command")
      command !
    },
    atlasSchemaInspect := {
      val args = spaceDelimited("env to inspect").parsed

      if (args.length != 1)
        sys.error("Please only provide a single env")

      val command =
        s"""atlas schema inspect --config file://${atlasConfigFile.value.toString} --env ${args.head}"""

      println(s"Running command: $command")

      command !
    }
  )
}
