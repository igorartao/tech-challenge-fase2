package com.challange.lanchonete.application.usecase;

import com.challange.lanchonete.domain.*;
import com.challange.lanchonete.domain.exception.ResourceNotFoundException;
import com.challange.lanchonete.infrastructure.repository.ClienteRepository;
import com.challange.lanchonete.infrastructure.repository.PedidoRepository;
import com.challange.lanchonete.infrastructure.repository.ProdutoPedidoRepository;
import com.challange.lanchonete.infrastructure.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CriarPedidoUseCase implements ICriarPedidoUseCase {

    private static final Logger log = LoggerFactory.getLogger(CriarPedidoUseCase.class);

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoPedidoRepository produtoPedidoRepository;

    @Autowired
    public CriarPedidoUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public Pedido execute(PedidoRequest pedidoRequest) {
        Pedido pedido = new Pedido();

        Cliente cliente = clienteRepository.findById(pedidoRequest.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.RECEBIDO);
        pedido.setProdutos(new ArrayList<>());

        pedido = pedidoRepository.save(pedido);

        boolean produtosEncontrados = false;

        for (Long produtoId : pedidoRequest.getProdutoIds()) {
            Optional<Produto> produtoOptional = produtoRepository.findById(produtoId);

            if (!produtoOptional.isPresent()) {
                log.info("Produto não encontrado");
                continue;
            }

            ProdutoPedido produtoPedido = new ProdutoPedido();
            produtoPedido.setPedido(pedido);
            produtoPedido.setProduto(produtoOptional.get());
            produtoPedido.setQuantidade(1);

            produtoPedidoRepository.save(produtoPedido);
            produtosEncontrados = true;
            pedido.getProdutos().add(produtoOptional.get());
        }

        if (!produtosEncontrados) {
            pedidoRepository.delete(pedido);
            throw new ResourceNotFoundException("Nenhum produto encontrado");
        }

        return pedido;
    }
}