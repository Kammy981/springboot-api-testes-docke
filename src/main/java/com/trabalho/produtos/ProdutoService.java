package com.trabalho.produtos;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProdutoService {
    private static final Logger LOG = LoggerFactory.getLogger(ProdutoService.class);
    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public Produto cadastrar(ProdutoDados dados) {
        Produto produto = repository.save(new Produto(dados));
        LOG.info("Cadastro de produto solicitado para ID {}", produto.getId());
        return produto;
    }

    @Transactional(readOnly = true)
    public List<Produto> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Produto buscar(long id) {
        return repository.findById(id).orElseThrow(() -> new ProdutoNaoEncontradoException(id));
    }

    public Produto atualizar(long id, ProdutoDados dados) {
        Produto produto = buscar(id);
        produto.atualizar(dados);
        LOG.info("Atualização de produto solicitada para ID {}", id);
        return produto;
    }

    public void excluir(long id) {
        repository.delete(buscar(id));
        LOG.info("Exclusão de produto solicitada para ID {}", id);
    }
}
