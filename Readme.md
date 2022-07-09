# ZIO Cron

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.jkobejs/zio-cron.12/badge.svg)](https://mvnrepository.com/artifact/io.github.jkobejs/zio-cron)

Cron scheduler for ZIO 1 and ZIO 2 inspired by [cron4zio](https://github.com/tharwaninitin/cron4zio).

It uses [cron-utils](https://github.com/jmrozanec/cron-utils) to define cron and calculate next run. Please take a look
at repository to find out how to define and parse cron expressions.

## SBT

ZIO1

```scala
libraryDependencies += "io.github.jkobejs" %% "zio1-cron" % "x.x.x"
```

ZIO2

```scala
libraryDependencies += "io.github.jkobejs" %% "zio-cron" % "x.x.x"
```

## Example

```scala
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import io.github.jkobejs.cron.syntax.CronOps
import zio.{Clock, Console, ZIO, ZIOAppDefault}

object Main extends ZIOAppDefault {
  def run = {
    val zio = for {
      currentTime <- Clock.localDateTime
      _ <- Console.printLine(s"${currentTime.toString}")
    } yield ()

    val parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))

    for {
      // every 2 seconds in this year
      cron <- ZIO.attempt(parser.parse("*/2 * * * * ? 2022"))
      runs <- zio.repeatWithCron(cron)
      _ <- Console.printLine(s"Total runs: $runs")
    } yield ()
  }
}
```
