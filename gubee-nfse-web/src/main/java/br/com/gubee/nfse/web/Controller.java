package br.com.gubee.nfse.web;
/*
criar 2 servicos com springboot
    Serviço 2:
     Criar uma operação: que randomicamente (Math.random que suba um erro de toomanyrequest - setar 429 como resposta)
     hora devolve um 200 hora volta um 429

    Servico 1,
        Criar uma endpoint que vc chame somente para teste que inicia chamadas ao servico 2 a cada 200ms
        - Criar uma operação que esse ednpoint ira invocar para chamar o servico 2
        - Essa operação deverá ser feita usando um circuit break para que ela abra o circuito quando comecar a dar erro

 */

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.function.Supplier;


@RestController
@RequestMapping("/service1")
public class Controller {

    private final RestTemplate restTemplate = new RestTemplate();

    private final CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(6)
            .permittedNumberOfCallsInHalfOpenState(3)
            .waitDurationInOpenState(Duration.ofMillis(1000))
            .failureRateThreshold(50)
            .build();

    private CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(config);

    private final RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(2)
            .waitDuration(Duration.ofMillis(1000))
            .failAfterMaxAttempts(true)
            .build();

    private RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);


    @GetMapping("/loop/{type}")
    public void loopAccessAnotherService(@PathVariable String type) throws Exception {
        var cont = 0;
        do {
            execute(type);
            Thread.sleep(200);
            cont++;
        } while (cont < 30);
    }

    @GetMapping("/manual/{type}")
    public void manualAccessAnotherService(@PathVariable String type) throws Exception {
        execute(type);
    }

    private int cont = 0;

    private void execute(String type) {
        var retry = retryRegistry.retry("myRetry");
        var circuitBreaker = circuitBreakerRegistry.circuitBreaker("myCircuit");
        Supplier<Integer> decoratedSupplier = Decorators.ofSupplier(() -> sendRequest(type).getStatusCode().value())
                .withCircuitBreaker(circuitBreaker)
                .withRetry(retry)
                .decorate();
        try {
            decoratedSupplier.get();
            System.out.println("Success " + circuitBreaker.getState());
        } catch (HttpClientErrorException.TooManyRequests e) {
            System.out.println("TooManyRequests " + circuitBreaker.getState());
        } finally {
            circuitBreaker.getEventPublisher().onEvent(event -> System.out.println(event));
        }
    }


    private ResponseEntity<String> sendRequest(String type) {
        cont++;
        System.out.println(cont);
        final String uri = String.format("http://localhost:8090/service2/%s", type);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
        return result;
    }


}
