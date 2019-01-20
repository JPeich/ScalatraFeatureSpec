import com.example.app._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.setInitParameter(ScalatraBase.PortKey, "8055")
    context.mount(new MyScalatraServlet, "/*")
  }
}