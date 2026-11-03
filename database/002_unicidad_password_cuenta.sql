-- Para una base existente: revisar previamente las contraseñas duplicadas.
-- La migración falla si ya hay duplicados; no elimina ni modifica cuentas.
ALTER TABLE cuentas_maestras
    ADD COLUMN password_huella BINARY(32)
        GENERATED ALWAYS AS (UNHEX(SHA2(password_cuenta, 256))) STORED,
    ADD CONSTRAINT uq_cuentas_password_huella UNIQUE (password_huella);
