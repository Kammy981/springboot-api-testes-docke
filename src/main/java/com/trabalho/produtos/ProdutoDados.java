package com.trabalho.produtos;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record ProdutoDados(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Size(max = 500) String descricao,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal preco,
        @NotNull @PositiveOrZero Integer estoque) {}
