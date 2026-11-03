# CinePass Web

CinePass es un MVP para ofrecer perfiles de plataformas de streaming. Un cliente puede registrarse, reservar un perfil durante 15 minutos y subir un comprobante de pago. Un administrador revisa el comprobante y, al aprobarlo, el cliente puede consultar los datos de acceso en **Mis pedidos**.

El administrador también puede crear cuentas maestras y perfiles desde el panel de inventario. Los pagos se validan manualmente: la aplicación no procesa transferencias ni verifica automáticamente los pagos de Yape o Plin.

## Tecnologías

- Java 17 y Spring Boot 4.1.2.
- Spring MVC, Spring Data JPA y Hibernate.
- Thymeleaf, HTML, CSS, JavaScript y Bootstrap.
- MariaDB de XAMPP y MySQL Connector/J.
- Maven Wrapper, Lombok y BCrypt mediante Spring Security Crypto.

La aplicación utiliza el patrón MVC: las entidades representan las tablas, los repositorios acceden a los datos, los servicios contienen la lógica, los controladores manejan las peticiones y Thymeleaf genera las vistas.

## Requisitos

- JDK 17 o superior. Comprueba la instalación con `java -version`.
- XAMPP con MariaDB/MySQL. La configuración incluida utiliza el dialecto de **MariaDB**, que es el motor usado en la instalación de XAMPP con la que se probó el proyecto.
- Conexión a Internet la primera vez para descargar las dependencias Maven.

No es necesario instalar Maven por separado: el proyecto incluye Maven Wrapper.

## Preparar la base de datos

1. Inicia **MySQL** y **Apache** desde XAMPP Control Panel.
2. Abre `http://localhost/phpmyadmin`.
3. En **Importar**, selecciona `database/cinepass.sql` y ejecuta la importación. El script crea la base `cinepass_db`, las tablas, los roles `ADMINISTRADOR` y `CLIENTE`, las plataformas iniciales y los disparadores de auditoría.

Para una base ya existente creada antes de incorporar la unicidad de contraseñas, revisa `database/002_unicidad_password_cuenta.sql`. Esta migración debe aplicarse una sola vez y fallará si ya existen contraseñas duplicadas o si la columna ya existe. No es necesaria en una base nueva importada desde el script actual.

## Configurar la conexión

Revisa `src/main/resources/application.properties` y ajusta los datos de tu instalación. Para una instalación local habitual de XAMPP:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cinepass_db
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect
```

Si tu usuario tiene contraseña o tu servidor utiliza otro puerto, cambia los valores correspondientes. No subas contraseñas reales al repositorio. La opción `ddl-auto=update` está pensada para desarrollo; el script SQL sigue siendo necesario para crear los datos iniciales y los disparadores.

## Ejecutar la aplicación

En una terminal situada en la raíz del proyecto, ejecuta:

**Windows:**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux o macOS:**

```bash
./mvnw spring-boot:run
```

Cuando aparezca `Started CinepassWebApplication`, abre `http://localhost:8080`. Para detener la aplicación, pulsa `Ctrl+C` en la terminal.

## Preparar un administrador

1. Abre `http://localhost:8080/registro` y registra una cuenta con un correo y una contraseña propios. El registro crea un usuario con rol `CLIENTE` y guarda la contraseña como hash BCrypt.
2. En phpMyAdmin, selecciona `cinepass_db` y ejecuta la siguiente consulta, sustituyendo el correo por el que registraste:

```sql
UPDATE usuarios
SET id_rol = (SELECT id_rol FROM roles WHERE nombre = 'ADMINISTRADOR')
WHERE correo = 'tu-correo@ejemplo.com';
```

3. Entra a `http://localhost:8080/admin/login` con ese correo y contraseña. Desde **Gestionar inventario**, crea una cuenta maestra y después un perfil con su precio. Usa datos ficticios para la demostración.

## Probar el recorrido

1. Registra una cuenta de cliente distinta a la del administrador e inicia sesión en `http://localhost:8080/login`.
2. En el catálogo, reserva un perfil disponible. La reserva dura 15 minutos.
3. En el checkout, registra el método de pago y sube un comprobante de prueba PDF, PNG o JPEG de hasta 5 MB.
4. Inicia sesión como administrador en otra sesión del navegador. En el panel, descarga y revisa el comprobante; después apruébalo o recházalo.
5. Si se aprueba, el cliente puede consultar sus credenciales en **Mis pedidos**. Las reservas que vencen sin enviar comprobante se liberan mediante una tarea programada.

Los comprobantes se guardan en la carpeta local `uploads/comprobantes`, relativa al directorio desde el que se inicia la aplicación. Conserva esa carpeta junto con la base de datos si necesitas mantener los archivos de prueba.

## Estructura principal

```text
database/                       Script de base de datos y migración
src/main/java/com/cinepass/app/
  config/                       Configuración web
  controller/                   Controladores MVC
  dto/                          Datos de entrada y salida
  model/entity/                 Entidades JPA
  repository/                   Acceso a datos
  service/                      Lógica de negocio
src/main/resources/
  templates/                    Vistas Thymeleaf
  static/                       CSS y JavaScript
  application.properties        Configuración local
```

## Estado del MVP

El recorrido principal se probó localmente con datos ficticios. La creación de inventario está disponible en el panel; la edición y eliminación todavía no están implementadas. El sistema guarda las contraseñas de cuentas maestras de forma recuperable para entregarlas al cliente; antes de usar credenciales reales o publicar el sistema, se debe reforzar su almacenamiento y revisar la seguridad del despliegue.
