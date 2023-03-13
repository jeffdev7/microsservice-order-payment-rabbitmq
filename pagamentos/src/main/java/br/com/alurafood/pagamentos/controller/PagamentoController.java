package br.com.alurafood.pagamentos.controller;

import br.com.alurafood.pagamentos.dto.PagamentoDTO;
import br.com.alurafood.pagamentos.model.Pagamento;
import br.com.alurafood.pagamentos.service.PagamentoService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    @Autowired
    private PagamentoService service;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping

    public Page<PagamentoDTO> listar(@PageableDefault(size = 10) Pageable paginacao) {
        return service.getAll(paginacao);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PagamentoDTO> detalhar(@PathVariable @NotNull Long id) {
        PagamentoDTO dto = service.getById(id);

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<PagamentoDTO> cadastrar(@RequestBody @Valid PagamentoDTO dto,
                                                  UriComponentsBuilder uriBuilder) {
        PagamentoDTO pagamento = service.createPayment(dto);
        URI endereco = uriBuilder.path("/pagamentos/{id}").buildAndExpand(pagamento.getId()).toUri();

        //Message msg = new Message(("payment was created with ID " + pagamento.getId()).getBytes());
       // rabbitTemplate.convertAndSend("pagamento.concluido", pagamento);
        rabbitTemplate.convertAndSend("pagamentos.exchange","", pagamento);

        return ResponseEntity.created(endereco).body(pagamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagamentoDTO> atualizar(@PathVariable @NotNull Long id,
                                                  @RequestBody @Valid PagamentoDTO dto) {
        PagamentoDTO atualizado = service.updatePayment(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PagamentoDTO> remover(@PathVariable @NotNull Long id) {
        service.removePayment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/confirmar")
   @CircuitBreaker(name = "atualizaPedido", fallbackMethod = "pagamentoAutorizadoComIntegacaoPendente")
    public void confirmPayment(@PathVariable @NotNull Long id){
        service.confirmPayment(id);

    }
    public void pagamentoAutorizadoComIntegracaoPendente(Long id, Exception e){
        service.alteraStatus(id);
    }

}