package io.github.jkobejs

import com.cronutils.model.Cron
import com.cronutils.model.time.ExecutionTime
import zio.{Ref, ZIO}

import java.time.{Duration, ZoneId}
import java.util.Optional

package object cron {
  object syntax {
    implicit class CronOps[R, E, A](zio: ZIO[R, E, A]) {

      /**
       * Repeats effect using given cron schedule and zone id.
       *
       * @param cron
       *   cron definition
       * @param zoneId
       *   java time zone id
       * @return
       *   number of runs
       */
      def repeatWithCron(
        cron: Cron,
        zoneId: ZoneId = ZoneId.systemDefault()
      ): ZIO[R, E, Long] = cronSchedule(cron, zio, zoneId)
    }

  }

  private def cronSchedule[R, E, A](
    expression: Cron,
    effect: ZIO[R, E, A],
    zoneId: ZoneId = ZoneId.systemDefault()
  ): ZIO[R, E, Long] = {
    val getSleepFor: ZIO[Any, Nothing, Option[Duration]] = for {
      currentTime <- zio.Clock.localDateTime.map(_.atZone(ZoneId.systemDefault()))
    } yield optionalToOption(
      ExecutionTime
        .forCron(expression)
        .timeToNextExecution(currentTime.withZoneSameInstant(zoneId))
    )

    for {
      runsRef <- Ref.make(0L)
      _ <- (getSleepFor.flatMap {
             case Some(duration) => ZIO.sleep(duration).as(true) <* effect.zipLeft(runsRef.update(_ + 1))
             case None           => ZIO.succeed(false)
           }).repeatWhile(identity)
      runs <- runsRef.get
    } yield runs
  }

  private def optionalToOption[T](optional: Optional[T]): Option[T] =
    if (optional.isPresent)
      Some(optional.get)
    else None
}
