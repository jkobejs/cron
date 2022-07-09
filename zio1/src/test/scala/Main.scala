import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import io.github.jkobejs.cron.syntax.CronOps
import zio._

object Main extends App {
  def run(args: List[String]) = {
    val zio = for {
      currentTime <- clock.localDateTime
      _           <- console.putStrLn(s"${currentTime.toString}")
    } yield ()

    val parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))

    (for {
      cron <- ZIO(parser.parse("*/2 * * * * ? 2022"))
      runs <- zio.repeatWithCron(cron)
      _    <- console.putStrLn(s"Total runs: $runs")
    } yield ()).exitCode
  }
}
