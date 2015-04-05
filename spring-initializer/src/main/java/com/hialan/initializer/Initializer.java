package com.hialan.initializer;

import com.hialan.config.MVCConfig;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

/**
 * User: Alan
 * Email:alan@hialan.com
 * Date: 4/5/15 03:13
 */
public class Initializer implements WebApplicationInitializer {
	public void onStartup(ServletContext servletContext) throws ServletException {
		AnnotationConfigWebApplicationContext mvcContext = new AnnotationConfigWebApplicationContext();

		mvcContext.register(MVCConfig.class);

		ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcherServlet",
				new DispatcherServlet(mvcContext));

		dispatcher.setLoadOnStartup(1);

		dispatcher.addMapping("/app/*");

	}
}
