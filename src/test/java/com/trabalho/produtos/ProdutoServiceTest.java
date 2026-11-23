package com.trabalho.produtos;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {
    private static final ProdutoDados DADOS =
            new ProdutoDados("Caderno", "Capa dura", new BigDecimal("19.90"), 10);

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoService service;

    @Test
    void deveCadastrarProdutoValido() {
        // Arrange: simula o repositório devolvendo o produto que recebeu.
        when(repository.save(any(Produto.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        // Act: chama o serviço com os dados de um novo produto.
        Produto produto = service.cadastrar(DADOS);

        // Assert: confere o resultado e a chamada de persistência.
        assertEquals(DADOS.nome(), produto.getNome());
        assertEquals(DADOS.descricao(), produto.getDescricao());
        assertEquals(DADOS.preco(), produto.getPreco());
        assertEquals(DADOS.estoque(), produto.getEstoque());
        verify(repository).save(produto);
    }

    @Test
    void deveAtualizarProdutoExistente() {
        var produto = new Produto(DADOS);
        var novosDados = new ProdutoDados("Agenda", "Capa flexível", new BigDecimal("24.90"), 5);
        when(repository.findById(1L)).thenReturn(Optional.of(produto));

        Produto atualizado = service.atualizar(1L, novosDados);

        assertEquals(novosDados.nome(), atualizado.getNome());
        assertEquals(novosDados.preco(), atualizado.getPreco());
        verify(repository).findById(1L);
    }

    @Test
    void deveFalharAoBuscarProdutoInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        var erro = assertThrows(ProdutoNaoEncontradoException.class, () -> service.buscar(99L));

        assertTrue(erro.getMessage().contains("99"));
    }

    @Test
    void deveFalharAoExcluirProdutoInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProdutoNaoEncontradoException.class, () -> service.excluir(99L));

        verify(repository, never()).delete(any(Produto.class));
    }
}
