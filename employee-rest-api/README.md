# Employee REST API con Quarkus

Esta es una API REST para la gestión de datos de empleados, desarrollada con Java y Quarkus. El proyecto utiliza una arquitectura reactiva con Panache y se conecta a una base de datos PostgreSQL externa.

## Tecnologías Utilizadas

- **Framework**: Quarkus
- **Lenguaje**: Java
- **Base de Datos**: PostgreSQL
- **Migraciones**: Flyway
- **Build Tool**: Maven
- **Pruebas**: JUnit 5, REST Assured, Mockito

---

## Requisitos Previos

- JDK 17+
- Apache Maven 3.8+
- Docker o una instancia de PostgreSQL en ejecución.

---

## Configuración del Entorno

### 1. Configurar la Base de Datos PostgreSQL

El proyecto está configurado para conectarse a una base de datos PostgreSQL externa. Puedes iniciar una instancia usando Docker:

```bash
docker run --name quarkus-db -e POSTGRES_USER=mi_usuario -e POSTGRES_PASSWORD=mi_contraseña -e POSTGRES_DB=mi_base_de_datos -p 5432:5432 -d postgres
```

### 2. Configurar la Conexión en `application.yml`

Asegúrate de que tu archivo `src/main/resources/application.yml` contenga las credenciales correctas para la base de datos en el perfil de desarrollo.

```yaml
"%dev":
  quarkus:
    datasource:
      username: mi_usuario
      password: mi_contraseña
      jdbc:
        url: jdbc:postgresql://localhost:5432/mi_base_de_datos
    log:
      level: DEBUG
    # ... resto de la configuración
```

### 3. Ejecutar las Migraciones de Flyway

Las migraciones de la base de datos se gestionan con Flyway y se encuentran en `src/main/resources/db/migration`. Para aplicarlas, puedes ejecutar la aplicación en modo de desarrollo, ya que está configurado para migrar al inicio.

---

## Cómo Ejecutar la Aplicación

Para iniciar la aplicación en modo de desarrollo, ejecuta el siguiente comando:

```bash
# En Linux/macOS
./mvnw quarkus:dev

# En Windows
./mvnw.cmd quarkus:dev
```

Una vez iniciada, la API estará disponible en `http://localhost:8080`.

### Documentación de la API (Swagger UI)

Puedes acceder a la documentación interactiva de la API a través de Swagger UI en la siguiente URL:

- **Swagger UI**: http://localhost:8080/q/swagger-ui/

---

## Cómo Ejecutar las Pruebas

El proyecto incluye pruebas unitarias y de integración.

### Pruebas Unitarias y de Integración

Para ejecutar todas las pruebas (JVM mode), usa el siguiente comando:

```bash
./mvnw test
```

### Pruebas Nativas

Para ejecutar las pruebas en modo nativo (requiere GraalVM o un contenedor como Docker), empaqueta la aplicación con el perfil `native`:

```bash
./mvnw verify -Pnative
```

---