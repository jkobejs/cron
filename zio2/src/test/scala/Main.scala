import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import io.github.jkobejs.cron.syntax.CronOps
import zio.{Clock, Console, ZIO, ZIOAppDefault}

object Main extends ZIOAppDefault {
  def run = {
    val zio = for {
      currentTime <- Clock.localDateTime
      _           <- Console.printLine(s"${currentTime.toString}")
    } yield ()

    val parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))

    for {
      cron <- ZIO.attempt(parser.parse("*/2 * * * * ? 2022"))
      runs <- zio.repeatWithCron(cron)
      _    <- Console.printLine(s"Total runs: $runs")
    } yield ()
  }
}
