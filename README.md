# Venus - Guía de configuración

Aplicación web de Recursos Humanos con Jakarta EE 10, JAX-RS, JPA/EclipseLink, Oracle Database y Payara Server.

Incluye empleados, domicilios, cursos, asignaciones empleado-curso, países, estados, puestos, departamentos y estatus.

## 1. Requisitos

| Herramienta | Versión |
| --- | --- |
| JDK | 21 |
| Maven | 3.9 o superior |
| Payara Server | 7 |
| Oracle Database | 21c o superior |
| NetBeans o VS Code | Actual |

El proyecto usa `ojdbc11` `23.4.0.24.05` y Jakarta EE Web `10.0.0`.

Comprueba Java y Maven:

```powershell
java -version
mvn -version
```

## 2. Clonar y abrir el proyecto

Cada integrante debe usar sus propias rutas y credenciales. Desde PowerShell:

```powershell
git clone <REPO_URL>
cd Sarita.NoSQL.Venus
git status
mvn clean package
```

En NetBeans usa **File > Open Project**. En VS Code usa **File > Open Folder** y selecciona la carpeta que contiene `pom.xml`.

## 3. Crear toda la base de datos

El archivo oficial para crear la base de datos completa es:

```text
SCRIPT_CREACION_DE _TODA_LA_BD.sql
```

Está en la raíz del proyecto. Este es el script que debe utilizar todo el equipo. No es necesario copiar manualmente otro bloque SQL del README.

El script contiene:

- Borrado opcional de tablas existentes con `CASCADE CONSTRAINTS`.
- Creación de todas las tablas.
- Claves primarias y foráneas.
- Columnas identity de Oracle.
- Catálogos iniciales.
- Países y estados iniciales.
- Departamentos y puestos iniciales.
- Cursos y empleados de prueba, si están incluidos en la versión del archivo.

### Antes de ejecutarlo

1. Cada integrante debe tener una PDB disponible.
2. Crear o seleccionar un usuario propio de Oracle.
3. Conectarse a esa PDB con el usuario que utilizará Payara.
4. Abrir `SCRIPT_CREACION_DE _TODA_LA_BD.sql` en SQL Developer o SQL*Plus.
5. Ejecutar el script completo.

Ejemplo para preparar un usuario nuevo, reemplazando los valores entre `< >`:

```sql
ALTER SESSION SET CONTAINER = <PDB_SERVICE>;

CREATE USER <DB_USER> IDENTIFIED BY "<DB_PASSWORD>";
GRANT CREATE SESSION, RESOURCE TO <DB_USER>;
ALTER USER <DB_USER> QUOTA UNLIMITED ON USERS;
```

Después hay que conectarse como `<DB_USER>` en `<PDB_SERVICE>` y ejecutar el archivo oficial.

> Atención: el script incluye bloques `DROP TABLE`. Ejecutarlo puede borrar la información existente del esquema. Úsalo para una instalación inicial o para reiniciar una base de pruebas, no sobre datos que deban conservarse.

Verifica la instalación:

```sql
SELECT USER FROM dual;
SELECT table_name FROM user_tables ORDER BY table_name;
SELECT COUNT(*) FROM cat_estatus;
SELECT COUNT(*) FROM empleado;
SELECT COUNT(*) FROM curso;
```

## 4. Iniciar Payara desde NetBeans

Cada integrante puede instalar Payara en cualquier ruta. Para trabajar con la interfaz web, registra Payara en NetBeans:

```text
<PAYARA_HOME>\glassfish\domains\domain1\
<PAYARA_HOME>\glassfish\domains\domain1\lib\
```

En NetBeans:

1. Abre la pestaña **Services**.
2. Haz clic derecho en **Servers** y selecciona **Add Server**.
3. Selecciona **Payara Server**.
4. Indica la carpeta de instalación en `<PAYARA_HOME>`.
5. Selecciona o crea el dominio `domain1`.
6. Haz clic derecho sobre Payara y selecciona **Start**.

Cuando el servidor esté iniciado, abre la consola web de administración:

```text
http://localhost:4848
```

La aplicación usa el puerto `8080`.

## 5. Preparar el driver Oracle

Esta parte **no se hace desde la interfaz web de Payara**. La consola web sirve para crear y configurar el pool, pero no instala archivos `.jar` dentro del servidor.

Maven utiliza el driver declarado en `pom.xml`, pero el pool de Payara también necesita tener acceso al driver antes de hacer **Ping**.

Busca el archivo:

```powershell
Get-ChildItem "$env:USERPROFILE\.m2\repository\com\oracle\database\jdbc\ojdbc11" -Recurse -Filter ojdbc11-*.jar
```

Copia el archivo `ojdbc11-*.jar` en la carpeta `lib` del dominio:

```text
<PAYARA_HOME>\glassfish\domains\domain1\lib\
```

Después de copiar el driver, reinicia Payara desde NetBeans con clic derecho sobre el servidor y **Restart**. Cuando Payara vuelva a iniciar, continúa con la configuración web del pool en la siguiente sección.

## 6. Crear el pool JDBC y el recurso JNDI desde la interfaz web de Payara

La aplicación busca exactamente este recurso JNDI en `persistence.xml`:

```xml
<jta-data-source>jdbc/orcl</jta-data-source>
```

Todo lo de esta sección se hace desde `http://localhost:4848`, sin comandos de Payara:

Ve a **Resources > JDBC > JDBC Connection Pools > New** y usa:

```text
Pool Name: ORACLE
Resource Type: javax.sql.DataSource
Datasource Classname: oracle.jdbc.pool.OracleDataSource
```

Propiedades del pool:

```text
URL      = jdbc:oracle:thin:@//<DB_HOST>:<DB_PORT>/<PDB_SERVICE>
user     = <DB_USER>
password = <DB_PASSWORD>
```

Guarda y presiona **Ping**. El ping debe funcionar antes de desplegar la aplicación.

Después ve a **Resources > JDBC > JDBC Resources > New**:

```text
JNDI Name: jdbc/orcl
Pool Name: ORACLE
```

No subas contraseñas reales, `domain.xml` personal ni archivos con credenciales al repositorio.

## 7. Construir y desplegar desde la consola web

Construye desde la carpeta del proyecto:

```powershell
cd <PROJECT_DIR>
mvn clean package
```

Se generará:

```text
target\Sarita.NoSQL.Venus-1.0-SNAPSHOT.war
```

### Desplegar la WAR desde Payara Admin Console

1. Construye el proyecto con Maven.
2. Abre `http://localhost:4848`.
3. En el menú izquierdo entra a **Applications**.
4. Presiona **Deploy**.
5. Selecciona **Packaged File to Be Uploaded to the Server**.
6. Presiona **Browse** y selecciona:

```text
<PROJECT_DIR>\target\Sarita.NoSQL.Venus-1.0-SNAPSHOT.war
```

7. En **Context Root** escribe:

```text
/Sarita.NoSQL.Venus-1.0-SNAPSHOT
```

8. Deja **Virtual Servers** como `server`.
9. Presiona **OK** o **Deploy**.
10. En la lista de aplicaciones verifica que aparezca como **Enabled**.

También puedes registrar el servidor en NetBeans, agregar el proyecto y usar **Clean and Build > Run**; NetBeans abrirá el despliegue en Payara.

## 8. Abrir la aplicación

Página principal:

```text
http://localhost:8080/Sarita.NoSQL.Venus-1.0-SNAPSHOT/
```

API base:

```text
http://localhost:8080/Sarita.NoSQL.Venus-1.0-SNAPSHOT/resources/
```

Rutas principales:

| Método | Ruta | Función |
| --- | --- | --- |
| GET | `/resources/empleados` | Lista empleados |
| POST | `/resources/empleados` | Crea empleado y domicilio |
| PUT | `/resources/empleados/{id}` | Edita empleado y domicilio |
| DELETE | `/resources/empleados/{id}` | Da de baja empleado |
| GET | `/resources/empleados/catalogos` | Carga los catálogos del formulario |
| GET/POST | `/resources/catalogos/cursos` | Consulta y crea cursos |
| GET/POST | `/resources/catalogos/paises` | Consulta y crea países |
| GET/POST | `/resources/catalogos/estados` | Consulta y crea estados |
| GET/POST | `/resources/empleado-cursos` | Consulta y asigna cursos |
| GET | `/resources/empleado-cursos/empleado/{id}` | Consulta cursos de un empleado |

## 9. Flujo de uso

1. Abre **Empleados**.
2. Selecciona **Nuevo empleado**.
3. Captura los datos personales, laborales y el domicilio.
4. Usa un correo institucional que todavía no exista.
5. Guarda el empleado.
6. Usa **Ver detalle** para consultar toda su información.
7. En **Cursos y asignaciones**, crea o selecciona un curso.
8. Activa **Curso en curso** si `fecha_fin` debe permanecer como `NULL`.
9. Desactiva la opción y captura una fecha final cuando el curso termine.

## 10. Solución de problemas

### Payara devuelve `404`

Comprueba el despliegue desde `http://localhost:4848`, entrando a **Applications**. La aplicación debe aparecer como **Enabled**.

Usa exactamente el contexto:

```text
/Sarita.NoSQL.Venus-1.0-SNAPSHOT/
```

### `No password credential found`

Configura nuevamente el usuario y contraseña en **Resources > JDBC > JDBC Connection Pools > ORACLE > Additional Properties**. Guarda, reinicia Payara desde NetBeans y usa el botón **Ping** del pool.

### `ORA-01017`, `ORA-17002` o error de conexión

Revisa `<DB_HOST>`, `<DB_PORT>`, `<PDB_SERVICE>`, `<DB_USER>` y `<DB_PASSWORD>`.

### `ORA-00001 unique constraint violated`

El correo del empleado o el nombre del curso ya existe. Usa un dato diferente.

### `ORA-02289` o `ORA-32795`

No insertes manualmente IDs de columnas `GENERATED ALWAYS AS IDENTITY`. La aplicación omite esas columnas para que Oracle genere los valores.

### Revisar el log de Payara

```powershell
Get-Content '<PAYARA_HOME>\glassfish\domains\domain1\logs\server.log' -Tail 100
```

## 11. Estructura MVC

```text
src/main/java/com/mycompany/sarita/nosql/venus/
├── model/       Entidades JPA y relaciones con Oracle
├── service/     Lógica de negocio y objetos de petición
└── resources/   Controladores REST JAX-RS

src/main/webapp/
├── index.html   Vista y comportamiento del frontend
└── styles.css   Estilos visuales externos
```

La persistencia se configura en `src/main/resources/META-INF/persistence.xml` y usa el recurso JNDI `jdbc/orcl`.

## 12. Git para el equipo

```powershell
git status
git pull
git add .
git commit -m "Describe el cambio"
git push
```

Antes de hacer `push`, verifica que no incluyes contraseñas, configuraciones personales, `target/` ni archivos generados por Payara.
