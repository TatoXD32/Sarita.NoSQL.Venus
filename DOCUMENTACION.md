# Venus - Documentacion del codigo

## 1. Que es la aplicacion

Venus es una aplicacion web de Recursos Humanos construida como una aplicacion **Jakarta EE 10** empaquetada como archivo `WAR`. Administra:

- Empleados y sus domicilios.
- Departamentos, puestos, sexos y estatus.
- Paises y estados.
- Cursos.
- Asignaciones de cursos a empleados.

La aplicacion usa:

- **Jakarta REST (JAX-RS)** para publicar la API HTTP.
- **JPA/EclipseLink** para mapear clases Java a tablas de Oracle.
- **CDI** para inyectar servicios.
- **JTA** para manejar transacciones.
- **HTML, CSS y JavaScript** como interfaz web.
- **Maven** para compilar y generar el `WAR`.
- **Payara Server** para ejecutar la aplicacion.

La base de datos se crea con `SCRIPT_CREACION_DE _TODA_LA_BD.sql`. El codigo no crea automaticamente el esquema: en `persistence.xml` la propiedad de generacion de esquema esta configurada en `none`.

## 2. Estructura del proyecto

```text
src/main/
├── java/com/mycompany/sarita/nosql/venus/
│   ├── JakartaRestConfiguration.java
│   ├── model/       Entidades JPA que representan tablas
│   ├── resources/   Endpoints REST de la API
│   └── service/     Logica de empleados y clases Request
├── resources/META-INF/persistence.xml
└── webapp/
    ├── index.html   Interfaz y codigo JavaScript del navegador
    ├── styles.css   Estilos de la interfaz
    └── WEB-INF/web.xml
```

El flujo general es:

```text
Usuario
  -> index.html y JavaScript
  -> fetch() a /resources/...
  -> recurso JAX-RS
  -> EmpleadoService o EntityManager
  -> JPA / SQL nativo
  -> Oracle
  -> JSON de respuesta
  -> actualizacion de la pantalla
```

## 3. Configuracion y arranque de REST

### `JakartaRestConfiguration`

La clase `JakartaRestConfiguration` extiende `jakarta.ws.rs.core.Application` y tiene:

```java
@ApplicationPath("resources")
```

Esto establece el prefijo comun de la API. Por ejemplo, el metodo `listar()` de `EmpleadosResource`, que tiene `@Path("empleados")`, queda publicado como:

```text
/resources/empleados
```

El archivo `web.xml` indica que `index.html` es la pagina inicial de la aplicacion.

### `persistence.xml`

La unidad de persistencia se llama `my_persistence_unit` y usa transacciones JTA:

```xml
<persistence-unit name="my_persistence_unit" transaction-type="JTA">
    <jta-data-source>jdbc/orcl</jta-data-source>
</persistence-unit>
```

`jdbc/orcl` es un recurso JNDI configurado en Payara. El `EntityManager` de los servicios y recursos usa exactamente ese nombre mediante `@PersistenceContext(unitName = "my_persistence_unit")`.

La cadena de conexion queda fuera del codigo, en el pool JDBC de Payara. Por eso el despliegue necesita:

1. Oracle disponible y con el script ejecutado.
2. El driver `ojdbc11` instalado en Payara.
3. Un pool JDBC, normalmente llamado `ORACLE`.
4. Un recurso JNDI llamado `jdbc/orcl`.

## 4. Modelos y relaciones

Todas las clases del paquete `model` son entidades JPA. `@Entity` indica que una clase se persiste y `@Table` indica la tabla correspondiente. `@Id` marca la clave primaria; en las claves numericas se usa `GenerationType.IDENTITY`, por lo que Oracle genera el identificador.

| Clase | Tabla | Funcion y relaciones |
| --- | --- | --- |
| `Empleado` | `empleado` | Entidad principal. Tiene datos personales, correo, telefono y fechas. Se relaciona con `Sexo`, `Departamento`, `Domicilio`, `Puesto` y `CatEstatus` mediante `@ManyToOne`. |
| `Domicilio` | `domicilio` | Guarda calle, codigo postal, numeros y referencia a `Estado`. |
| `Estado` | `estado` | Guarda nombre, clave, estatus y referencia a `Pais`. |
| `Pais` | `pais` | Catalogo de paises con clave, nombre y estatus. |
| `Departamento` | `departamento` | Catalogo de departamentos. |
| `Puesto` | `puesto` | Catalogo de puestos. |
| `Sexo` | `sexo` | Catalogo de sexos. |
| `CatEstatus` | `cat_estatus` | Catalogo general de estatus. Tiene clave, descripcion y categoria. |
| `Curso` | `curso` | Curso de capacitacion con nombre, descripcion y estatus. |
| `EmpleadoCurso` | `empleado_curso` | Tabla intermedia entre empleado y curso. Tambien guarda `fechaInicio` y `fechaFin`. |

### Relaciones principales

```text
Pais 1 ------ N Estado
Estado 1 ---- N Domicilio
Domicilio 1 - 1 Empleado     (el domicilio se marca como unique en Empleado)
Sexo 1 ------ N Empleado
Departamento 1 - N Empleado
Puesto 1 ---- N Empleado
CatEstatus 1 - N Empleado / Curso / Pais / Estado
Empleado 1 -- N EmpleadoCurso N -- 1 Curso
```

Los nombres Java no siempre son iguales a los nombres SQL. Por ejemplo:

- `idEmpleado` corresponde a `id_empleado`.
- `apellidoPaterno` corresponde a `apellido_paterno`.
- `descripcionPuesto` corresponde a `descripcion_puesto`.
- `codigoPostal` corresponde a `codigo_postal`.

Las anotaciones `@Column` y `@JoinColumn` hacen esa conversion explicita.

## 5. Clases Request

Las clases `Request` son objetos simples que reciben el JSON enviado por el navegador. No representan tablas completas; representan los datos necesarios para una operacion.

- `EmpleadoRequest`: datos personales, laborales y del domicilio. Contiene las claves de las relaciones (`idSexo`, `idDepto`, `idPuesto`, `idEstatus`, `idEstado`).
- `CursoRequest`: `nombre`, `descripcion` e `idEstatus`.
- `PaisRequest`: `idPais`, `nombre` e `idEstatus`.
- `EstadoRequest`: `idEstado`, `nombre`, `idPais` e `idEstatus`.
- `EmpleadoCursoRequest`: `idEmpleado`, `idCurso`, `fechaInicio` y `fechaFin`.

Los getters y setters permiten que Jakarta REST convierta automaticamente JSON a objetos Java y viceversa.

Ejemplo de JSON para crear un empleado:

```json
{
  "nombre": "Ana",
  "apellidoPaterno": "Lopez",
  "apellidoMaterno": "Garcia",
  "fechaNacimiento": "1995-04-10",
  "fechaIngreso": "2024-01-15",
  "correoInstitucional": "ana.lopez@empresa.com",
  "telefonoCelular": "5555555555",
  "idSexo": "F",
  "idDepto": 1,
  "idPuesto": 2,
  "idEstatus": "ACTIVO",
  "calle": "Av. Central",
  "codigoPostal": "01000",
  "numeroExterior": "25",
  "numeroInterior": "4",
  "idEstado": "CDMX"
}
```

Las fechas se manejan como `LocalDate`, por lo que se envian en formato `AAAA-MM-DD`.

## 6. Servicios

### `EmpleadoService`

Es un bean CDI con `@ApplicationScoped`: se crea una instancia administrada por la aplicacion y se inyecta en `EmpleadosResource` con `@Inject`.

Su `EntityManager` permite ejecutar consultas JPQL, buscar entidades y ejecutar SQL nativo.

#### `listar()`

Consulta los empleados y trae de inmediato sus relaciones con `join fetch`: sexo, departamento, puesto y estatus. Ordena por `idEmpleado` y devuelve una lista de `Empleado`.

El recurso REST serializa esa lista como JSON para la tabla principal.

#### `buscar(Long id)`

Usa `entityManager.find(Empleado.class, id)`. Devuelve el empleado encontrado o `null`. El recurso transforma `null` en HTTP `404`.

#### `correoExiste(String correo, Long idExcluir)`

Cuenta empleados que tienen el mismo correo institucional. Cuando se edita un empleado, `idExcluir` evita que el empleado se compare consigo mismo. Esta validacion se ejecuta antes de crear o actualizar.

#### `crear(EmpleadoRequest datos)`

Realiza dos inserciones dentro de una operacion transaccional:

1. Inserta el domicilio.
2. Consulta el ID generado del domicilio.
3. Inserta el empleado usando ese ID y las claves de los catalogos.
4. Busca y devuelve el empleado creado por su correo.

La clase usa SQL nativo para las inserciones y JPQL para volver a leer las entidades.

#### `actualizar(Long id, EmpleadoRequest datos)`

Busca el empleado, actualiza sus propiedades y obtiene las entidades relacionadas con `entityManager.getReference(...)`. Luego llama a `actualizarDomicilio(...)` para modificar la tabla `domicilio` relacionada.

Si el empleado no existe, devuelve `null` y el recurso responde `404`.

#### `actualizarDomicilio(Domicilio domicilio, EmpleadoRequest datos)`

Es un metodo privado. Actualiza calle, codigo postal, numeros y estado usando el ID del domicilio.

#### `darDeBaja(Long id)`

No elimina fisicamente al empleado. Busca el registro y cambia su estatus a `BAJA`. Esto conserva el historial y permite que el empleado siga existiendo en la base de datos.

#### `catalogos()`

Consulta departamentos, puestos, sexos, estatuses, domicilios y estados. Devuelve un `record` llamado `Catalogos`, que agrupa todos esos resultados para que el formulario pueda llenar sus listas desplegables con una sola llamada.

## 7. Recursos REST y endpoints

Todos los recursos producen y consumen `application/json` salvo los mensajes de error de duplicidad de correo, que son texto plano.

### `EmpleadosResource`

Ruta base: `/resources/empleados`

| Metodo Java | HTTP | Ruta | Funcion |
| --- | --- | --- | --- |
| `listar()` | GET | `/empleados` | Lista empleados con sus relaciones principales. |
| `catalogos()` | GET | `/empleados/catalogos` | Devuelve catalogos para el formulario de empleados. |
| `crear(EmpleadoRequest)` | POST | `/empleados` | Valida correo, crea domicilio y empleado. Responde `201`. |
| `buscar(Long)` | GET | `/empleados/{id}` | Busca un empleado. Responde `200` o `404`. |
| `actualizar(Long, EmpleadoRequest)` | PUT | `/empleados/{id}` | Valida correo y actualiza empleado y domicilio. |
| `darDeBaja(Long)` | DELETE | `/empleados/{id}` | Cambia el estatus a `BAJA`. Responde `204` o `404`. |

Aunque el verbo es `DELETE`, la operacion es una baja logica, no un `DELETE` SQL.

### `CatalogosResource`

Ruta base: `/resources/catalogos`

| HTTP | Ruta | Funcion |
| --- | --- | --- |
| GET | `/estatus` | Lista estatus ordenados por categoria y descripcion. |
| GET | `/cursos` | Lista cursos ordenados por nombre. |
| POST | `/cursos` | Crea un curso. |
| PUT | `/cursos/{id}` | Modifica un curso. |
| DELETE | `/cursos/{id}` | Cambia el estatus del curso a `INACTIVO`; no lo borra. |
| GET | `/paises` | Lista paises. |
| POST | `/paises` | Crea un pais. |
| PUT | `/paises/{id}` | Modifica un pais. |
| GET | `/estados` | Lista estados incluyendo su pais mediante `join fetch`. |
| POST | `/estados` | Crea un estado asociado a un pais. |
| PUT | `/estados/{id}` | Modifica nombre, pais y estatus de un estado. |

Este recurso inyecta directamente `EntityManager` con `@PersistenceContext`. Los metodos de escritura tienen `@Transactional`, por lo que los cambios se confirman como una unidad.

### `EmpleadoCursosResource`

Ruta base: `/resources/empleado-cursos`

| HTTP | Ruta | Funcion |
| --- | --- | --- |
| GET | `/empleado-cursos` | Lista asignaciones con empleado y curso. |
| GET | `/empleado-cursos/empleado/{id}` | Lista los cursos de un empleado. |
| POST | `/empleado-cursos` | Asigna un curso a un empleado. |
| PUT | `/empleado-cursos/{id}` | Modifica empleado, curso y fechas de una asignacion. |
| DELETE | `/empleado-cursos/{id}` | Elimina una asignacion. |

`fechaFin` puede ser `null`. La interfaz lo utiliza para representar un curso que sigue en curso.

### `JakartaEE11Resource`

Tiene la ruta `/resources/jakartaee11` y el metodo `ping()`, que responde el texto `ping Jakarta EE`. Es un endpoint de prueba para comprobar que Jakarta REST esta funcionando.

## 8. Como funciona la vista web

La vista principal esta en `src/main/webapp/index.html`. No utiliza un framework JavaScript: usa DOM, `fetch`, formularios y dialogos HTML nativos.

Tiene tres secciones controladas por botones con `data-view`:

1. **Empleados**: tabla, alta, edicion, detalle y baja logica.
2. **Cursos y asignaciones**: alta y activacion/desactivacion de cursos, y asignacion a empleados.
3. **Catalogos**: alta de paises y estados, y consulta de estatus.

### Variables principales

- `api`: ruta base `resources/empleados`.
- `employees`: arreglo de empleados cargado desde la API.
- `catalogs`: objeto con departamentos, puestos, sexos, estatuses, domicilios y estados.
- `editingId`: ID del empleado que se esta editando; si no existe, el formulario crea uno nuevo.

### Funciones JavaScript principales

- `load()`: carga empleados y catalogos en paralelo. Despues llama a `render()` y `loadAdmin()`.
- `render()`: convierte el arreglo de empleados en filas HTML.
- `loadAdmin()`: carga cursos, paises, estados y asignaciones usando `Promise.all`.
- `renderAdmin(...)`: llena selects y tablas de administracion y conecta el cambio de estatus de cursos.
- `fillCatalogs()`: llena los selects del formulario de empleados.
- `openEditor(employee)`: abre el dialogo para crear o editar. Si recibe un empleado, copia sus datos y relaciones al formulario.
- `openDetail(employee)`: abre la ficha, muestra los datos del empleado y consulta sus cursos con `/empleado-cursos/empleado/{id}`.
- `fullName(employee)`: combina nombre y apellidos.
- `statusOptions()`: construye las opciones de estatus.
- `employeeLabel(employee)`: crea el texto que aparece en el selector de empleados.

### Como se guarda un empleado desde la pantalla

1. El usuario abre **Nuevo empleado** o **Editar**.
2. El formulario se transforma con `new FormData(form)` y `Object.fromEntries(...)`.
3. El JavaScript decide entre `POST /resources/empleados` y `PUT /resources/empleados/{id}`.
4. Envia el objeto como JSON con `fetch`.
5. `EmpleadosResource` valida el correo.
6. `EmpleadoService` escribe empleado y domicilio.
7. Si la respuesta es correcta, se cierra el dialogo y se vuelve a ejecutar `load()`.

### Como se cargan los catalogos

Al iniciar, `load()` hace estas llamadas en paralelo:

```text
GET resources/empleados
GET resources/empleados/catalogos
```

Los resultados llenan la tabla y los selects de sexo, departamento, puesto, estatus y estado. Al abrir la seccion administrativa, `loadAdmin()` agrega cursos, paises, estados y asignaciones.

### Cursos en curso

El checkbox `#ongoing-course` controla `fechaFin`:

- Marcado: desactiva el campo de fecha y envia `fechaFin: null`.
- Desmarcado: permite capturar una fecha final.

Esto coincide con el modelo `EmpleadoCurso`, donde `fechaFin` es un `LocalDate` que puede ser nulo.

### `styles.css`

Define la apariencia completa de la aplicacion: colores, tablas, botones, formularios, paneles, dialogos, estados y diseño responsive. La logica no depende del CSS; las clases como `status ACTIVO` y `status BAJA` solo permiten mostrar visualmente el estatus recibido de la API.

## 9. Flujos completos

### Alta de empleado

```text
Formulario HTML
 -> POST /resources/empleados
 -> EmpleadosResource.crear()
 -> EmpleadoService.correoExiste()
 -> EmpleadoService.crear()
 -> INSERT domicilio
 -> INSERT empleado
 -> JSON del empleado creado
 -> load() actualiza la tabla
```

### Edicion de empleado

```text
Boton Editar
 -> openEditor(employee)
 -> PUT /resources/empleados/{id}
 -> valida correo excluyendo ese ID
 -> EmpleadoService.actualizar()
 -> actualiza empleado y domicilio
 -> JSON actualizado
```

### Baja de empleado

```text
Boton Dar de baja
 -> DELETE /resources/empleados/{id}
 -> EmpleadoService.darDeBaja()
 -> estatus = BAJA
 -> HTTP 204
 -> load() vuelve a consultar la lista
```

### Consulta del detalle

```text
Boton Ver detalle
 -> datos del empleado ya cargados en memoria
 -> GET /resources/empleado-cursos/empleado/{id}
 -> se combinan datos personales, domicilio y cursos
 -> se muestra el dialogo de detalle
```

## 10. Construccion y ejecucion

Desde la carpeta que contiene `pom.xml`:

```bash
mvn clean package
```

El resultado es:

```text
target/Sarita.NoSQL.Venus-1.0-SNAPSHOT.war
```

Con Payara iniciado, la aplicacion se abre normalmente en:

```text
http://localhost:8080/Sarita.NoSQL.Venus-1.0-SNAPSHOT/
```

La API queda en:

```text
http://localhost:8080/Sarita.NoSQL.Venus-1.0-SNAPSHOT/resources/
```

El proyecto compila con Java 17 segun `pom.xml`, usa Jakarta EE Web `10.0.0` y el driver Oracle `ojdbc11` `23.4.0.24.05`.

## 11. Errores y respuestas esperadas

- `200 OK`: consulta o actualizacion exitosa.
- `201 Created`: recurso creado.
- `204 No Content`: baja o eliminacion exitosa sin cuerpo de respuesta.
- `404 Not Found`: el ID solicitado no existe.
- `409 Conflict`: el correo institucional ya esta registrado.
- Error de conexion: revisar el pool JDBC `ORACLE`, el recurso `jdbc/orcl`, el driver de Oracle y las credenciales de Payara.

## 12. Resumen de responsabilidades

| Parte | Responsabilidad |
| --- | --- |
| `model` | Define entidades, columnas, claves y relaciones JPA. |
| `service` | Recibe datos de entrada y concentra la logica de empleados. |
| `resources` | Define rutas HTTP, recibe requests y devuelve respuestas. |
| `EntityManager` | Ejecuta JPQL, SQL nativo, busquedas y persistencia. |
| `persistence.xml` | Conecta JPA con la unidad y el recurso JNDI de Payara. |
| `index.html` | Muestra la interfaz y consume la API con JavaScript. |
| `styles.css` | Define la presentacion visual. |
| Oracle | Persiste la informacion real. |
| Payara | Proporciona servidor web, CDI, JPA, transacciones y datasource. |
