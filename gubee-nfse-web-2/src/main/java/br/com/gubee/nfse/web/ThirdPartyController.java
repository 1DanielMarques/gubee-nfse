package br.com.gubee.nfse.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RestController
@RequestMapping("/service2")
public class ThirdPartyController {

    private int initial = 1;

    @GetMapping("/mix")
    public ResponseEntity<String> mixOperation() {
        var randomNumber = (int) (Math.random() * 10);
        if (randomNumber % 2 == 0 && initial > 1) {
            return ResponseEntity.status(HttpStatus.valueOf(429)).body("Error");
        } else {
            initial = 2;
            return ResponseEntity.status(HttpStatus.valueOf(200)).body("Success");
        }
    }

    @GetMapping("/good")
    public ResponseEntity<String> goodOperation() {
        return ResponseEntity.status(HttpStatus.valueOf(200)).body("Success");
    }

    @GetMapping("/bad")
    public ResponseEntity<String> badOperation() {
        return ResponseEntity.status(HttpStatus.valueOf(429)).body("Too Many requests");
    }

}
