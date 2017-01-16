import com.google.inject.AbstractModule
import services.{
  CouchClientService,
  CouchClientServiceImp,
  TwitterListenerService,
  TwitterListenerServiceImp
}

class Module extends AbstractModule {

  def configure = {
    bind(classOf[TwitterListenerService])
      .to(classOf[TwitterListenerServiceImp])
      .asEagerSingleton
    bind(classOf[CouchClientService])
      .to(classOf[CouchClientServiceImp])
      .asEagerSingleton

  }

}
