package edu.utah.blulab.configuration;

import edu.utah.blulab.services.CorpusManagementWebService;
import edu.utah.blulab.services.ICorpusManagementWebService;
import edu.utah.blulab.services.INlpOperationsWebServices;
import edu.utah.blulab.services.NlpOperationsWebServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
@EnableWebMvc
//@ComponentScan(basePackages = {"edu.utah.blulab.services","edu.utah.blulab.controller"})
@ComponentScan(basePackages = "edu.utah.blulab")
public class IevizConfiguration extends WebMvcConfigurerAdapter {
    @Bean(name = "viewResolver")
    public ViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }



    @Bean(name = "CorpusManagementWebService")
    public ICorpusManagementWebService corpusManagementWebService() {
        return new CorpusManagementWebService();
    }

    @Bean(name = "NlpOperationsWebService")
    public INlpOperationsWebServices nlpOperationsWebServices() {
        return new NlpOperationsWebServices();
    }

    //	@Bean
//	public View jsonTemplate() {
//		MappingJackson2JsonView view = new MappingJackson2JsonView();
//		view.setPrettyPrint(true);
//		view.setContentType("text/plain");
//		return view;
//	}
//
    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("utf-8");
        resolver.setMaxUploadSize(8000000);
        return resolver;
    }


//	@Bean(name = "jsonMessageConverter")
//	public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
//		return new MappingJackson2HttpMessageConverter();
//	}
//
//
//	@Bean(name = "requestMappingHandlerAdapter")
//	public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
//		RequestMappingHandlerAdapter handlerAdapter = new RequestMappingHandlerAdapter();
//		handlerAdapter.getMessageConverters().add(0, jackson2HttpMessageConverter());
//		return handlerAdapter;
//	}

    /*
     * Configure ResourceHandlers to serve static resources like CSS/ Javascript etc...
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }
}