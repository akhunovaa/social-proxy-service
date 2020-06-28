package com.botmasterzzz.social.config.init;

import com.botmasterzzz.social.config.ApplicationConfig;
import org.springframework.lang.NonNull;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

public class ApplicationInit implements WebApplicationInitializer {

    private static final String DISPATCHER_SERVLET_NAME = "dispatcher-social-service";

    @Override
    public void onStartup(@NonNull ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(ApplicationConfig.class);
        servletContext.addListener(new ContextLoaderListener(rootContext));
        rootContext.setServletContext(servletContext);
        ServletRegistration.Dynamic servlet = servletContext.addServlet(DISPATCHER_SERVLET_NAME, new DispatcherServlet(rootContext));
        servlet.addMapping("/");
        servlet.setLoadOnStartup(1);

    }
}
