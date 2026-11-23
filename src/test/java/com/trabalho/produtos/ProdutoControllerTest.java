package com.trabalho.produtos;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdutoController.class)
class ProdutoControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProdutoService service;

    @Test
    void deveRetornarProdutoEmJson() throws Exception {
        var dados = new ProdutoDados("Caderno", "Capa dura", new BigDecimal("19.90"), 10);
        when(service.buscar(1L)).thenReturn(new Produto(dados));

        mvc.perform(get("/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Caderno"))
                .andExpect(jsonPath("$.estoque").value(10));
    }

    @Test
    void deveRetornar404ParaProdutoInexistente() throws Exception {
        when(service.buscar(99L)).thenThrow(new ProdutoNaoEncontradoException(99L));

        mvc.perform(get("/produtos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deveRejeitarCadastroInvalido() throws Exception {
        mvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
