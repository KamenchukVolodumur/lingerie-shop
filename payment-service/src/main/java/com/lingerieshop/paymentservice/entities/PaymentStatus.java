package com.lingerieshop.paymentservice.entities;

public enum PaymentStatus {
    PENDING, // Посилання згенеровано, клієнт ще вводить дані картки
    HOLD,    // Monobank відповів: "Гроші на картці клієнта успішно заморожено"
    PAID,    // Адмін підтвердив наявність, гроші остаточно списані (Capture)
    CANCELED // Товару немає, гроші миттєво розморожено (Void)
}

