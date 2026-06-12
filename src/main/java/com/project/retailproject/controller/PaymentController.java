package com.project.retailproject.controller;

import com.project.retailproject.dto.PaymentRequestDTO;
import com.project.retailproject.dto.PaymentResponseDTO;
import com.project.retailproject.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")

public class PaymentController {

    @Autowired private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> processPayment(@RequestBody PaymentRequestDTO dto) {
        return ResponseEntity.ok(paymentService.insertPayment(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> updatePayment(@PathVariable Long id, @RequestBody PaymentRequestDTO dto) {
        return ResponseEntity.ok(paymentService.updatePayment(id, dto));
    }

    @PatchMapping("/{id}/refund")
    public ResponseEntity<Void> refundPayment(@PathVariable Long id) {
        paymentService.refundPayment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<PaymentResponseDTO>> getByInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getByInvoice(invoiceId));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<PaymentResponseDTO>> getPaginated(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getAllPaymentsPaginated(pageable));
    }
}
