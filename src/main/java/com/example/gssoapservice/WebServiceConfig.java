package com.example.gssoapservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.PayloadLoggingInterceptor;
import org.springframework.ws.soap.security.support.KeyStoreFactoryBean;
import org.springframework.ws.soap.security.x509.X509AuthenticationProvider;
import org.springframework.ws.soap.security.x509.populator.DaoX509AuthoritiesPopulator;
import org.springframework.ws.soap.security.xwss.XwsSecurityInterceptor;
import org.springframework.ws.soap.security.xwss.callback.KeyStoreCallbackHandler;
import org.springframework.ws.soap.security.xwss.callback.SpringCertificateValidationCallbackHandler;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.util.List;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "students")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema studentsSchema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("StudentPort");
        definition.setTargetNamespace("http://example.com/gssoapservice");
        definition.setLocationUri("/ws");
        definition.setSchema(studentsSchema);
        return definition;
    }

    @Bean
    public XsdSchema studentsSchema() {
        return new SimpleXsdSchema(new ClassPathResource("student-details.xsd"));
    }


    @Bean
    PayloadLoggingInterceptor payloadLoggingInterceptor() {
        return new PayloadLoggingInterceptor();
    }

    @Bean
    PayloadValidatingInterceptor payloadValidatingInterceptor() {
        PayloadValidatingInterceptor validatingInterceptor = new PayloadValidatingInterceptor();
        validatingInterceptor.setValidateRequest(true);
        validatingInterceptor.setValidateResponse(true);
        validatingInterceptor.setXsdSchema(studentsSchema());
        return validatingInterceptor;
    }



//    ###############################################################################
//    #                   INTERCEPTOR - PLAIN PASSWORD - XWSS                      #
//    ###############################################################################

//    @Bean
//    XwsSecurityInterceptor securityInterceptor() {
//        XwsSecurityInterceptor securityInterceptor = new XwsSecurityInterceptor();
//        securityInterceptor.setCallbackHandler(callbackHandler());
//        securityInterceptor.setPolicyConfiguration(new ClassPathResource("securityPolicy.xml"));
//        return securityInterceptor;
//    }
//
//    @Bean
//    SimplePasswordValidationCallbackHandler callbackHandler() {
//        SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();
//        callbackHandler.setUsersMap(Collections.singletonMap("admin", "pwd123"));
//        return callbackHandler;
//    }

//    ###############################################################################
//    #                   INTERCEPTOR - PLAIN PASSWORD - WSS4J                      #
//    ###############################################################################

//    @Bean
//    Wss4jSecurityInterceptor securityInterceptor() {
//        Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();
//        securityInterceptor.setValidationCallbackHandler(callbackHandler());
//        securityInterceptor.setValidationActions("UsernameToken");
//        return securityInterceptor;
//    }
//
//
//    @Bean
//    SpringSecurityPasswordValidationCallbackHandler callbackHandler() {
//        SpringSecurityPasswordValidationCallbackHandler callbackHandler = new SpringSecurityPasswordValidationCallbackHandler();
//        callbackHandler.setUserDetailsService(userDetailsService);
//        return callbackHandler;
//    }

//    ###############################################################################
//    #                  INTERCEPTOR - DIGESTED PASSWORD - XWSS                     #
//    ###############################################################################
//    @Bean
//    XwsSecurityInterceptor securityInterceptor() {
//        XwsSecurityInterceptor securityInterceptor = new XwsSecurityInterceptor();
//        securityInterceptor.setCallbackHandler(callbackHandler());
//        securityInterceptor.setPolicyConfiguration(new ClassPathResource("securityDigestPolicy.xml"));
//        return securityInterceptor;
//    }
//
//    @Bean
//    SpringDigestPasswordValidationCallbackHandler callbackHandler() {
//        SpringDigestPasswordValidationCallbackHandler callbackHandler = new SpringDigestPasswordValidationCallbackHandler();
//        callbackHandler.setUserDetailsService(userDetailsService);
//        return callbackHandler;
//    }

//    ###############################################################################
//    #                 INTERCEPTOR - DIGESTED PASSWORD - WSS4J                     #
//    ###############################################################################

//    @Bean
//    Wss4jSecurityInterceptor securityInterceptor() {
//        Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();
//        securityInterceptor.setValidationCallbackHandler(callbackHandler());
//        securityInterceptor.setValidationActions("UsernameToken");
//        return securityInterceptor;
//    }
//
//    @Bean
//    SpringSecurityPasswordValidationCallbackHandler callbackHandler() {
//        SpringSecurityPasswordValidationCallbackHandler callbackHandler = new SpringSecurityPasswordValidationCallbackHandler();
//        callbackHandler.setUserDetailsService(userDetailsService);
//        return callbackHandler;
//    }

//    ###############################################################################
//    #                 INTERCEPTOR - Certificate Authentication                    #
//    ###############################################################################
    @Bean
    XwsSecurityInterceptor securityInterceptor() {
        XwsSecurityInterceptor securityInterceptor = new XwsSecurityInterceptor();
        //Security Policy -> securityPolicy.xml
        securityInterceptor.setPolicyConfiguration(new ClassPathResource("securityPolicyX509.xml"));
        securityInterceptor.setCallbackHandlers(new CallbackHandler[]{
                keyStoreHandler(),
                certificateHandler()
        });
        return securityInterceptor;
    }

    public KeyStoreCallbackHandler keyStoreHandler() {
        KeyStoreCallbackHandler keyStoreHandler = new KeyStoreCallbackHandler();
        keyStoreHandler.setKeyStore(keystore().getObject());
        keyStoreHandler.setPrivateKeyPassword("123456");
        keyStoreHandler.setDefaultAlias("mycert");

        return keyStoreHandler;
    }

    @Bean
    KeyStoreFactoryBean keystore() {
        KeyStoreFactoryBean bean = new KeyStoreFactoryBean();
        bean.setLocation(new ClassPathResource("keystore/server.keystore"));
        bean.setPassword("123456");
        return bean;
    }

    @Bean
    SpringCertificateValidationCallbackHandler certificateHandler() {
        SpringCertificateValidationCallbackHandler callbackHandler = new SpringCertificateValidationCallbackHandler();
        callbackHandler.setAuthenticationManager(authenticationManager());
        return callbackHandler;
    }

    @Bean
    ProviderManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    @Bean
    DaoX509AuthoritiesPopulator authPopulator() {
        DaoX509AuthoritiesPopulator authPopulator = new DaoX509AuthoritiesPopulator();
        authPopulator.setUserDetailsService(userDetailsService);
        return authPopulator;
    }

    @Bean
    X509AuthenticationProvider authenticationProvider() {
        X509AuthenticationProvider authenticationProvider = new X509AuthenticationProvider();
        authenticationProvider.setX509AuthoritiesPopulator(authPopulator());
        return authenticationProvider;
    }

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        interceptors.add(payloadLoggingInterceptor());
        interceptors.add(payloadValidatingInterceptor());
        interceptors.add(securityInterceptor());
    }

}
