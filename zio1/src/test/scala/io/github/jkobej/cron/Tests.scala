package io.github.jkobej.cron

import com.cronutils.builder.CronBuilder
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.field.expression._
import io.github.jkobejs.cron.syntax._
import zio.test._
import zio.test.environment.TestClock
import zio.{Ref, ZIO}

import java.time.temporal.ChronoUnit
import java.time.{Duration, Instant, LocalDateTime, ZoneId}

object Tests extends DefaultRunnableSpec {
  private val dateTime     = LocalDateTime.of(2022, 1, 7, 6, 5)
  private val startInstant = LocalDateTime.of(2021, 11, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant
  private val endInstant   = LocalDateTime.of(2023, 2, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant
  private val days         = ChronoUnit.DAYS.between(startInstant, endInstant).toInt

  override def spec = suite("zio1")(
    testM("runs at specific time") {
      val cron = CronBuilder
        .cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
        .withSecond(FieldExpressionFactory.on(0))
        .withMinute(FieldExpressionFactory.on(5))
        .withHour(FieldExpressionFactory.on(6))
        .withDoM(FieldExpressionFactory.on(7))
        .withDoW(FieldExpressionFactory.questionMark())
        .withMonth(FieldExpressionFactory.always())
        .withYear(FieldExpressionFactory.on(2022))
        .instance()

      for {
        datesRef <- Ref.make[List[LocalDateTime]](Nil)
        eff = for {
                date <- zio.clock.localDateTime.orDie
                _    <- datesRef.update(dates => date :: dates)
              } yield ()
        _     <- TestClock.setTime(Duration.between(Instant.EPOCH, startInstant))
        _     <- TestClock.setTimeZone(ZoneId.systemDefault())
        fork  <- eff.repeatWithCron(cron).fork
        _     <- TestClock.adjust(Duration.ofDays(1)).repeatN(days)
        runs  <- fork.join
        dates <- datesRef.get
      } yield assertTrue(runs == 12L) && assertTrue(dates.toSet == (1 to 12).map(dateTime.withMonth).toSet)
    },
    testM("fails on invalid effect") {
      val cron = CronBuilder
        .cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
        .withSecond(FieldExpressionFactory.always())
        .withMinute(FieldExpressionFactory.always())
        .withHour(FieldExpressionFactory.always())
        .withDoM(FieldExpressionFactory.always())
        .withDoW(FieldExpressionFactory.questionMark())
        .withMonth(FieldExpressionFactory.always())
        .withYear(FieldExpressionFactory.always())
        .instance()

      val error = "error"
      for {
        fork   <- ZIO.fail(error).repeatWithCron(cron).either.fork
        _      <- TestClock.adjust(Duration.ofSeconds(1))
        either <- fork.join
      } yield assert(either)(Assertion.isLeft(Assertion.equalTo(error)))
    },
    testM("runs at specific time within other zone") {
      val cron = CronBuilder
        .cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
        .withSecond(FieldExpressionFactory.on(0))
        .withMinute(FieldExpressionFactory.on(5))
        .withHour(FieldExpressionFactory.on(5))
        .withDoM(FieldExpressionFactory.on(7))
        .withDoW(FieldExpressionFactory.questionMark())
        .withMonth(FieldExpressionFactory.always())
        .withYear(FieldExpressionFactory.on(2022))
        .instance()

      for {
        datesRef <- Ref.make[List[LocalDateTime]](Nil)
        eff = for {
          date <- zio.clock.localDateTime.orDie
          _    <- datesRef.update(dates => date :: dates)
        } yield ()
        _     <- TestClock.setTime(Duration.between(Instant.EPOCH, startInstant))
        _     <- TestClock.setTimeZone(ZoneId.systemDefault())
        fork  <- eff.repeatWithCron(cron, ZoneId.of("Europe/London")).fork
        _     <- TestClock.adjust(Duration.ofDays(1)).repeatN(days)
        runs  <- fork.join
        dates <- datesRef.get
      } yield assertTrue(runs == 12L) && assertTrue(dates.toSet == (1 to 12).map(dateTime.withMonth).toSet)
    }
  )
}
