# Employee REST API

Una API REST desarrollada con Quarkus para la gestión de empleados. Proporciona operaciones CRUD (Crear, Leer, Actualizar, Eliminar) para entidades de empleados con persistencia en PostgreSQL.

## 🚀 Tecnologías

- Java 21
- [Quarkus](https://quarkus.io/) 3.28.5
- PostgreSQL
- Flyway para migraciones de base de datos
- MapStruct para mapeo de objetos
- Lombok para reducción de código boilerplate
- OpenAPI (Swagger) para documentación
- Docker para contenerización

## ✨ Características

- Operaciones CRUD completas para empleados
- Validación de datos con Hibernate Validator
- Documentación de API con OpenAPI/Swagger
- Migraciones de base de datos automáticas con Flyway
- Soporte para construcción nativa con GraalVM

## ✅ Requisitos Previos

- JDK 21
- Maven 3.9+
- PostgreSQL 
- Docker (opcional)

## Configuración

1. Crear la base de datos PostgreSQL:

```sql
CREATE DATABASE employees_db;
```

2. Configurar las credenciales de la base de datos en `src/main/resources/application.yml`:

```yaml
quarkus:
  datasource:
    db-kind: postgresql
    username: tu_usuario
    password: tu_password
    jdbc:
      url: jdbc:postgresql://localhost:5432/employees_db
```

## Ejecutar la Aplicación

### Modo Desarrollo

```bash
./mvnw quarkus:dev
```

La aplicación estará disponible en http://localhost:8080
La interfaz Swagger UI estará en http://localhost:8080/q/swagger-ui
La interfaz Dev UI estará disponible en http://localhost:8080/q/dev/

### Construir y Ejecutar en Producción

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Construir Über-jar

```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
java -jar target/*-runner.jar
```

### Construir Imagen Nativa

```bash
./mvnw package -Dnative
```

O si no tienes GraalVM instalado:

```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

Ejecutar el ejecutable nativo:
```bash
./target/employee-rest-api-1.0.0-SNAPSHOT-runner
```

### Docker

Construir imagen:

```bash
docker build -f src/main/docker/Dockerfile.jvm -t employee-rest-api:latest .
```

Ejecutar contenedor:

```bash
docker run -i --rm -p 8080:8080 employee-rest-api:latest
```

## API Endpoints

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/v1/employees` | Obtener todos los empleados |
| GET | `/api/v1/employees/{id}` | Obtener empleado por ID |
| POST | `/api/v1/employees` | Crear nuevo empleado |
| PUT | `/api/v1/employees` | Actualizar empleado existente |
| DELETE | `/api/v1/employees/{id}` | Eliminar empleado |

## Estructura del Proyecto

```
src/
├── main/
│   ├── docker/           # Archivos Dockerfile
│   ├── java/            # Código fuente Java
│   │   └── dev/demo/employee/
│   │       ├── Controller/  # Controladores REST
│   │       ├── Entity/      # Entidades JPA
│   │       ├── Mappers/     # Mappers MapStruct
│   │       ├── Model/       # DTOs
│   │       ├── Repository/  # Repositorios
│   │       └── Service/     # Lógica de negocio
│   └── resources/
│       ├── application.yml  # Configuración
│       └── db/migration/    # Scripts Flyway
└── test/                    # Tests
```

## Guías Relacionadas

- [Hibernate ORM con Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [REST](https://quarkus.io/guides/rest)
- [REST Jackson](https://quarkus.io/guides/rest#json-serialisation)
- [Hibernate Validator](https://quarkus.io/guides/validation)
- [Flyway](https://quarkus.io/guides/flyway)
- [SmallRye OpenAPI](https://quarkus.io/guides/openapi-swaggerui)
- [JDBC PostgreSQL](https://quarkus.io/guides/datasource)
- [Configuración YAML](https://quarkus.io/guides/config-yaml)

## Contribuir

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo [LICENSE.md](LICENSE.md) para más detalles.

## ENG ##

# Employee REST API

A REST API developed with Quarkus for employee management. Provides CRUD operations (Create, Read, Update, Delete) for employee entities with PostgreSQL persistence.

## Technologies

- Java 21
- [Quarkus](https://quarkus.io/) 3.28.5
- PostgreSQL
- Flyway for database migrations
- MapStruct for object mapping
- Lombok for boilerplate code reduction
- OpenAPI (Swagger) for documentation
- Docker for containerization

## Features

- Complete CRUD operations for employees
- Data validation with Hibernate Validator
- API documentation with OpenAPI/Swagger
- Automatic database migrations with Flyway
- Native build support with GraalVM

## Prerequisites

- JDK 21
- Maven 3.9+
- PostgreSQL 
- Docker (optional)

## Setup

1. Create PostgreSQL database:

```sql
CREATE DATABASE employees_db;
```

2. Configure database credentials in `src/main/resources/application.yml`:

```yaml
quarkus:
  datasource:
    db-kind: postgresql
    username: your_username
    password: your_password
    jdbc:
      url: jdbc:postgresql://localhost:5432/employees_db
```

## Running the Application

### Development Mode

```bash
./mvnw quarkus:dev
```

The application will be available at http://localhost:8080
Swagger UI interface will be at http://localhost:8080/q/swagger-ui
Dev UI interface will be available at http://localhost:8080/q/dev/

### Build and Run for Production

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Build Über-jar

```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
java -jar target/*-runner.jar
```

### Build Native Image

```bash
./mvnw package -Dnative
```

Or if you don't have GraalVM installed:

```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

Run the native executable:
```bash
./target/employee-rest-api-1.0.0-SNAPSHOT-runner
```

### Docker

Build image:

```bash
docker build -f src/main/docker/Dockerfile.jvm -t employee-rest-api:latest .
```

Run container:

```bash
docker run -i --rm -p 8080:8080 employee-rest-api:latest
```

## API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/v1/employees` | Get all employees |
| GET | `/api/v1/employees/{id}` | Get employee by ID |
| POST | `/api/v1/employees` | Create new employee |
| PUT | `/api/v1/employees` | Update existing employee |
| DELETE | `/api/v1/employees/{id}` | Delete employee |

## Project Structure

```
src/
├── main/
│   ├── docker/           # Dockerfile files
│   ├── java/            # Java source code
│   │   └── dev/demo/employee/
│   │       ├── Controller/  # REST Controllers
│   │       ├── Entity/      # JPA Entities
│   │       ├── Mappers/     # MapStruct Mappers
│   │       ├── Model/       # DTOs
│   │       ├── Repository/  # Repositories
│   │       └── Service/     # Business Logic
│   └── resources/
│       ├── application.yml  # Configuration
│       └── db/migration/    # Flyway Scripts
└── test/                    # Tests
```

## Related Guides

- [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [REST](https://quarkus.io/guides/rest)
- [REST Jackson](https://quarkus.io/guides/rest#json-serialisation)
- [Hibernate Validator](https://quarkus.io/guides/validation)
- [Flyway](https://quarkus.io/guides/flyway)
- [SmallRye OpenAPI](https://quarkus.io/guides/openapi-swaggerui)
- [JDBC PostgreSQL](https://quarkus.io/guides/datasource)
- [YAML Configuration](https://quarkus.io/guides/config-yaml)

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details