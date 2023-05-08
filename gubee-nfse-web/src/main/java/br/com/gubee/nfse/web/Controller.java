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

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@RestController
@RequestMapping("/service1")
public class Controller {

    private final RestTemplate restTemplate = new RestTemplate();

    private final CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(6)
            .waitDurationInOpenState(Duration.ofMillis(10))
            .failureRateThreshold(50)
            .build();

    private CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

    @GetMapping
    public void accessAnotherService() throws Exception {
        int cont = 0;
        do {
            execute();
            Thread.sleep(200);
            cont++;
        } while (cont < 20);
    }

    private void execute() throws Exception {
        try {
            registry
                    .circuitBreaker("myCircuit", config)
                    .decorateCallable(() -> {
                        final String uri = "http://localhost:8090/service2";
                        RestTemplate restTemplate = new RestTemplate();
                        ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
                        System.out.println(result.getBody());
                        return result.getStatusCode();
                    }).call();
        } catch (Exception e) {
            System.out.println("Error");
        }

    }


}
