package services

//trait Kamon {
//  def start(): Unit
//  def shutdown(): Unit
//  def metrics(): MetricsModule
//  def tracer(): TracerModule
//  def counter(): Counter
//}
//
///**
//  * This is how we implement eager binding so that the Kamon API
//  * service is created early and eagerly.
//  */
//class KamonModule extends Module {
//
//  /**
//    * This will be built at the start of the application and makes sure
//    * that any refrence to Kamon will be binded to KamonAPIService class
//    */
//  def bindings(environment: Environment, configuration: Configuration) = {
//    Seq(bind[Kamon].to[KamonAPIService].eagerly())
//  }
//}
//
///**
//  * Kamon Metrics API that we will use throughout the application
//  */
//@Singleton
//class KamonAPIService @Inject()(lifecycle: ApplicationLifecycle,
//                                environment: Environment)
//    extends Kamon {
//
//  private val log = Logger(classOf[KamonAPIService])
//
//  log.info("Register the Kamon Play Module")
//
//  start() //force to start kamon eagerly on application startup
//
//  def start(): Unit = kamon.Kamon.start()
//  def shutdown(): Unit = kamon.Kamon.shutdown()
//  def metrics(): MetricsModule = kamon.Kamon.metrics
//  def tracer(): TracerModule = kamon.Kamon.tracer
//  def counter(): Counter = kamon.Kamon.metrics.counter("test-counter")
//
//}
