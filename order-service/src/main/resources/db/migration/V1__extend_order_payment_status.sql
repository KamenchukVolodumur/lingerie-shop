DO $$
BEGIN
    IF to_regclass('public.orders') IS NOT NULL THEN
        ALTER TABLE orders
            DROP CONSTRAINT IF EXISTS orders_payment_status_check;

        ALTER TABLE orders
            ADD CONSTRAINT orders_payment_status_check
            CHECK (payment_status IN ('PENDING', 'RELEASE_PENDING', 'PAID', 'FAILED', 'CANCELED', 'REFUNDED'));
    END IF;
END $$;
