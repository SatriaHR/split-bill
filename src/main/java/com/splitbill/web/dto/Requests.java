package com.splitbill.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class Requests {
    private Requests() { }
    public record SignUp(@NotBlank @Size(max = 100) String username, @NotBlank @Size(min = 8, max = 200) String password) { }
    public record SignIn(@NotBlank String username, @NotBlank String password) { }
    public record Amount(@NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount) { }
    public record BillHeadCreate(@NotBlank @Size(max = 500) String description) { }
    public record BillHeadUpdate(@NotBlank @Size(max = 500) String description) { }
    public record BillDetailCreate(@NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal amountToPay, @NotNull Long userId) { }
}
