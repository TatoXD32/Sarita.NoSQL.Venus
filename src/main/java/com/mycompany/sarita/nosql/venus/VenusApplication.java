package com.mycompany.sarita.nosql.venus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import com.mycompany.sarita.nosql.venus.security.PasswordUtil;
import com.mycompany.sarita.nosql.venus.service.EmpleadoRequest;
import com.mycompany.sarita.nosql.venus.service.EmpleadoValidator;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bson.Document;
import org.bson.types.ObjectId;

public final class VenusApplication {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();
    private static MongoDatabase database;

    private VenusApplication() {
    }

    public static void main(String[] args) {
        String uri = System.getenv().getOrDefault("MONGODB_URI", "mongodb://localhost:27017");
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        MongoClient client = MongoClients.create(uri);
        database = client.getDatabase("Nosql");
        database.runCommand(new Document("ping", 1));

        Javalin app = Javalin.create();
        registerRoutes(app);
        app.before(ctx -> {
            if (ctx.path().startsWith("/resources/")
                    && !ctx.path().equals("/resources/auth/login")
                    && !ctx.path().equals("/resources/auth/sesion")
                    && !ctx.path().equals("/resources/auth/logout")
                    && !authenticated(ctx)) {
                ctx.status(401).json(Map.of("error", "Debes iniciar sesión"));
                ctx.skipRemainingHandlers();
            }
        });
        app.exception(ApiException.class, (exception, ctx) ->
                ctx.status(exception.status).result(exception.getMessage()));
        app.exception(MongoException.class, (exception, ctx) -> {
            if (exception instanceof com.mongodb.MongoWriteException write
                    && write.getError().getCode() == 11000) {
                ctx.status(409).result("El registro ya existe o viola un índice único.");
            } else {
                System.err.println("Error de MongoDB atendiendo " + ctx.path() + ": " + exception.getMessage());
                ctx.status(500).json(Map.of("error", "Error al consultar o actualizar MongoDB."));
            }
        });
        app.exception(Exception.class, (exception, ctx) -> {
            System.err.println("Error atendiendo " + ctx.path() + ": " + exception.getMessage());
            ctx.status(500).json(Map.of("error", "Error interno del servidor."));
        });
        app.get("/", ctx -> serveStatic(ctx, "index.html"));
        app.get("/<path>", ctx -> serveStatic(ctx, ctx.path()));
        app.start(port);
        System.out.printf("Venus disponible en http://localhost:%d%n", port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.stop();
            client.close();
        }));
    }

    private static void registerRoutes(Javalin app) {
        app.post("/resources/auth/login", VenusApplication::login);
        app.get("/resources/auth/sesion", VenusApplication::session);
        app.post("/resources/auth/logout", VenusApplication::logout);

        app.get("/resources/empleados", ctx ->
                ctx.json(database.getCollection("empleados").find().sort(Sorts.ascending("_id"))
                        .map(VenusApplication::employeeResponse).into(new ArrayList<>())));
        app.get("/resources/empleados/catalogos", ctx -> ctx.json(employeeCatalogs()));
        app.post("/resources/empleados", VenusApplication::createEmployee);
        app.get("/resources/empleados/{id}", VenusApplication::getEmployee);
        app.put("/resources/empleados/{id}", VenusApplication::updateEmployee);
        app.delete("/resources/empleados/{id}", VenusApplication::terminateEmployee);

        app.get("/resources/catalogos/estatus", ctx -> ctx.json(statuses()));
        app.post("/resources/catalogos/estatus", ctx -> createValue(ctx, "estatus_empleados", "idEstatus"));
        app.put("/resources/catalogos/estatus/{id}", ctx -> updateValue(ctx, "estatus_empleados", "idEstatus"));
        app.get("/resources/catalogos/sexos", ctx -> ctx.json(sexes()));
        app.post("/resources/catalogos/sexos", ctx -> createValue(ctx, "sexos", "idSexo"));
        app.put("/resources/catalogos/sexos/{id}", ctx -> updateValue(ctx, "sexos", "idSexo"));
        app.delete("/resources/catalogos/sexos/{id}", ctx -> deleteValue(ctx, "sexos", "idSexo", "sexo.id_sexo"));

        app.get("/resources/catalogos/departamentos", ctx -> ctx.json(departments()));
        app.post("/resources/catalogos/departamentos", VenusApplication::createDepartment);
        app.put("/resources/catalogos/departamentos/{id}", VenusApplication::updateDepartment);
        app.delete("/resources/catalogos/departamentos/{id}", VenusApplication::deleteDepartment);
        app.get("/resources/catalogos/puestos", ctx -> ctx.json(positions()));
        app.post("/resources/catalogos/puestos", VenusApplication::createPosition);
        app.put("/resources/catalogos/puestos/{id}", VenusApplication::updatePosition);
        app.delete("/resources/catalogos/puestos/{id}", VenusApplication::deletePosition);
        app.get("/resources/catalogos/paises", ctx -> ctx.json(countries()));
        app.post("/resources/catalogos/paises", ctx -> createLocation(ctx, true));
        app.put("/resources/catalogos/paises/{id}", ctx -> updateLocation(ctx, true));
        app.delete("/resources/catalogos/paises/{id}", ctx -> {
            deleteCountry(ctx.pathParam("id"));
            ctx.status(204);
        });
        app.get("/resources/catalogos/estados", ctx -> ctx.json(states()));
        app.post("/resources/catalogos/estados", ctx -> createLocation(ctx, false));
        app.put("/resources/catalogos/estados/{id}", ctx -> updateLocation(ctx, false));
        app.delete("/resources/catalogos/estados/{id}", ctx -> {
            deleteState(ctx.pathParam("id"));
            ctx.status(204);
        });

        app.get("/resources/empleado-cursos", ctx -> ctx.json(assignments(null)));
        app.get("/resources/empleado-cursos/empleado/{id}", ctx ->
                ctx.json(assignments(parseLong(ctx.pathParam("id"), "idEmpleado"))));
        app.post("/resources/empleado-cursos", VenusApplication::createAssignment);
        app.put("/resources/empleado-cursos/{id}", VenusApplication::updateAssignment);
        app.delete("/resources/empleado-cursos/{id}", VenusApplication::deleteAssignment);
    }

    private static void login(Context ctx) throws IOException {
        Map<String, Object> body = body(ctx);
        String username = text(body.get("usuario"));
        String password = text(body.get("contrasena"));
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ApiException(400, "Datos incompletos");
        }
        Document credential = database.getCollection("credenciales")
                .find(Filters.eq("usuario", username.trim())).first();
        if (credential == null || !PasswordUtil.verify(password, credential.getString("contrasena_hash"))) {
            throw new ApiException(401, "Credenciales incorrectas");
        }
        String token = UUID.randomUUID().toString();
        SESSIONS.put(token, new Session(username.trim(), Instant.now().plusSeconds(1800)));
        ctx.header("Set-Cookie", "venus_session=" + token + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=1800");
        ctx.json(Map.of("mensaje", "Inicio de sesión correcto", "usuario", username.trim()));
    }

    private static void session(Context ctx) {
        Session session = sessionFor(ctx);
        if (session == null) {
            ctx.status(401).json(Map.of("autenticado", false));
        } else {
            ctx.json(Map.of("autenticado", true, "usuario", session.username));
        }
    }

    private static void logout(Context ctx) {
        String token = cookie(ctx, "venus_session");
        if (token != null) SESSIONS.remove(token);
        ctx.header("Set-Cookie", "venus_session=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0");
        ctx.json(Map.of("mensaje", "Sesión cerrada"));
    }

    private static boolean authenticated(Context ctx) {
        return sessionFor(ctx) != null;
    }

    private static Session sessionFor(Context ctx) {
        String token = cookie(ctx, "venus_session");
        if (token == null) return null;
        Session session = SESSIONS.get(token);
        if (session != null && session.expiresAt.isBefore(Instant.now())) {
            SESSIONS.remove(token);
            return null;
        }
        return session;
    }

    private static String cookie(Context ctx, String name) {
        String header = ctx.header("Cookie");
        if (header == null) return null;
        for (String item : header.split(";")) {
            String[] pair = item.trim().split("=", 2);
            if (pair.length == 2 && pair[0].equals(name)) return pair[1];
        }
        return null;
    }

    private static void createEmployee(Context ctx) throws IOException {
        Map<String, Object> body = body(ctx);
        EmpleadoRequest request = employeeRequest(body);
        validateEmployee(request, body, null);
        Document employee = employeeDocument(request, body, nextNumericId("empleados"));
        database.getCollection("empleados").insertOne(employee);
        ctx.status(201).json(employeeResponse(employee));
    }

    private static void getEmployee(Context ctx) {
        Document employee = findEmployee(ctx.pathParam("id"));
        if (employee == null) throw new ApiException(404, "Empleado no encontrado.");
        ctx.json(employeeResponse(employee));
    }

    private static void updateEmployee(Context ctx) throws IOException {
        long id = parseLong(ctx.pathParam("id"), "idEmpleado");
        Map<String, Object> body = body(ctx);
        EmpleadoRequest request = employeeRequest(body);
        validateEmployee(request, body, id);
        MongoCollection<Document> employees = database.getCollection("empleados");
        if (employees.find(Filters.eq("_id", id)).first() == null)
            throw new ApiException(404, "Empleado no encontrado.");
        Document changes = employeeDocument(request, body, id);
        changes.remove("_id");
        changes.remove("cursos");
        UpdateResult result = employees.updateOne(Filters.eq("_id", id), new Document("$set", changes));
        updateOptional(body, "rfc", "rfc", id);
        updateOptional(body, "estadoCivil", "estado_civil", id);
        if (result.getMatchedCount() == 0) throw new ApiException(404, "Empleado no encontrado.");
        ctx.json(employeeResponse(findEmployee(Long.toString(id))));
    }

    private static void updateOptional(Map<String, Object> body, String inputKey, String dbKey, long id) {
        String alternate = inputKey.equals("estadoCivil") ? "estado_civil" : inputKey;
        if (!body.containsKey(inputKey) && !body.containsKey(alternate)) return;
        Object value = body.containsKey(inputKey) ? body.get(inputKey) : body.get(alternate);
        MongoCollection<Document> employees = database.getCollection("empleados");
        if (value == null || (value instanceof String string && string.isBlank())) {
            employees.updateOne(Filters.eq("_id", id), Updates.unset(dbKey));
        } else {
            employees.updateOne(Filters.eq("_id", id), Updates.set(dbKey, value.toString().trim()));
        }
    }

    private static void terminateEmployee(Context ctx) {
        long id = parseLong(ctx.pathParam("id"), "idEmpleado");
        Document status = findValue("estatus_empleados", "BAJA");
        if (status == null) throw new ApiException(500, "No existe el estatus BAJA en el catálogo.");
        UpdateResult result = database.getCollection("empleados").updateOne(
                Filters.eq("_id", id),
                Updates.set("estatus", new Document("id_estatus", "BAJA")
                        .append("descripcion", status.getString("descripcion"))));
        if (result.getMatchedCount() == 0) throw new ApiException(404, "Empleado no encontrado.");
        ctx.status(204);
    }

    private static void validateEmployee(EmpleadoRequest request, Map<String, Object> body, Long excludedId) {
        EmpleadoValidator.ValidationResult result = EmpleadoValidator.validar(request);
        List<String> errors = new ArrayList<>(result.errores());
        require(request.getNombre(), "nombre", errors);
        require(request.getApellidoPaterno(), "apellidoPaterno", errors);
        require(request.getFechaIngreso() == null ? null : "ok", "fechaIngreso", errors);
        require(request.getCorreoInstitucional(), "correoInstitucional", errors);
        require(request.getIdSexo(), "idSexo", errors);
        require(request.getIdDepto() == null ? null : "ok", "idDepto", errors);
        require(request.getIdPuesto() == null ? null : "ok", "idPuesto", errors);
        require(request.getCalle(), "calle", errors);
        require(request.getCodigoPostal(), "codigoPostal", errors);
        require(request.getNumeroExterior(), "numeroExterior", errors);
        require(request.getIdEstado(), "idEstado", errors);
        if (request.getCorreoInstitucional() != null
                && database.getCollection("empleados").find(Filters.and(
                        Filters.eq("correo_institucional", request.getCorreoInstitucional().trim()),
                        excludedId == null ? Filters.exists("_id") : Filters.ne("_id", excludedId))).first() != null) {
            throw new ApiException(409, "El correo institucional ya está registrado.");
        }
        if (!errors.isEmpty()) throw new ApiException(400, String.join("; ", errors));
        String sex = normalize(request.getIdSexo());
        String status = normalize(request.getIdEstatus() == null || request.getIdEstatus().isBlank()
                ? "ACTIVO" : request.getIdEstatus());
        if (findValue("sexos", sex) == null) throw new ApiException(400, "El sexo seleccionado no existe.");
        if (findValue("estatus_empleados", status) == null) throw new ApiException(400, "El estatus seleccionado no existe.");
        if (database.getCollection("departamentos").find(Filters.eq("_id", request.getIdDepto())).first() == null)
            throw new ApiException(400, "El departamento seleccionado no existe.");
        if (database.getCollection("puestos").find(Filters.eq("_id", request.getIdPuesto())).first() == null)
            throw new ApiException(400, "El puesto seleccionado no existe.");
        if (locationForState(request.getIdEstado()) == null) throw new ApiException(400, "El estado seleccionado no existe.");
    }

    private static void require(String value, String field, List<String> errors) {
        if (value == null || value.isBlank()) errors.add("El campo " + field + " es obligatorio.");
    }

    private static Document employeeDocument(EmpleadoRequest request, Map<String, Object> body, long id) {
        String sexId = normalize(request.getIdSexo());
        String statusId = normalize(request.getIdEstatus() == null || request.getIdEstatus().isBlank()
                ? "ACTIVO" : request.getIdEstatus());
        Document sex = findValue("sexos", sexId);
        Document status = findValue("estatus_empleados", statusId);
        Document location = locationForState(request.getIdEstado());
        Document address = new Document("calle", request.getCalle().trim())
                .append("codigo_postal", request.getCodigoPostal().trim())
                .append("numero_exterior", request.getNumeroExterior().trim())
                .append("numero_interior", blankToNull(request.getNumeroInterior()))
                .append("estado", new Document("id_estado", location.getString("id_estado"))
                        .append("nombre", location.getString("nombre"))
                        .append("pais", new Document("id_pais", location.getString("id_pais"))
                                .append("nombre", location.getString("pais_nombre"))));
        Document employee = new Document("_id", id)
                .append("nombre", request.getNombre().trim())
                .append("apellido_paterno", request.getApellidoPaterno().trim())
                .append("apellido_materno", blankToNull(request.getApellidoMaterno()))
                .append("fecha_nacimiento", toBsonDate(request.getFechaNacimiento()))
                .append("correo_institucional", request.getCorreoInstitucional().trim())
                .append("telefono_celular", request.getTelefonoCelular().replaceAll("\\D+", ""))
                .append("fecha_ingreso", toBsonDate(request.getFechaIngreso()))
                .append("id_depto", request.getIdDepto())
                .append("id_puesto", request.getIdPuesto())
                .append("sexo", new Document("id_sexo", sex.getString("id_sexo"))
                        .append("descripcion", sex.getString("descripcion")))
                .append("estatus", new Document("id_estatus", status.getString("id_estatus"))
                        .append("descripcion", status.getString("descripcion")))
                .append("domicilio", address)
                .append("cursos", List.of());
        addOptional(employee, body, "rfc", "rfc");
        addOptional(employee, body, "estadoCivil", "estado_civil");
        return employee;
    }

    private static void addOptional(Document employee, Map<String, Object> body, String inputKey, String dbKey) {
        String alternate = inputKey.equals("estadoCivil") ? "estado_civil" : inputKey;
        Object value = body.containsKey(inputKey) ? body.get(inputKey) : body.get(alternate);
        if (value != null && !value.toString().isBlank()) employee.put(dbKey, value.toString().trim());
    }

    private static EmpleadoRequest employeeRequest(Map<String, Object> body) {
        EmpleadoRequest request = new EmpleadoRequest();
        request.setNombre(text(body.get("nombre")));
        request.setApellidoPaterno(text(body.get("apellidoPaterno")));
        request.setApellidoMaterno(text(body.get("apellidoMaterno")));
        request.setFechaNacimiento(parseDate(body.get("fechaNacimiento"), "fechaNacimiento"));
        request.setFechaIngreso(parseDate(body.get("fechaIngreso"), "fechaIngreso"));
        request.setCorreoInstitucional(text(body.get("correoInstitucional")));
        request.setTelefonoCelular(text(body.get("telefonoCelular")));
        request.setIdSexo(text(body.get("idSexo")));
        request.setIdDepto(nullableLong(body.get("idDepto"), "idDepto"));
        request.setIdPuesto(nullableLong(body.get("idPuesto"), "idPuesto"));
        request.setIdEstatus(text(body.get("idEstatus")));
        request.setCalle(text(body.get("calle")));
        request.setCodigoPostal(text(body.get("codigoPostal")));
        request.setNumeroExterior(text(body.get("numeroExterior")));
        request.setNumeroInterior(text(body.get("numeroInterior")));
        request.setIdEstado(text(body.get("idEstado")));
        return request;
    }

    private static Map<String, Object> employeeResponse(Document employee) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("idEmpleado", employee.get("_id"));
        response.put("nombre", employee.getString("nombre"));
        response.put("apellidoPaterno", employee.getString("apellido_paterno"));
        response.put("apellidoMaterno", employee.getString("apellido_materno"));
        response.put("fechaNacimiento", fromBsonDate(employee.getDate("fecha_nacimiento")));
        response.put("correoInstitucional", employee.getString("correo_institucional"));
        response.put("telefonoCelular", employee.getString("telefono_celular"));
        response.put("fechaIngreso", fromBsonDate(employee.getDate("fecha_ingreso")));
        response.put("sexo", catalogValueResponse(employee.get("sexo", Document.class), "idSexo", "id_sexo"));
        response.put("departamento", departmentResponse(employee.get("id_depto")));
        response.put("puesto", positionResponse(employee.get("id_puesto")));
        response.put("estatus", catalogValueResponse(employee.get("estatus", Document.class), "idEstatus", "id_estatus"));
        response.put("domicilio", addressResponse(employee.get("domicilio", Document.class)));
        response.put("cursos", courseResponses(employee.getList("cursos", Document.class, List.of())));
        copyOptional(employee, response, "rfc", "rfc");
        copyOptional(employee, response, "estado_civil", "estadoCivil");
        return response;
    }

    private static void copyOptional(Document source, Map<String, Object> target, String dbKey, String responseKey) {
        if (source.containsKey(dbKey)) target.put(responseKey, source.get(dbKey));
    }

    private static Map<String, Object> addressResponse(Document address) {
        if (address == null) return null;
        Document state = address.get("estado", Document.class);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("calle", address.getString("calle"));
        response.put("codigoPostal", address.getString("codigo_postal"));
        response.put("numeroExterior", address.getString("numero_exterior"));
        response.put("numeroInterior", address.getString("numero_interior"));
        if (state != null) {
            Document country = state.get("pais", Document.class);
            Map<String, Object> stateResponse = new LinkedHashMap<>();
            stateResponse.put("idEstado", state.getString("id_estado"));
            stateResponse.put("nombre", state.getString("nombre"));
            if (country != null) stateResponse.put("pais", Map.of(
                    "idPais", country.getString("id_pais"), "nombre", country.getString("nombre")));
            response.put("estado", stateResponse);
        }
        return response;
    }

    private static Map<String, Object> catalogValueResponse(Document value, String responseId, String dbId) {
        if (value == null) return null;
        return Map.of(responseId, value.getString(dbId), "descripcion", value.getString("descripcion"));
    }

    private static Map<String, Object> departmentResponse(Object id) {
        if (id == null) return null;
        Document item = database.getCollection("departamentos").find(Filters.eq("_id", id)).first();
        return item == null ? null : Map.of("idDepto", item.get("_id"), "descripcion", item.getString("descripcion"));
    }

    private static Map<String, Object> positionResponse(Object id) {
        if (id == null) return null;
        Document item = database.getCollection("puestos").find(Filters.eq("_id", id)).first();
        return item == null ? null : Map.of("idPuesto", item.get("_id"), "descripcionPuesto", item.getString("descripcion_puesto"));
    }

    private static List<Map<String, Object>> courseResponses(List<Document> courses) {
        List<Map<String, Object>> response = new ArrayList<>();
        for (Document course : courses) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idCurso", course.getObjectId("id_curso").toHexString());
            item.put("nombreCurso", course.getString("nombre_curso"));
            item.put("descripcion", course.getString("descripcion"));
            item.put("fechaInicio", fromBsonDate(course.getDate("fecha_inicio")));
            item.put("fechaFin", fromBsonDate(course.getDate("fecha_fin")));
            item.put("constancia", course.getString("constancia"));
            response.add(item);
        }
        return response;
    }

    private static Map<String, Object> employeeCatalogs() {
        return Map.of(
                "departamentos", departments(),
                "puestos", positions(),
                "sexos", sexes(),
                "estatuses", statuses(),
                "estados", states(),
                "domicilios", List.of());
    }

    private static List<Map<String, Object>> departments() {
        List<Map<String, Object>> response = new ArrayList<>();
        for (Document item : database.getCollection("departamentos").find().sort(Sorts.ascending("descripcion"))) {
            response.add(Map.of("idDepto", item.get("_id"), "descripcion", item.getString("descripcion"),
                    "enUso", database.getCollection("empleados").countDocuments(Filters.eq("id_depto", item.get("_id"))) > 0));
        }
        return response;
    }

    private static List<Map<String, Object>> positions() {
        List<Map<String, Object>> response = new ArrayList<>();
        for (Document item : database.getCollection("puestos").find().sort(Sorts.ascending("descripcion_puesto"))) {
            response.add(Map.of("idPuesto", item.get("_id"), "descripcionPuesto", item.getString("descripcion_puesto"),
                    "enUso", database.getCollection("empleados").countDocuments(Filters.eq("id_puesto", item.get("_id"))) > 0));
        }
        return response;
    }

    private static List<Map<String, Object>> statuses() {
        return valueResponses("estatus_empleados", "idEstatus", "id_estatus");
    }

    private static List<Map<String, Object>> sexes() {
        return valueResponses("sexos", "idSexo", "id_sexo");
    }

    private static List<Map<String, Object>> valueResponses(String catalogId, String responseId, String field) {
        List<Map<String, Object>> response = new ArrayList<>();
        for (Document value : catalogValues(catalogId)) {
            response.add(Map.of(responseId, value.getString(field), "descripcion", value.getString("descripcion"),
                    "enUso", valueInUse(field, value.getString(field))));
        }
        response.sort(Comparator.comparing(item -> item.get("descripcion").toString()));
        return response;
    }

    private static boolean valueInUse(String field, String id) {
        String path = field.equals("id_sexo") ? "sexo.id_sexo" : "estatus.id_estatus";
        return database.getCollection("empleados").countDocuments(Filters.eq(path, id)) > 0;
    }

    private static List<Map<String, Object>> countries() {
        List<Map<String, Object>> response = new ArrayList<>();
        for (Document country : locationDocument().getList("paises", Document.class, List.of())) {
            response.add(Map.of("idPais", country.getString("id_pais"), "nombre", country.getString("nombre"),
                    "enUso", database.getCollection("empleados")
                            .countDocuments(Filters.eq("domicilio.estado.pais.id_pais", country.getString("id_pais"))) > 0));
        }
        return response;
    }

    private static List<Map<String, Object>> states() {
        List<Map<String, Object>> response = new ArrayList<>();
        for (Document country : locationDocument().getList("paises", Document.class, List.of())) {
            for (Document state : country.getList("estados", Document.class, List.of())) {
                response.add(Map.of("idEstado", state.getString("id_estado"), "nombre", state.getString("nombre"),
                        "pais", Map.of("idPais", country.getString("id_pais"), "nombre", country.getString("nombre")),
                        "enUso", database.getCollection("empleados")
                                .countDocuments(Filters.eq("domicilio.estado.id_estado", state.getString("id_estado"))) > 0));
            }
        }
        response.sort(Comparator.comparing(item -> item.get("nombre").toString()));
        return response;
    }

    private static void createDepartment(Context ctx) throws IOException {
        Map<String, Object> body = body(ctx);
        String description = requiredText(body, "descripcion");
        long id = nextNumericId("departamentos");
        Document item = new Document("_id", id).append("descripcion", description);
        database.getCollection("departamentos").insertOne(item);
        ctx.status(201).json(Map.of("idDepto", id, "descripcion", description));
    }

    private static void updateDepartment(Context ctx) throws IOException {
        long id = parseLong(ctx.pathParam("id"), "idDepto");
        String description = requiredText(body(ctx), "descripcion");
        UpdateResult result = database.getCollection("departamentos")
                .updateOne(Filters.eq("_id", id), Updates.set("descripcion", description));
        if (result.getMatchedCount() == 0) throw new ApiException(404, "Departamento no encontrado.");
        ctx.json(Map.of("idDepto", id, "descripcion", description));
    }

    private static void deleteDepartment(Context ctx) {
        long id = parseLong(ctx.pathParam("id"), "idDepto");
        if (database.getCollection("empleados").countDocuments(Filters.eq("id_depto", id)) > 0)
            throw new ApiException(409, "No se puede eliminar un departamento asignado a empleados.");
        if (database.getCollection("departamentos").deleteOne(Filters.eq("_id", id)).getDeletedCount() == 0)
            throw new ApiException(404, "Departamento no encontrado.");
        ctx.status(204);
    }

    private static void createPosition(Context ctx) throws IOException {
        String description = requiredText(body(ctx), "descripcionPuesto");
        long id = nextNumericId("puestos");
        database.getCollection("puestos").insertOne(new Document("_id", id).append("descripcion_puesto", description));
        ctx.status(201).json(Map.of("idPuesto", id, "descripcionPuesto", description));
    }

    private static void updatePosition(Context ctx) throws IOException {
        long id = parseLong(ctx.pathParam("id"), "idPuesto");
        String description = requiredText(body(ctx), "descripcionPuesto");
        UpdateResult result = database.getCollection("puestos")
                .updateOne(Filters.eq("_id", id), Updates.set("descripcion_puesto", description));
        if (result.getMatchedCount() == 0) throw new ApiException(404, "Puesto no encontrado.");
        ctx.json(Map.of("idPuesto", id, "descripcionPuesto", description));
    }

    private static void deletePosition(Context ctx) {
        long id = parseLong(ctx.pathParam("id"), "idPuesto");
        if (database.getCollection("empleados").countDocuments(Filters.eq("id_puesto", id)) > 0)
            throw new ApiException(409, "No se puede eliminar un puesto asignado a empleados.");
        if (database.getCollection("puestos").deleteOne(Filters.eq("_id", id)).getDeletedCount() == 0)
            throw new ApiException(404, "Puesto no encontrado.");
        ctx.status(204);
    }

    private static void createValue(Context ctx, String catalogId, String inputId) throws IOException {
        Map<String, Object> body = body(ctx);
        String id = requiredText(body, inputId).toUpperCase(Locale.ROOT);
        String description = requiredText(body, "descripcion");
        List<Document> values = catalogValues(catalogId);
        String dbId = catalogId.equals("sexos") ? "id_sexo" : "id_estatus";
        if (findValue(catalogId, id) != null) throw new ApiException(409, "La clave ya existe.");
        values.add(new Document(dbId, id).append("descripcion", description));
        database.getCollection("catalogos").replaceOne(Filters.eq("_id", catalogId),
                new Document("_id", catalogId).append("valores", values));
        ctx.status(201).json(Map.of(inputId, id, "descripcion", description));
    }

    private static void updateValue(Context ctx, String catalogId, String responseId) throws IOException {
        String id = ctx.pathParam("id").toUpperCase(Locale.ROOT);
        String description = requiredText(body(ctx), "descripcion");
        String dbId = catalogId.equals("sexos") ? "id_sexo" : "id_estatus";
        Document catalog = catalogDocument(catalogId);
        List<Document> values = catalog.getList("valores", Document.class, new ArrayList<>());
        Document value = values.stream().filter(item -> id.equals(item.getString(dbId))).findFirst().orElse(null);
        if (value == null) throw new ApiException(404, "Registro de catálogo no encontrado.");
        value.put("descripcion", description);
        database.getCollection("catalogos").replaceOne(Filters.eq("_id", catalogId), catalog);
        ctx.json(Map.of(responseId, id, "descripcion", description));
    }

    private static void deleteValue(Context ctx, String catalogId, String responseId, String employeePath) {
        String id = ctx.pathParam("id").toUpperCase(Locale.ROOT);
        if (database.getCollection("empleados").countDocuments(Filters.eq(employeePath, id)) > 0)
            throw new ApiException(409, "No se puede eliminar un valor que está asignado a empleados.");
        String dbId = catalogId.equals("sexos") ? "id_sexo" : "id_estatus";
        Document catalog = catalogDocument(catalogId);
        List<Document> values = catalog.getList("valores", Document.class, new ArrayList<>());
        boolean removed = values.removeIf(item -> id.equals(item.getString(dbId)));
        if (!removed) throw new ApiException(404, "Registro de catálogo no encontrado.");
        database.getCollection("catalogos").replaceOne(Filters.eq("_id", catalogId), catalog);
        ctx.status(204);
    }

    private static void createLocation(Context ctx, boolean countryRequest) throws IOException {
        Map<String, Object> body = body(ctx);
        Document location = locationDocument();
        List<Document> countries = location.getList("paises", Document.class, new ArrayList<>());
        if (countryRequest) {
            String id = requiredText(body, "idPais").toUpperCase(Locale.ROOT);
            String name = requiredText(body, "nombre");
            if (findCountry(countries, id) != null) throw new ApiException(409, "La clave de país ya existe.");
            countries.add(new Document("id_pais", id).append("nombre", name).append("estados", new ArrayList<Document>()));
            database.getCollection("catalogos").replaceOne(Filters.eq("_id", "ubicaciones"), location);
            ctx.status(201).json(Map.of("idPais", id, "nombre", name));
        } else {
            String id = requiredText(body, "idEstado").toUpperCase(Locale.ROOT);
            String name = requiredText(body, "nombre");
            String countryId = requiredText(body, "idPais").toUpperCase(Locale.ROOT);
            Document country = findCountry(countries, countryId);
            if (country == null) throw new ApiException(400, "El país seleccionado no existe.");
            if (findState(countries, id) != null) throw new ApiException(409, "La clave de estado ya existe.");
            country.getList("estados", Document.class).add(new Document("id_estado", id).append("nombre", name));
            database.getCollection("catalogos").replaceOne(Filters.eq("_id", "ubicaciones"), location);
            ctx.status(201).json(Map.of("idEstado", id, "nombre", name,
                    "pais", Map.of("idPais", countryId, "nombre", country.getString("nombre"))));
        }
    }

    private static void updateLocation(Context ctx, boolean countryRequest) throws IOException {
        Map<String, Object> body = body(ctx);
        String id = ctx.pathParam("id").toUpperCase(Locale.ROOT);
        String name = requiredText(body, "nombre");
        Document location = locationDocument();
        List<Document> countries = location.getList("paises", Document.class, new ArrayList<>());
        if (countryRequest) {
            Document country = findCountry(countries, id);
            if (country == null) throw new ApiException(404, "País no encontrado.");
            country.put("nombre", name);
            database.getCollection("catalogos").replaceOne(Filters.eq("_id", "ubicaciones"), location);
            ctx.json(Map.of("idPais", id, "nombre", name));
            return;
        }
        Document state = findState(countries, id);
        if (state == null) throw new ApiException(404, "Estado no encontrado.");
        String countryId = requiredText(body, "idPais").toUpperCase(Locale.ROOT);
        Document previousCountry = findCountryByState(countries, id);
        Document newCountry = findCountry(countries, countryId);
        if (newCountry == null) throw new ApiException(400, "El país seleccionado no existe.");
        if (!previousCountry.getString("id_pais").equals(countryId)) {
            previousCountry.getList("estados", Document.class).remove(state);
            newCountry.getList("estados", Document.class).add(state);
        }
        state.put("nombre", name);
        database.getCollection("catalogos").replaceOne(Filters.eq("_id", "ubicaciones"), location);
        ctx.json(Map.of("idEstado", id, "nombre", name,
                "pais", Map.of("idPais", countryId, "nombre", newCountry.getString("nombre"))));
    }

    private static void deleteCountry(String id) {
        Document location = locationDocument();
        List<Document> countries = location.getList("paises", Document.class, new ArrayList<>());
        Document country = findCountry(countries, id);
        if (country == null) throw new ApiException(404, "País no encontrado.");
        if (!country.getList("estados", Document.class, List.of()).isEmpty()
                || database.getCollection("empleados").countDocuments(
                        Filters.eq("domicilio.estado.pais.id_pais", id)) > 0)
            throw new ApiException(409, "No se puede eliminar un país con estados o empleados relacionados.");
        countries.remove(country);
        database.getCollection("catalogos").replaceOne(Filters.eq("_id", "ubicaciones"), location);
    }

    private static void deleteState(String id) {
        if (database.getCollection("empleados").countDocuments(Filters.eq("domicilio.estado.id_estado", id)) > 0)
            throw new ApiException(409, "No se puede eliminar un estado asignado a empleados.");
        Document location = locationDocument();
        List<Document> countries = location.getList("paises", Document.class, new ArrayList<>());
        Document country = findCountryByState(countries, id);
        if (country == null) throw new ApiException(404, "Estado no encontrado.");
        country.getList("estados", Document.class).removeIf(state -> id.equals(state.getString("id_estado")));
        database.getCollection("catalogos").replaceOne(Filters.eq("_id", "ubicaciones"), location);
    }

    private static void createAssignment(Context ctx) throws IOException {
        Map<String, Object> body = body(ctx);
        long employeeId = requiredLong(body, "idEmpleado");
        Document employee = findEmployee(Long.toString(employeeId));
        if (employee == null) throw new ApiException(400, "El empleado no existe.");
        Document course = courseDocument(body, new ObjectId());
        database.getCollection("empleados").updateOne(Filters.eq("_id", employeeId), Updates.push("cursos", course));
        ctx.status(201).json(Map.of("mensaje", "Curso registrado correctamente", "registrosInsertados", 1));
    }

    private static void updateAssignment(Context ctx) throws IOException {
        ObjectId objectId = parseObjectId(ctx.pathParam("id"));
        Map<String, Object> body = body(ctx);
        long employeeId = requiredLong(body, "idEmpleado");
        Document employee = database.getCollection("empleados")
                .find(Filters.eq("cursos.id_curso", objectId)).first();
        if (employee == null) throw new ApiException(404, "Curso asignado no encontrado.");
        if (employeeId != number(employee.get("_id")))
            throw new ApiException(400, "El curso no pertenece al empleado indicado.");
        Document course = courseDocument(body, objectId);
        UpdateResult result = database.getCollection("empleados").updateOne(
                Filters.and(Filters.eq("_id", employee.get("_id")),
                        Filters.elemMatch("cursos", Filters.eq("id_curso", objectId))),
                Updates.set("cursos.$", course));
        if (result.getMatchedCount() == 0) throw new ApiException(404, "Curso asignado no encontrado.");
        ctx.json(assignmentResponse(employeeId, course, employee));
    }

    private static void deleteAssignment(Context ctx) {
        ObjectId objectId = parseObjectId(ctx.pathParam("id"));
        UpdateResult result = database.getCollection("empleados")
                .updateOne(Filters.eq("cursos.id_curso", objectId), Updates.pull("cursos", Filters.eq("id_curso", objectId)));
        if (result.getModifiedCount() == 0) throw new ApiException(404, "Curso asignado no encontrado.");
        ctx.status(204);
    }

    static Document courseDocument(Map<String, Object> body, ObjectId id) {
        String name = requiredText(body, "nombreCurso").toUpperCase(Locale.ROOT);
        LocalDate start = parseDate(body.get("fechaInicio"), "fechaInicio");
        LocalDate end = parseDate(body.get("fechaFin"), "fechaFin");
        if (start == null) throw new ApiException(400, "fechaInicio es obligatoria.");
        if (end != null && end.isBefore(start)) throw new ApiException(400, "fechaFin no puede ser anterior a fechaInicio.");
        return new Document("id_curso", id)
                .append("nombre_curso", name)
                .append("descripcion", blankToNull(text(body.get("descripcion"))))
                .append("fecha_inicio", toBsonDate(start))
                .append("fecha_fin", end == null ? null : toBsonDate(end))
                .append("constancia", blankToNull(text(body.get("constancia"))));
    }

    private static List<Map<String, Object>> assignments(Long employeeId) {
        List<Map<String, Object>> response = new ArrayList<>();
        for (Document employee : database.getCollection("empleados").find()) {
            if (employeeId != null && !employeeId.equals(number(employee.get("_id")))) continue;
            for (Document course : employee.getList("cursos", Document.class, List.of())) {
                response.add(assignmentResponse(number(employee.get("_id")), course, employee));
            }
        }
        response.sort(Comparator.comparing((Map<String, Object> item) -> item.get("fechaInicio").toString()).reversed());
        return response;
    }

    private static Map<String, Object> assignmentResponse(Long employeeId, Document course, Document employee) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("idEmpleadoCurso", course.getObjectId("id_curso").toHexString());
        response.put("nombreCurso", course.getString("nombre_curso"));
        response.put("descripcion", course.getString("descripcion"));
        response.put("constancia", course.getString("constancia"));
        response.put("fechaInicio", fromBsonDate(course.getDate("fecha_inicio")));
        response.put("fechaFin", fromBsonDate(course.getDate("fecha_fin")));
        response.put("empleado", Map.of("idEmpleado", employeeId,
                "nombre", employee.getString("nombre"),
                "apellidoPaterno", employee.getString("apellido_paterno"),
                "apellidoMaterno", employee.getString("apellido_materno") == null ? "" : employee.getString("apellido_materno")));
        return response;
    }

    private static Document findEmployee(String id) {
        return database.getCollection("empleados").find(Filters.eq("_id", parseLong(id, "idEmpleado"))).first();
    }

    private static long nextNumericId(String collection) {
        Document last = database.getCollection(collection).find().sort(Sorts.descending("_id")).first();
        return last == null ? 1 : number(last.get("_id")) + 1;
    }

    private static List<Document> catalogValues(String id) {
        Document catalog = catalogDocument(id);
        return catalog.getList("valores", Document.class, new ArrayList<>());
    }

    private static Document catalogDocument(String id) {
        Document catalog = database.getCollection("catalogos").find(Filters.eq("_id", id)).first();
        if (catalog == null) throw new ApiException(500, "Falta el documento de catálogo: " + id + ".");
        return catalog;
    }

    private static Document findValue(String catalogId, String id) {
        String key = catalogId.equals("sexos") ? "id_sexo" : "id_estatus";
        return catalogValues(catalogId).stream()
                .filter(value -> id.equalsIgnoreCase(value.getString(key))).findFirst().orElse(null);
    }

    private static Document locationDocument() {
        return catalogDocument("ubicaciones");
    }

    private static Document locationForState(String stateId) {
        Document countriesDoc = locationDocument();
        List<Document> countries = countriesDoc.getList("paises", Document.class, List.of());
        Document country = findCountryByState(countries, stateId);
        Document state = findState(countries, stateId);
        if (country == null || state == null) return null;
        return new Document("id_estado", state.getString("id_estado"))
                .append("nombre", state.getString("nombre"))
                .append("id_pais", country.getString("id_pais"))
                .append("pais_nombre", country.getString("nombre"));
    }

    private static Document findCountry(List<Document> countries, String id) {
        return countries.stream().filter(country -> id.equalsIgnoreCase(country.getString("id_pais"))).findFirst().orElse(null);
    }

    private static Document findState(List<Document> countries, String id) {
        for (Document country : countries) {
            Document state = country.getList("estados", Document.class, List.of()).stream()
                    .filter(item -> id.equalsIgnoreCase(item.getString("id_estado"))).findFirst().orElse(null);
            if (state != null) return state;
        }
        return null;
    }

    private static Document findCountryByState(List<Document> countries, String id) {
        return countries.stream().filter(country -> country.getList("estados", Document.class, List.of()).stream()
                .anyMatch(state -> id.equalsIgnoreCase(state.getString("id_estado")))).findFirst().orElse(null);
    }

    private static Map<String, Object> body(Context ctx) throws IOException {
        try {
            Map<String, Object> body = JSON.readValue(ctx.body(), new TypeReference<>() { });
            if (body == null) throw new ApiException(400, "El cuerpo JSON es obligatorio.");
            return body;
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw new ApiException(400, "El cuerpo de la solicitud debe ser JSON válido.");
        }
    }

    private static String requiredText(Map<String, Object> body, String key) {
        String value = text(body.get(key));
        if (value == null || value.isBlank()) throw new ApiException(400, "El campo " + key + " es obligatorio.");
        return value.trim();
    }

    private static Long requiredLong(Map<String, Object> body, String key) {
        Long value = nullableLong(body.get(key), key);
        if (value == null) throw new ApiException(400, "El campo " + key + " es obligatorio.");
        return value;
    }

    private static Long nullableLong(Object value, String field) {
        if (value == null || value.toString().isBlank()) return null;
        try {
            return value instanceof Number number ? number.longValue() : Long.parseLong(value.toString());
        } catch (NumberFormatException exception) {
            throw new ApiException(400, "El campo " + field + " debe ser numérico.");
        }
    }

    private static long parseLong(String value, String field) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new ApiException(400, "El campo " + field + " debe ser numérico.");
        }
    }

    private static ObjectId parseObjectId(String value) {
        if (!ObjectId.isValid(value)) throw new ApiException(400, "El identificador del curso no es válido.");
        return new ObjectId(value);
    }

    private static LocalDate parseDate(Object value, String field) {
        if (value == null || value.toString().isBlank()) return null;
        try {
            return LocalDate.parse(value.toString());
        } catch (DateTimeParseException exception) {
            throw new ApiException(400, "El campo " + field + " debe tener formato AAAA-MM-DD.");
        }
    }

    static Date toBsonDate(LocalDate value) {
        return Date.from(value.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    private static String fromBsonDate(Date value) {
        return value == null ? null : value.toInstant().atZone(ZoneOffset.UTC).toLocalDate().toString();
    }

    private static String text(Object value) {
        return value == null ? null : value.toString();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private static long number(Object value) {
        if (value instanceof Number number) return number.longValue();
        throw new ApiException(500, "Se encontró un identificador no numérico en MongoDB.");
    }

    private static void serveStatic(Context ctx, String path) throws IOException {
        if (path == null || path.isBlank()) path = "index.html";
        path = path.replaceFirst("^/+", "");
        if (path.contains("..") || path.contains("\\")) {
            ctx.status(404);
            return;
        }
        String resource = "public/" + path;
        try (InputStream stream = VenusApplication.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                ctx.status(404);
                return;
            }
            String contentType = path.endsWith(".html") ? "text/html; charset=UTF-8"
                    : path.endsWith(".js") ? "text/javascript; charset=UTF-8"
                    : path.endsWith(".css") ? "text/css; charset=UTF-8"
                    : "application/octet-stream";
            ctx.contentType(contentType).result(new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private record Session(String username, Instant expiresAt) { }

    private static final class ApiException extends RuntimeException {
        private final int status;

        private ApiException(int status, String message) {
            super(message);
            this.status = status;
        }
    }
}
