CREATE DATABASE IF NOT EXISTS cinepass_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cinepass_db;

CREATE TABLE roles (
    id_rol INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL,
    CONSTRAINT uq_roles_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE usuarios (
    id_usuario BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_rol INT UNSIGNED NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(20) NULL,
    estado ENUM('ACTIVO', 'INACTIVO', 'SUSPENDIDO') NOT NULL DEFAULT 'ACTIVO',
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuarios_correo UNIQUE (correo),
    CONSTRAINT fk_usuarios_roles
        FOREIGN KEY (id_rol) REFERENCES roles (id_rol)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_usuarios_rol_estado (id_rol, estado)
) ENGINE=InnoDB;

CREATE TABLE plataformas (
    id_plataforma INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    descripcion VARCHAR(500) NULL,
    logo_url VARCHAR(500) NULL,
    estado ENUM('ACTIVA', 'INACTIVA') NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT uq_plataformas_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE cuentas_maestras (
    id_cuenta_maestra BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_plataforma INT UNSIGNED NOT NULL,
    correo_cuenta VARCHAR(150) NOT NULL,
    password_cuenta VARCHAR(255) NOT NULL,
    password_huella BINARY(32) GENERATED ALWAYS AS (UNHEX(SHA2(password_cuenta, 256))) STORED,
    tipo_plan VARCHAR(80) NULL,
    fecha_adquisicion DATE NULL,
    fecha_vencimiento DATE NULL,
    costo DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    estado ENUM('ACTIVA', 'INACTIVA', 'VENCIDA') NOT NULL DEFAULT 'ACTIVA',
    observaciones VARCHAR(500) NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cuentas_password_huella UNIQUE (password_huella),
    CONSTRAINT fk_cuentas_plataformas
        FOREIGN KEY (id_plataforma) REFERENCES plataformas (id_plataforma)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_cuentas_costo CHECK (costo >= 0),
    CONSTRAINT chk_cuentas_fechas CHECK (
        fecha_adquisicion IS NULL
        OR fecha_vencimiento IS NULL
        OR fecha_vencimiento >= fecha_adquisicion
    ),
    INDEX idx_cuentas_plataforma_estado (id_plataforma, estado)
) ENGINE=InnoDB;

CREATE TABLE perfiles (
    id_perfil BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_cuenta_maestra BIGINT UNSIGNED NOT NULL,
    nombre_perfil VARCHAR(80) NOT NULL,
    pin_acceso VARCHAR(100) NULL,
    precio_venta DECIMAL(10, 2) NOT NULL,
    estado ENUM('DISPONIBLE', 'RESERVADO', 'VENDIDO') NOT NULL DEFAULT 'DISPONIBLE',
    fecha_reserva DATETIME NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_perfil_cuenta_nombre UNIQUE (id_cuenta_maestra, nombre_perfil),
    CONSTRAINT fk_perfiles_cuentas
        FOREIGN KEY (id_cuenta_maestra) REFERENCES cuentas_maestras (id_cuenta_maestra)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_perfiles_precio CHECK (precio_venta >= 0),
    INDEX idx_perfiles_estado (estado),
    INDEX idx_perfiles_cuenta_estado (id_cuenta_maestra, estado)
) ENGINE=InnoDB;

CREATE TABLE pedidos (
    id_pedido BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT UNSIGNED NOT NULL,
    id_perfil BIGINT UNSIGNED NOT NULL,
    codigo_pedido VARCHAR(40) NOT NULL,
    monto_total DECIMAL(10, 2) NOT NULL,
    estado ENUM(
        'RESERVADA',
        'EN_VALIDACION',
        'ACTIVA',
        'VENCIDA',
        'RECHAZADA',
        'CANCELADA'
    ) NOT NULL DEFAULT 'RESERVADA',
    fecha_pedido TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_limite_reserva DATETIME NULL,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_pedidos_codigo UNIQUE (codigo_pedido),
    CONSTRAINT fk_pedidos_usuarios
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_pedidos_perfiles
        FOREIGN KEY (id_perfil) REFERENCES perfiles (id_perfil)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_pedidos_monto CHECK (monto_total >= 0),
    INDEX idx_pedidos_usuario_fecha (id_usuario, fecha_pedido),
    INDEX idx_pedidos_perfil_estado (id_perfil, estado),
    INDEX idx_pedidos_estado_fecha (estado, fecha_pedido)
) ENGINE=InnoDB;

CREATE TABLE transacciones (
    id_transaccion BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_pedido BIGINT UNSIGNED NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL,
    numero_operacion VARCHAR(100) NOT NULL,
    titular VARCHAR(150) NOT NULL,
    monto DECIMAL(10, 2) NOT NULL,
    estado ENUM(
        'PENDIENTE',
        'EN_VALIDACION',
        'APROBADA',
        'RECHAZADA',
        'CANCELADA'
    ) NOT NULL DEFAULT 'PENDIENTE',
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_transaccion_metodo_operacion UNIQUE (metodo_pago, numero_operacion),
    CONSTRAINT fk_transacciones_pedidos
        FOREIGN KEY (id_pedido) REFERENCES pedidos (id_pedido)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_transacciones_monto CHECK (monto >= 0),
    INDEX idx_transacciones_pedido_fecha (id_pedido, fecha_registro),
    INDEX idx_transacciones_estado_fecha (estado, fecha_registro)
) ENGINE=InnoDB;

CREATE TABLE vouchers (
    id_voucher BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_transaccion BIGINT UNSIGNED NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_archivo VARCHAR(1000) NOT NULL,
    tipo_mime VARCHAR(100) NOT NULL,
    tamano_bytes BIGINT UNSIGNED NOT NULL,
    sha256 CHAR(64) NULL,
    estado ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO', 'OBSERVADO')
        NOT NULL DEFAULT 'PENDIENTE',
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_vouchers_transaccion UNIQUE (id_transaccion),
    CONSTRAINT fk_vouchers_transacciones
        FOREIGN KEY (id_transaccion) REFERENCES transacciones (id_transaccion)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_vouchers_tamano CHECK (tamano_bytes > 0),
    INDEX idx_vouchers_estado_fecha (estado, fecha_registro)
) ENGINE=InnoDB;

CREATE TABLE auditoria_vouchers (
    id_auditoria BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_voucher BIGINT UNSIGNED NOT NULL,
    id_usuario_actor BIGINT UNSIGNED NULL,
    accion ENUM('CREADO', 'ACTUALIZADO') NOT NULL,
    estado_anterior ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO', 'OBSERVADO') NULL,
    estado_nuevo ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO', 'OBSERVADO') NOT NULL,
    detalle VARCHAR(500) NOT NULL,
    fecha_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auditoria_vouchers_voucher
        FOREIGN KEY (id_voucher) REFERENCES vouchers (id_voucher)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_auditoria_vouchers_usuario
        FOREIGN KEY (id_usuario_actor) REFERENCES usuarios (id_usuario)
        ON UPDATE CASCADE ON DELETE SET NULL,
    INDEX idx_auditoria_voucher_fecha (id_voucher, fecha_evento),
    INDEX idx_auditoria_usuario_fecha (id_usuario_actor, fecha_evento)
) ENGINE=InnoDB;

INSERT IGNORE INTO roles (nombre) VALUES ('ADMINISTRADOR'), ('CLIENTE');
INSERT IGNORE INTO plataformas (nombre) VALUES ('Netflix'), ('Max (HBO)'), ('Disney+');

DROP TRIGGER IF EXISTS trg_vouchers_auditoria_insert;
DROP TRIGGER IF EXISTS trg_vouchers_auditoria_update;

DELIMITER //

CREATE TRIGGER trg_vouchers_auditoria_insert
AFTER INSERT ON vouchers
FOR EACH ROW
BEGIN
    INSERT INTO auditoria_vouchers (
        id_voucher,
        accion,
        estado_anterior,
        estado_nuevo,
        detalle
    ) VALUES (
        NEW.id_voucher,
        'CREADO',
        NULL,
        NEW.estado,
        'Registro de voucher creado'
    );
END//

CREATE TRIGGER trg_vouchers_auditoria_update
AFTER UPDATE ON vouchers
FOR EACH ROW
BEGIN
    IF NOT (OLD.estado <=> NEW.estado)
       OR NOT (OLD.nombre_archivo <=> NEW.nombre_archivo)
       OR NOT (OLD.ruta_archivo <=> NEW.ruta_archivo)
       OR NOT (OLD.tipo_mime <=> NEW.tipo_mime)
       OR NOT (OLD.tamano_bytes <=> NEW.tamano_bytes)
       OR NOT (OLD.sha256 <=> NEW.sha256) THEN
        INSERT INTO auditoria_vouchers (
            id_voucher,
            accion,
            estado_anterior,
            estado_nuevo,
            detalle
        ) VALUES (
            NEW.id_voucher,
            'ACTUALIZADO',
            OLD.estado,
            NEW.estado,
            'Se modificó el estado o los datos del archivo'
        );
    END IF;
END//

DELIMITER ;
