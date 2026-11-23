package com.trabalho.produtos;

public class ProdutoNaoEncontradoException extends RuntimeException {
    ProdutoNaoEncontradoException(long id) {
        super("Produto " + id + " não encontrado");
    }
}
