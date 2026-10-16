package com.mycompany.sarita.nosql.venus.security;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

@Path("auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Resource(lookup = "jdbc/orcl")
    private DataSource dataSource;

    @Context
    private HttpServletRequest request;

    public static class LoginRequest {
        public String usuario;
        public String contrasena;
    }

    @POST
    @Path("login")
    public Response login(LoginRequest datos) {

        if (datos == null || datos.usuario == null
                || datos.contrasena == null
                || datos.usuario.isBlank()
                || datos.contrasena.isEmpty()) {
            return Response.status(400)
                    .entity(Map.of("error", "Datos incompletos"))
                    .build();
        }

        String sql = """
                SELECT CONTRASENA_HASH
                FROM CREDENCIALES
                WHERE USUARIO = ?
                """;

        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, datos.usuario.trim());

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (!resultado.next()
                        || !PasswordUtil.verify(
                                datos.contrasena,
                                resultado.getString("CONTRASENA_HASH"))) {

                    return Response.status(401)
                            .entity(Map.of("error", "Credenciales incorrectas"))
                            .build();
                }
            }

            HttpSession anterior = request.getSession(false);
            if (anterior != null) {
                anterior.invalidate();
            }

            HttpSession sesion = request.getSession(true);
            sesion.setAttribute("usuario", datos.usuario.trim());
            sesion.setMaxInactiveInterval(1800);

            return Response.ok(Map.of(
                    "mensaje", "Inicio de sesión correcto",
                    "usuario", datos.usuario.trim()
            )).build();

        } catch (Exception e) {
            return Response.status(500)
                    .entity(Map.of("error", "Error de autenticación"))
                    .build();
        }
    }

    @GET
    @Path("sesion")
    public Response sesion() {

        HttpSession sesion = request.getSession(false);

        if (sesion == null || sesion.getAttribute("usuario") == null) {
            return Response.status(401)
                    .entity(Map.of("autenticado", false))
                    .build();
        }

        return Response.ok(Map.of(
                "autenticado", true,
                "usuario", sesion.getAttribute("usuario")
        )).build();
    }

    @POST
    @Path("logout")
    public Response logout() {

        HttpSession sesion = request.getSession(false);

        if (sesion != null) {
            sesion.invalidate();
        }

        return Response.ok(Map.of(
                "mensaje", "Sesión cerrada"
        )).build();
    }
}
