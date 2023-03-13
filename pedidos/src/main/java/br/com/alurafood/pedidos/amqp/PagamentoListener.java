package br.com.alurafood.pedidos.amqp;

import br.com.alurafood.pedidos.dto.PagamentoDTO;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PagamentoListener {
    @RabbitListener(queues = "pagamentos.detalhes-pedido")//before:(queues = "pagamento.concluido")
    public void getMessage(PagamentoDTO pagamento){
        String msg = """
                Dados do pagamento: %s
                Númeero do pedido: %s
                Valor $: %s
                Status: %s
                """.formatted(pagamento.getId(),
                pagamento.getPedidoId(),
                pagamento.getValor(),
                pagamento.getStatus());
        System.out.println("Recebi a mensagem " + msg) ;
        //it'd better should be saved in db
    }
}
