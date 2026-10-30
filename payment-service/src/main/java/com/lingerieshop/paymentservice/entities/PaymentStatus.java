package com.lingerieshop.paymentservice.entities;

public enum PaymentStatus {
    PENDING, // Посилання згенеровано, клієнт ще вводить дані картки
    HOLD,    // Monobank відповів: "Гроші на картці клієнта успішно заморожено"
    RELEASE_PENDING, // Замовлення скасовано; очікуємо webhook про розблокування hold
    PAID,    // Адмін підтвердив наявність, гроші остаточно списані (Capture)
    CANCELED // Monobank підтвердив, що платіж скасовано або hold розблоковано
}
