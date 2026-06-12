package com.project.retailproject.clients;

import com.project.retailproject.dto.InvoiceResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "InvoiceClient", url = "${invoice.service.url}")
public interface InvoiceClient {
    @GetMapping("/api/invoices/{id}")
    InvoiceResponseDTO getInvoiceById(@PathVariable Long id);

    @PatchMapping("/api/invoices/{id}/status")
    void updateInvoiceStatus(@PathVariable("id") Long id, @RequestParam("status") String status);
}
