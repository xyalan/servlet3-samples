package initializer;

import com.hialan.servlet.DynamicServlet;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.Set;

/**
 * User: Alan
 * Email:alan@hialan.com
 * Date: 4/5/15 02:28
 */
public class WebServletContainerInitializer implements ServletContainerInitializer {


	public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
		System.out.println("MyServletContainerInitializer init");
		ServletRegistration.Dynamic dynamic = servletContext.addServlet("dynamicServlet4", DynamicServlet.class);
		dynamic.addMapping("/dynamic4");

		servletContext.getServletRegistrations().get("dynamicServlet4").addMapping("/dynamic41");
	}
}
