package com.project.retailproject.dto;

import jakarta.validation.constraints.*;

public class PaymentRequestDTO {
    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;
    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount cannot be negative")
    private Double amount;
    @NotBlank(message = "Payment method is required")
    private String method;

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}
