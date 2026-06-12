package com.project.retailproject.service;

import com.project.retailproject.clients.AuditLogClient;
import com.project.retailproject.clients.InvoiceClient;
import com.project.retailproject.db.PaymentRepository;
import com.project.retailproject.dto.*;
import com.project.retailproject.exception.BadRequestException;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private InvoiceClient invoiceClient;
    @Autowired private AuditLogClient auditLogClient;

    private void log(String action) {
        try { auditLogClient.log(new AuditLogRequestDTO(action)); }
        catch (Exception e) { System.err.println("AuditLog failed: " + e.getMessage()); }
    }

    @Transactional
    public PaymentResponseDTO insertPayment(PaymentRequestDTO dto) {
        InvoiceResponseDTO invoice;
        try { invoice = invoiceClient.getInvoiceById(dto.getInvoiceId()); }
        catch (Exception e) {
            throw new ResourceNotFoundException("Invoice not found with ID: " + dto.getInvoiceId());
        }

        if ("CANCELLED".equals(invoice.getStatus())) {
            log("Payment.PROCESS_FAILED | CANCELLED InvoiceID: " + dto.getInvoiceId());
            throw new BadRequestException("Cannot pay a cancelled invoice");
        }
        if ("PAID".equals(invoice.getStatus())) {
            log("Payment.PROCESS_FAILED | Already PAID InvoiceID: " + dto.getInvoiceId());
            throw new BadRequestException("Invoice is already fully paid");
        }

        Payment payment = new Payment();
        payment.setInvoiceId(dto.getInvoiceId());
        payment.setAmount(dto.getAmount());
        payment.setMethod(dto.getMethod());
        payment.setDate(LocalDate.now());
        payment.setStatus("SUCCESS");
        Payment saved = paymentRepository.save(payment);

        Double totalPaid = paymentRepository.sumSuccessfulPaymentsByInvoiceId(dto.getInvoiceId());
        if (totalPaid == null) totalPaid = 0.0;

        String newInvoiceStatus;
        if (totalPaid >= invoice.getAmount()) newInvoiceStatus = "PAID";
        else if (totalPaid > 0) newInvoiceStatus = "PARTIALLY_PAID";
        else newInvoiceStatus = invoice.getStatus();

        try {
            invoiceClient.updateInvoiceStatus(dto.getInvoiceId(), newInvoiceStatus);
        }
        catch (Exception e) {
//            e.printStackTrace();
            log("Invoice.STATUS_UPDATE_FAILED | InvoiceID: " + dto.getInvoiceId());
        }

        log("Payment.PROCESS_SUCCESS | PaymentID: " + saved.getPaymentId()
                + " | InvoiceID: " + dto.getInvoiceId()
                + " | Amount: " + dto.getAmount()
                + " | Method: " + dto.getMethod()
                + " | TotalPaid: " + totalPaid
                + " | InvoiceStatus: " + newInvoiceStatus);

        return mapToDTO(saved, newInvoiceStatus);
    }

    public PaymentResponseDTO updatePayment(Long id, PaymentRequestDTO dto) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        String oldMethod = payment.getMethod();
        payment.setMethod(dto.getMethod());
        Payment saved = paymentRepository.save(payment);
        log("Payment.UPDATE_SUCCESS | PaymentID: " + id + " | Method: " + oldMethod + " -> " + dto.getMethod());
        return mapToDTO(saved, null);
    }

    @Transactional
    public void refundPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        if ("REFUNDED".equals(payment.getStatus())) {
            throw new BadRequestException("Payment is already refunded");
        }
        payment.setStatus("REFUNDED");
        paymentRepository.save(payment);

        Double totalPaid = paymentRepository.sumSuccessfulPaymentsByInvoiceId(payment.getInvoiceId());
        if (totalPaid == null) totalPaid = 0.0;

        Double invoiceAmount = 0.0;
        try { invoiceAmount = invoiceClient.getInvoiceById(payment.getInvoiceId()).getAmount(); }
        catch (Exception e) {}

        String newInvoiceStatus;
        if (totalPaid <= 0) newInvoiceStatus = "PENDING";
        else if (totalPaid < invoiceAmount) newInvoiceStatus = "PARTIALLY_PAID";
        else newInvoiceStatus = "PAID";

        try { invoiceClient.updateInvoiceStatus(payment.getInvoiceId(), newInvoiceStatus); }
        catch (Exception e) {}

        log("Payment.REFUND_SUCCESS | PaymentID: " + id
                + " | InvoiceID: " + payment.getInvoiceId()
                + " | Amount: " + payment.getAmount()
                + " | Status: REFUNDED"
                + " | InvoiceStatus: " + newInvoiceStatus);
    }

    public PaymentResponseDTO getPayment(Long id) {
        return mapToDTO(paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id)), null);
    }

    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll().stream().map(p -> mapToDTO(p, null)).collect(Collectors.toList());
    }

    public List<PaymentResponseDTO> getByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream().map(p -> mapToDTO(p, null)).collect(Collectors.toList());
    }

    public Page<PaymentResponseDTO> getAllPaymentsPaginated(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(p -> mapToDTO(p, null));
    }

    private PaymentResponseDTO mapToDTO(Payment p, String invoiceStatus) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setPaymentId(p.getPaymentId());
        dto.setInvoiceId(p.getInvoiceId());
        dto.setAmount(p.getAmount());
        dto.setDate(p.getDate());
        dto.setMethod(p.getMethod());
        dto.setStatus(p.getStatus());
        dto.setInvoiceStatus(invoiceStatus);
        return dto;
    }
}
