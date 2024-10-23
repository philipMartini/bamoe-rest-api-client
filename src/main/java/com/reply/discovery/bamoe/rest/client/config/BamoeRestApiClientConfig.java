package com.reply.discovery.bamoe.rest.client.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.reply.discovery.autogen.backends.bamoe.ApiClient;
import com.reply.discovery.autogen.backends.bamoe.generic.BamoeBackendApi;

/**
 * Exposes to spring the {@link Bean}s used to connect to Camunda
 *
 * @author Filippo Martini, Mario Bernardo
 */
@Configuration
public class BamoeRestApiClientConfig {
    /**
     * Ingress Base URI
     */
    @Value("${com.reply.discovery.service.baseuri}")
    private String hermesBaseUri;
    
    /**
     * Ingress Base URI
     */
    @Value("${com.reply.discovery.service.username}")
    private String username;
    
    /**
     * Ingress Base URI
     */
    @Value("${com.reply.discovery.service.password}")
    private String password;
    /**
     * Connection timeout (in millis)
     */
    //@Value("${it.sky.mdw.bbh.hermes.connectiontimeout}")
    private int connectionTimeout = 1000;
    /**
     * Read timeout (in millis)
     */
    //@Value("${it.sky.mdw.bbh.hermes.readtimeout}")
    private int readTimeout = 60000;
    /**
     * Max number of connections
     */
    //@Value("${it.sky.mdw.bbh.hermes.maxconnections}")
    private int maxConnections = 24;
    /**
     * Max number of connections per route
     */
    //@Value("${it.sky.mdw.bbh.hermes.maxconnectionperroute}")
    private int maxConnectionPerRoute = 32;

    /**
     * Obtains a client to interrogate SCMS/Products operations APIs
     *
     * @param apiClient
     *                  {@link ApiClient} used to perform calls
     * @return The specific client
     */
    @Bean
    public BamoeBackendApi processInstanceApi(@Qualifier(value = "apiClientBamoe") ApiClient apiClient)
    {
        return new BamoeBackendApi(apiClient); // NOSONAR fb-contrib:WI_MANUALLY_ALLOCATING_AN_AUTOWIRED_BEAN
    }

  
    /**
     * Obtains a {@link ApiClient} instance, used by all APIs towards SCMS
     *
     * @param restTemplate
     *                     {@link RestTemplate} used to perform http calls
     * @return The {@link ApiClient} instance
     */
    @Bean(name = "apiClientBamoe")
    public ApiClient apiClient(@Qualifier("bamoeRestTemplate") RestTemplate restTemplate)
    {
        ApiClient api = new ApiClient(restTemplate); // NOSONAR fb-contrib:WI_MANUALLY_ALLOCATING_AN_AUTOWIRED_BEAN
        api.setBasePath(this.hermesBaseUri);
        return api;
    }

    /**
     * Obtains a {@link RestTemplate} bean
     *
     * @param interceptor
     *                    Request/Response interceptor
     * @return The {@link RestTemplate} instance
     */
    @Bean(name = "bamoeRestTemplate")
    public RestTemplate restTemplate()
    {
        return RestTemplateProvider.getRestTemplate(username, password, connectionTimeout, readTimeout, maxConnections, maxConnectionPerRoute);    
       
    }
    
    
}

