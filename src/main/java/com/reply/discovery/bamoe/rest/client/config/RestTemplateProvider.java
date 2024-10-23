package com.reply.discovery.bamoe.rest.client.config;

import java.util.List;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Factory class for {@link RestTemplate}
 * 
 * @author Giuseppe Regina
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestTemplateProvider
{
	/**
	 * Returns the {@link RestTemplate} used for the communication towards Kenan
	 *
	 * @param interceptor
	 *                              The {@link RestTemplateInterceptor}
	 * @param connectionTimeout
	 *                              Connection timeout
	 * @param readTimeout
	 *                              Read timeout
	 * @param maxConnections
	 *                              Max number of connections
	 * @param maxConnectionPerRoute
	 *                              Max number of connections per route
	 * @return The configured {@link RestTemplate} instance
	 */
	public static RestTemplate getRestTemplate(String username, String password,
			int connectionTimeout,
			int readTimeout,
			int maxConnections,
			int maxConnectionPerRoute)
	{
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(connectionTimeout)
				.setConnectionRequestTimeout(connectionTimeout)
				.setSocketTimeout(readTimeout)
				.build();
		CloseableHttpClient client = HttpClientBuilder.create()
				.setDefaultRequestConfig(config)
				.setMaxConnTotal(maxConnections)
				.setMaxConnPerRoute(maxConnectionPerRoute)
				.build();
		ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(client);
		ClientHttpRequestFactory bufferingRequestFactory = new BufferingClientHttpRequestFactory(requestFactory);
		RestTemplate restTemplate = new RestTemplate(bufferingRequestFactory);
		
		restTemplate.getInterceptors().add(
				  new BasicAuthorizationInterceptor(username, password));
		
		// Hacky hack
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON, new MediaType("text", "json")));
		converter.setObjectMapper(objectMapper);


		restTemplate.getMessageConverters().add(0, converter);
		return restTemplate;
	}
	
	/**
	 * Gets the keep alive strategy for the client
	 * @return The strategy
	 */
	
	
}
