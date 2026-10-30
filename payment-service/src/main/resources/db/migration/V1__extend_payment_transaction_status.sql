DO $$
BEGIN
    IF to_regclass('public.payment_transaction') IS NOT NULL THEN
        ALTER TABLE payment_transaction
            DROP CONSTRAINT IF EXISTS payment_transaction_status_check;

        ALTER TABLE payment_transaction
            ADD CONSTRAINT payment_transaction_status_check
            CHECK (status IN ('PENDING', 'HOLD', 'RELEASE_PENDING', 'PAID', 'CANCELED'));
    END IF;
END $$;
