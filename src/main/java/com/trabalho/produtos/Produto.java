package com.trabalho.produtos;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false, length = 500)
    private String descricao;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;
    @Column(nullable = false)
    private int estoque;

    protected Produto() {}

    Produto(ProdutoDados dados) {
        atualizar(dados);
    }

    void atualizar(ProdutoDados dados) {
        nome = dados.nome();
        descricao = dados.descricao();
        preco = dados.preco();
        estoque = dados.estoque();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPreco() { return preco; }
    public int getEstoque() { return estoque; }
}
