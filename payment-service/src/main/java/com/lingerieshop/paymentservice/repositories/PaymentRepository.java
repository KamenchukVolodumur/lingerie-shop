package com.lingerieshop.paymentservice.repositories;

import com.lingerieshop.paymentservice.entities.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentTransaction, String> {

    // Знайти транзакцію за нашим ID замовлення
    Optional<PaymentTransaction> findByOrderId(Long orderId);

    // ЗНАЙТИ ТРАНЗАКЦІЮ ЗА КЛЮЧОМ ВІД МОНОБАНКУ (критично важливо для вебхуків!)
    Optional<PaymentTransaction> findByExternalBankReference(String externalBankReference);
}