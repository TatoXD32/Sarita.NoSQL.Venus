package com.mycompany.sarita.nosql.venus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

class VenusApplicationTest {

    @Test
    void asignacionDeCursoUsaObjectIdYFechasBson() {
        Map<String, Object> body = new HashMap<>();
        body.put("nombreCurso", "Java Básico");
        body.put("fechaInicio", "2024-01-02");
        body.put("fechaFin", null);

        Document course = VenusApplication.courseDocument(body, new ObjectId());

        assertInstanceOf(ObjectId.class, course.get("id_curso"));
        assertInstanceOf(Date.class, course.get("fecha_inicio"));
        assertEquals(Date.from(Instant.parse("2024-01-02T00:00:00Z")), course.get("fecha_inicio"));
        assertNull(course.get("fecha_fin"));
    }

    @Test
    void fechaLocalSeConvierteAFechaBsonEnUtc() {
        Date bsonDate = VenusApplication.toBsonDate(LocalDate.of(2024, 5, 6));

        assertEquals(Instant.parse("2024-05-06T00:00:00Z"), bsonDate.toInstant());
        assertEquals(LocalDate.of(2024, 5, 6), bsonDate.toInstant().atZone(ZoneOffset.UTC).toLocalDate());
    }
}
