package com.mycompany.sarita.nosql.venus.security;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Map;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext context) throws IOException {

        String ruta = context.getUriInfo().getPath();

        if (ruta.equals("auth/login")
                || ruta.equals("auth/sesion")
                || ruta.equals("auth/logout")) {
            return;
        }

        String metodo = context.getMethod();

        if (!metodo.equals("GET") && !metodo.equals("HEAD")
                && !metodo.equals("OPTIONS")) {

            String origen = request.getHeader("Origin");
            String origenEsperado = request.getScheme() + "://"
                    + request.getServerName()
                    + ((request.getServerPort() == 80 && request.getScheme().equals("http"))
                    || (request.getServerPort() == 443 && request.getScheme().equals("https"))
                    ? "" : ":" + request.getServerPort());

            if (origen == null || !origen.equals(origenEsperado)) {
                context.abortWith(
                        Response.status(Response.Status.FORBIDDEN)
                                .entity(Map.of("error", "Origen no autorizado"))
                                .build()
                );
                return;
            }
        }

        HttpSession sesion = request.getSession(false);

        if (sesion == null || sesion.getAttribute("usuario") == null) {
            context.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity(Map.of(
                                    "error", "Debes iniciar sesión"
                            ))
                            .build()
            );
        }
    }
}
