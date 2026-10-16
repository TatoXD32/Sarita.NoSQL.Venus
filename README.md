# Venus — Sistema de Recursos Humanos

Aplicación web para la administración de empleados, domicilios, departamentos, puestos, catálogos y cursos realizados por empleados. Desarrollada con **Java 17 (compilación), Jakarta EE 10, JAX-RS, JPA/EclipseLink, Oracle Database y Payara Server**.

> **Estado actual:** la base Oracle local utiliza las 10 tablas funcionales del modelo actualizado. La aplicación se ha compilado y desplegado en Payara. Falta probar los scripts SQL en un esquema nuevo y completar la autenticación real del backend.

## 1. Requisitos

| Componente | Requisito / referencia |
| --- | --- |
| Java | JDK 17 o superior; el proyecto compila para Java 17 |
| Maven | Compatible con el `pom.xml` del proyecto (se ha utilizado Maven 3.8.7) |
| Jakarta EE | 10 |
| Payara | Payara Server 6 en el entorno Docker probado; verificar compatibilidad con la instalación propia |
| Oracle Database | Oracle Free 23 en el entorno Docker del proyecto; scripts con sintaxis Oracle |
| Editor | VS Code o NetBeans |
| Driver JDBC | `ojdbc11` según `pom.xml` |

Verifica las herramientas:

```bash
java -version
mvn -version
```

## 2. Obtener el proyecto

```bash
git clone https://github.com/TatoXD32/Sarita.NoSQL.Venus.git
cd Sarita.NoSQL.Venus
git status
```

Abre la carpeta que contiene `pom.xml` en VS Code o NetBeans. Cada integrante debe configurar sus propias rutas, conexiones y credenciales.

## 3. Modelo de base de datos corregido

El modelo se reorganizó para cumplir las observaciones de diseño:

1. **Sin script de borrado:** no se incluye un procedimiento de `DROP TABLE` como parte de la instalación normal.
2. **Estatus específicos:** se elimina el catálogo general `CAT_ESTATUS`. `ESTATUS_EMPLEADOS` contiene únicamente los estados de empleados; otros catálogos no dependen de un estatus genérico.
3. **Relación empleado–domicilio:** `DOMICILIOS.ID_EMPLEADO` es a la vez clave primaria y foránea hacia `EMPLEADOS.ID_EMPLEADO`. Un empleado puede tener como máximo un domicilio registrado en esta tabla.
4. **Tablas en plural:** los nombres siguen una convención uniforme.
5. **Restricciones:** se utilizan `PRIMARY KEY`, `FOREIGN KEY`, `UNIQUE`, `NOT NULL` y `CHECK` según corresponda.
6. **Sin identificadores innecesarios:** `DOMICILIOS` no tiene `ID_DOMICILIO`; utiliza `ID_EMPLEADO`. `EMPLEADO_CURSOS` conserva su identificador porque un empleado puede realizar varios cursos, incluso repetir uno.
7. **Cursos sin catálogo:** los cursos realizados se guardan directamente en `EMPLEADO_CURSOS`, con nombre, descripción, fechas y referencia de constancia; no existe una tabla catálogo `CURSO`.

### Tablas del nuevo modelo

| Tabla | Propósito |
| --- | --- |
| `EMPLEADOS` | Datos personales y laborales |
| `DOMICILIOS` | Domicilio de cada empleado, relación 1:1 |
| `EMPLEADO_CURSOS` | Cursos realizados por empleado, relación 1:N |
| `ESTATUS_EMPLEADOS` | Estatus propios del empleado |
| `DEPARTAMENTOS` | Catálogo de departamentos |
| `PUESTOS` | Catálogo de puestos |
| `SEXOS` | Catálogo de sexos |
| `PAISES` | Catálogo de países |
| `ESTADOS` | Estados asociados a países |
| `CREDENCIALES` | Datos de acceso vinculados a empleados |

**Restricciones destacadas:** `EMPLEADOS.TELEFONO_CELULAR` es obligatorio, único y de exactamente 10 dígitos; `CORREO_INSTITUCIONAL` es obligatorio, único y tiene validación básica de formato; `DOMICILIOS.CODIGO_POSTAL` debe tener 5 dígitos; la fecha de fin de un curso no puede ser anterior a su fecha de inicio. Las reglas de formato SQL son validaciones básicas, no sustituyen la validación de negocio en Java.

## 4. Scripts SQL

| Archivo | Uso |
| --- | --- |
| `SCRIPT_CREACION_TABLAS.sql` | Crea únicamente la estructura del esquema nuevo |
| `SCRIPT_INSERCION.sql` | Inserta los datos de ejemplo en el esquema nuevo |
| `SCRIPT_CREACION_DE _TODA_LA_BD.sql` | Ejecuta creación e inserciones en un solo archivo |

**Usa una sola alternativa:** ejecuta el script completo **o** el de tablas seguido del de inserciones. No ejecutes ambos métodos sobre el mismo esquema.

> **Importante:** estos archivos están diseñados para un **esquema nuevo y vacío**. No contienen borrado de tablas y no son scripts de actualización de una base ya existente. El script de borrado anterior se retira de la entrega.

### Crear una base nueva

1. Tener disponible una PDB de Oracle y un usuario con permisos para crear tablas.
2. Conectarse a la PDB con el usuario de aplicación, **no con `SYS` para crear las tablas de la aplicación**.
3. Ejecutar el archivo completo en SQL*Plus, desde el directorio donde se encuentre:

```sql
@"SCRIPT_CREACION_DE _TODA_LA_BD.sql"
```

Si los nombres de archivo conservan espacios o variantes, utiliza la ruta real entre comillas. También puedes abrir y ejecutar el script en SQL Developer.

Verifica las tablas y los datos:

```sql
SELECT USER FROM dual;
SELECT table_name FROM user_tables ORDER BY table_name;
SELECT COUNT(*) FROM EMPLEADOS;
SELECT COUNT(*) FROM DOMICILIOS;
SELECT COUNT(*) FROM EMPLEADO_CURSOS;
SELECT COUNT(*) FROM ESTATUS_EMPLEADOS;
```

**Datos de prueba:** los registros incluidos en el script de inserción son ejemplos. Las credenciales o hashes de ejemplo no deben utilizarse para autenticación real. Si los datos no cumplen alguna restricción, debe corregirse el script de inserción antes de volver a crear un esquema de prueba.

### Base de datos existente

El entorno local utiliza el modelo actualizado de 10 tablas funcionales:
EMPLEADOS, DOMICILIOS, EMPLEADO_CURSOS, ESTATUS_EMPLEADOS,
DEPARTAMENTOS, PUESTOS, SEXOS, PAISES, ESTADOS y CREDENCIALES.

Pueden existir tablas antiguas o de respaldo en Oracle, pero no forman
parte del modelo definitivo de la aplicación.

Los scripts de creación están destinados a esquemas nuevos y vacíos.
No deben ejecutarse sobre el esquema SARITA existente.

## 5. Conexión de Oracle con Payara

La unidad de persistencia utiliza el recurso JNDI definido en `src/main/resources/META-INF/persistence.xml`:

```xml
<jta-data-source>jdbc/orcl</jta-data-source>
```

En la consola de administración de Payara (`http://localhost:4848`), configura un **JDBC Connection Pool** compatible con Oracle:

```text
Pool Name: ORACLE
Resource Type: javax.sql.DataSource
Datasource Classname: oracle.jdbc.pool.OracleDataSource
URL: jdbc:oracle:thin:@//<DB_HOST>:<DB_PORT>/<PDB_SERVICE>
user: <DB_USER>
password: <DB_PASSWORD>
```

Crea el recurso JDBC `jdbc/orcl` asociado al pool `ORACLE`. Comprueba la conexión con **Ping**. El driver Oracle debe estar disponible para Payara, no solo para Maven.

En Docker, `DB_HOST` normalmente será el nombre accesible del contenedor Oracle desde el contenedor Payara, **no `localhost`**. Los valores exactos dependen de la red y la configuración del equipo.

No subas credenciales reales ni archivos de configuración con contraseñas a GitHub.

## 6. Compilar y desplegar

Desde la raíz del proyecto:

```bash
mvn clean package -DskipTests
```

El WAR esperado es:

```text
target/Sarita.NoSQL.Venus-1.0-SNAPSHOT.war
```

Puedes desplegarlo desde la consola de Payara (**Applications > Deploy**) o con los comandos de administración del servidor.

En el entorno Docker utilizado durante el desarrollo, cuando los contenedores y recursos ya están configurados, se ha empleado:

```bash
docker cp target/Sarita.NoSQL.Venus-1.0-SNAPSHOT.war sarita-payara-nuevo:/tmp/Sarita.NoSQL.Venus.war

docker exec sarita-payara-nuevo /opt/payara/appserver/bin/asadmin \
  --user admin --passwordfile /opt/payara/passwordFile \
  deploy --force=true --name Sarita.NoSQL.Venus \
  /tmp/Sarita.NoSQL.Venus.war
```

Estos comandos dependen de que exista el contenedor `sarita-payara-nuevo` y de que su archivo de contraseña esté configurado. **El despliegue no garantiza compatibilidad con el esquema nuevo hasta actualizar Java.**

## 7. Acceso y funcionalidades

Con el contexto de despliegue `Sarita.NoSQL.Venus`, la dirección esperada es:

```text
http://localhost:8080/Sarita.NoSQL.Venus/
```

La interfaz contempla búsqueda de empleados por identificador, nombre, apellidos o correo; filtros por estado; consulta de detalles; altas y edición; domicilio; y registro de cursos desde la edición de un empleado.

**Cursos:** en el diseño nuevo se registra el curso realizado directamente para el empleado. No se crea ni selecciona un curso desde un catálogo global. La vista y los servicios deben quedar alineados con este flujo.

**Bajas:** se debe conservar la lógica de baja de empleados acordada para la aplicación, sin confundirla con la eliminación física de registros.

> Las rutas REST, entidades y pantallas deben verificarse y adaptarse después de la migración SQL. Las rutas de catálogos de cursos documentadas en versiones anteriores del README ya no describen el modelo definitivo.

## 8. Estructura del proyecto

```text
src/main/java/com/mycompany/sarita/nosql/venus/
├── model/       Entidades JPA
├── service/     Lógica de negocio
└── resources/   Recursos REST

src/main/resources/META-INF/
└── persistence.xml

src/main/webapp/
├── index.html
├── views/       Vistas de empleados y catálogos
├── forms/       Formularios
├── js/          Lógica de interfaz
└── styles.css   Estilos
```

## 9. Solución de problemas

| Problema | Qué revisar |
| --- | --- |
| `ORA-00942` (tabla no existe) | Que el esquema conectado y los nombres plurales coincidan con las entidades y consultas Java |
| `ORA-00001` (valor duplicado) | Correo, teléfono, usuario o campos con `UNIQUE` |
| `ORA-02291` (clave foránea) | Que exista el empleado o el registro de catálogo referenciado |
| `ORA-02290` (CHECK) | Formato de teléfono, correo, código postal o coherencia de fechas |
| Error de conexión JDBC | Host, puerto, PDB, usuario, contraseña y **Ping** del pool `ORACLE` |
| Error `404` | Estado del despliegue y context root en Payara |
| Errores al insertar IDs | Revisar columnas `GENERATED ALWAYS AS IDENTITY`; no enviar valores manuales |

Para consultar las restricciones de una tabla en Oracle:

```sql
SELECT constraint_name, constraint_type, status
FROM user_constraints
WHERE table_name = 'EMPLEADOS'
ORDER BY constraint_name;
```

## 10. Git y colaboración

Antes de registrar cambios:

```bash
git status
git diff
```

Después de verificar los archivos correctos:

```bash
git add README.md
git commit -m "Documentacion: actualizar README al nuevo modelo de base de datos"
git push -u origin actualizacion-bd-local
```

No uses `git add .` sin revisar archivos temporales, respaldos, credenciales o contenido generado. El README documenta el **modelo SQL propuesto**; la migración de Oracle y la adaptación de Java se deben registrar en commits adicionales una vez probadas.
## 11. Autenticación y seguridad

### Estado actual

La aplicación incluye una pantalla de inicio de sesión de demostración.

Actualmente, el acceso de la interfaz no verifica las credenciales
contra Oracle. Los servicios REST tampoco están protegidos mediante
autenticación del servidor.

Por lo tanto, este mecanismo no debe utilizarse en producción.

### Implementación pendiente

- Validar usuario y contraseña desde el backend.
- Consultar la tabla CREDENCIALES de Oracle.
- Verificar las contraseñas mediante hashes seguros.
- Crear y controlar sesiones autenticadas.
- Proteger los endpoints REST.
- Implementar el cierre de sesión real.
- Evitar credenciales escritas directamente en JavaScript.
