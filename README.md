# 📦 ShipFlow

**ShipFlow** es un microservicio de gestión de envíos que permite registrar paquetes, cambiar sus estados mediante eventos (como `PENDING`, `IN_TRANSIT`, `DELIVERED`, etc.) y consultar el historial de movimientos de cada envío.

---

## 🚀 Tecnologías utilizadas

- Kotlin + Spring Boot
- PostgreSQL (Docker)
- JPA / Hibernate
- Postman (pruebas de API)
- Gradle (gestión de dependencias)

---

## 📁 Estructura del proyecto

```
├── controllers        # Endpoints REST para manejar envíos y eventos
├── services           # Lógica de negocio (creación, cambios de estado, validaciones)
├── repositories       # Acceso a datos (JPA)
├── models
│ ├── entities         # Entidades JPA (Package, PackageEvent, etc.)
│ ├── requests         # DTOs de entrada (CreatePackageRequest, etc.)
│ └── responses        # DTOs de salida (PackageResponse, etc.)
├── mappers            # Conversión entre entidades y DTOs
├── exceptions         # Excepciones personalizadas y su manejo
├── routes             # Definición centralizada de rutas del API
└── application.yml    # Configuración general del proyecto
```

---

## 📦 Endpoints principales

| Recurso      | Método  | Ruta                                         | Descripción                                |
|--------------|---------|----------------------------------------------|--------------------------------------------|
| **Paquetes** | GET     | `/api/shipflow/packages`                     | Obtener todos los paquetes                 |
|              | GET     | `/api/shipflow/packages/{trackingId}`        | Obtener un paquete por TrackingId          |
|              | POST    | `/api/shipflow/packages`                     | Registrar un nuevo paquete                 |
|              | PUT     | `/api/shipflow/packages/{trackingId}/status` | Actualizar el estado del paquete (evento)  |
|              | GET     | `/api/shipflow/packages/{trackingId}/events` | Obtener historial de eventos de un paquete |

---

## 🧪 Estados soportados

- `PENDING` → Estado inicial del paquete
- `IN_TRANSIT` → En camino
- `DELIVERED` → Entregado al destinatario
- `ON_HOLD` → En espera
- `CANCELLED` → Cancelado

Las transiciones entre estados están validadas para mantener la coherencia del flujo logístico.

---

## Paso 1: Clonar el repositorio

Clona este repositorio en tu máquina local con el siguiente comando:

```bash
git clone https://github.com/AlexanderPavon/pavon_alexander_shipflow.git
```

Luego navega a la carpeta del proyecto:

```bash
cd pavon_alexander_shipflow
```

---

## Paso 2: Configuración de la base de datos

El proyecto usa PostgreSQL dentro de un contenedor Docker. Puedes levantar la base de datos con:

```bash
docker-compose up -d
```
o para visualizar los logs

```bash
docker-compose up
```

---

## Paso 3: Cómo ejecutar el proyecto

```bash
./gradlew bootRun
```

---

## Paso 4: Pruebas con Postman

En el proyecto se incluye la colección `ShipFlow test.postman_collection.json`, la cual puedes importar en Postman. Esta colección contiene todas las peticiones necesarias para probar los endpoints del sistema: creación de envíos, cambio de estado y consulta del historial de eventos.

#### ⚠️ Importante 
En las rutas que contienen {trackingId}, debes reemplazar manualmente ese valor con el tracking ID real que se genera al crear un envío. Copia ese ID desde la respuesta del endpoint de creación y pégalo directamente en la URL del request correspondiente en Postman.

---

## ✅ Validaciones importantes

- ❌ No se permite registrar un paquete con la misma ciudad de origen y destino.
- ❌ La descripción no puede exceder los 50 caracteres.
- ✅ Todos los nombres de ciudad se guardan en mayúsculas.
- ✅ Las transiciones de estado están controladas por reglas estrictas.