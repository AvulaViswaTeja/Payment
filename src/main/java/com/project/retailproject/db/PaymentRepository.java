package com.project.retailproject.db;

import com.project.retailproject.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceId(Long invoiceId);
    List<Payment> findByStatus(String status);
    List<Payment> findByMethod(String method);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.invoiceId = :invoiceId AND p.status = 'SUCCESS'")
    Double sumSuccessfulPaymentsByInvoiceId(@Param("invoiceId") Long invoiceId);
}
