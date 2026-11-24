# Venus

Backend Java 17 con Maven, servidor HTTP embebido Javalin y MongoDB. El frontend existente se sirve desde el mismo servidor; no requiere NetBeans, Payara ni Oracle.

## Requisitos

- JDK 17 o posterior
- Maven 3.8+
- MongoDB en `mongodb://localhost:27017`

## Preparar la base de datos

Desde la raíz del proyecto, ejecutar en ese orden:

```powershell
mongosh --file script-Venus.js
mongosh --file insercion_NSQL.js
```

> `script-Venus.js` elimina y recrea las colecciones del proyecto. No lo ejecutes sobre una base que contenga datos que quieras conservar.

Los scripts crean la base `Nosql`, las colecciones, validadores, índices y datos iniciales. Para habilitar el login, generar un hash con el programa auxiliar:

```powershell
mvn compile exec:java "-Dexec.mainClass=com.mycompany.sarita.nosql.venus.security.GenerarAdmin"
```

Después, insertar el hash generado en `mongosh` (reemplace `HASH_GENERADO`):

```javascript
use Nosql
db.credenciales.insertOne({
  _id: 1,
  usuario: "admin",
  contrasena_hash: "HASH_GENERADO"
})
```

## Ejecutar

Desde la raíz del proyecto:

```powershell
mvn compile exec:java
```

Abra <http://localhost:8080>. En VS Code también puede abrir `src/main/java/com/mycompany/sarita/nosql/venus/VenusApplication.java` y usar **Run Java**.

Configuración opcional mediante variables de entorno:

- `MONGODB_URI`: URI de conexión; por defecto `mongodb://localhost:27017`.
- `PORT`: puerto HTTP; por defecto `8080`.

El servidor usa siempre la base de datos `Nosql`. Los documentos y tipos BSON de `empleados` siguen el validador de `script-Venus.js`; fechas son BSON Date, los IDs numéricos y los IDs de cursos son ObjectId.
