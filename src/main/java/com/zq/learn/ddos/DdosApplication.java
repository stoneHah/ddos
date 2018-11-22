package com.zq.learn.ddos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

@SpringBootApplication
public class DdosApplication {

	@Bean
	public SimpleClientHttpRequestFactory httpClientFactory() {
		SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
		httpRequestFactory.setReadTimeout(2000);
		httpRequestFactory.setConnectTimeout(2000);

		SocketAddress address = new InetSocketAddress("118.193.26.18", 8080);
//		SocketAddress address = new InetSocketAddress("103.15.187.110", 81);
		Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
		httpRequestFactory.setProxy(proxy);

		return httpRequestFactory;
	}

	@Bean
	public RestTemplate restTemplate(SimpleClientHttpRequestFactory httpClientFactory) {
		RestTemplate restTemplate = new RestTemplate();
//		RestTemplate restTemplate = new RestTemplate(httpClientFactory);
		return restTemplate;
	}

	public static void main(String[] args) {
		SpringApplication.run(DdosApplication.class, args);
	}
}
