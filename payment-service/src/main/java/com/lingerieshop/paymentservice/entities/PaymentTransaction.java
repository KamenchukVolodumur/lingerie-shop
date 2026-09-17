package com.lingerieshop.paymentservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_transaction")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Ми не робимо тут @ManyToOne чи @JoinColumn,
    // бо замовлення лежать в іншій базі даних іншого мікросервісу.
    // Це просто число, за яким ми ідентифікуємо чек.
    @Column(nullable = false)
    private Long orderId;

    // Точна сума до оплати
    @Column(nullable = false)
    private BigDecimal amount;

    // Поточний стан грошей (чекають, заморожені, списані)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // Унікальний номер (invoiceId), який нам поверне Monobank.
    // Він критично потрібен, щоб потім сказати банку: "Спиши гроші саме за цим інвойсом".
    @Column(unique = true)
    private String externalBankReference;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Hibernate автоматично проставить час створення транзакції
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Hibernate автоматично оновить час при зміні статусу (наприклад, з HOLD на PAID)
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
