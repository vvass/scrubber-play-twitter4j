import com.google.inject.AbstractModule
import services.{TwitterListenerService, TwitterListenerServiceImp}

class Module extends AbstractModule {

  def configure = {
    bind(classOf[TwitterListenerService])
      .to(classOf[TwitterListenerServiceImp])
  }
}
