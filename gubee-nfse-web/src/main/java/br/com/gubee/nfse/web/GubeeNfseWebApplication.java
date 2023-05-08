package br.com.gubee.nfse.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GubeeNfseWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GubeeNfseWebApplication.class, args);
	}

}

/*
public Long send(String cityName, Invoice invoice) throws Exception {
	try {
		return circuitBreakerRegistry
				.circuitBreaker( cityName )
				.decorateCallable( () -> invoiceService.send( cityName, invoice ) )
				.call();
	} catch (Exception e) {
		log.error( SENDING_ERROR_MESSAGE, e );
		throw e;
	}
}
 */
