package services

import javax.inject._

import kamon.metric.instrument.Counter
import kamon.metric.MetricsModule
import kamon.trace.TracerModule
import play.api.inject.{ApplicationLifecycle, Module}
import play.api.{Configuration, Environment, Logger}

import scala.concurrent.Future

trait Kamon {
  def start(): Unit
  def shutdown(): Unit
  def metrics(): MetricsModule
  def tracer(): TracerModule
  def counter(): Counter
}

class KamonModule extends Module {

  /**
    * This is how we implement eager binding so that the Kamon API
    * service is created early and eagerly.
    */
  def bindings(environment: Environment, configuration: Configuration) = {
    // This will be built at the start of the application
    Seq(bind[Kamon].to[KamonAPIService].eagerly())
  }
}

@Singleton
class KamonAPIService @Inject()(lifecycle: ApplicationLifecycle,
                                environment: Environment)
    extends Kamon {

  /**
    * Kamon Metrics API that we will use throughout the application
    */
  private val log = Logger(classOf[KamonAPIService])

  log.info("Register the Kamon Play Module")

  start() //force to start kamon eagerly on application startup

  def start(): Unit = kamon.Kamon.start()
  def shutdown(): Unit = kamon.Kamon.shutdown()
  def metrics(): MetricsModule = kamon.Kamon.metrics
  def tracer(): TracerModule = kamon.Kamon.tracer
  def counter(): Counter = kamon.Kamon.metrics.counter("test-counter")

}
