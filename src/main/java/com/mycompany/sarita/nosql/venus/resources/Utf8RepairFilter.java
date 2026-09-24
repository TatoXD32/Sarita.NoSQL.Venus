package com.mycompany.sarita.nosql.venus.resources;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Provider
public class Utf8RepairFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object entity = responseContext.getEntity();
        if (entity == null) {
            return;
        }
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        responseContext.setEntity(repairEntity(entity, visited));
    }

    private Object repairEntity(Object value, Set<Object> visited) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            return repairText(text);
        }
        if (visited.contains(value)) {
            return value;
        }
        visited.add(value);

        if (value instanceof Collection<?> collection) {
            List<Object> repaired = new ArrayList<>();
            for (Object item : collection) {
                repaired.add(repairEntity(item, visited));
            }
            return repaired;
        }
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> repaired = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                repaired.put(repairEntity(entry.getKey(), visited), repairEntity(entry.getValue(), visited));
            }
            return repaired;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            Object[] repaired = new Object[length];
            for (int i = 0; i < length; i++) {
                repaired[i] = repairEntity(Array.get(value, i), visited);
            }
            return repaired;
        }
        if (value.getClass().isRecord()) {
            Map<String, Object> repaired = new LinkedHashMap<>();
            for (java.lang.reflect.RecordComponent component : value.getClass().getRecordComponents()) {
                try {
                    Method accessor = component.getAccessor();
                    accessor.setAccessible(true);
                    repaired.put(component.getName(), repairEntity(accessor.invoke(value), visited));
                } catch (ReflectiveOperationException e) {
                    return value;
                }
            }
            return repaired;
        }
        Class<?> type = value.getClass();
        if (type.getPackage() != null && type.getPackageName().startsWith("java.")) {
            return value;
        }
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object current = field.get(value);
                Object repaired = repairEntity(current, visited);
                if (repaired != current) {
                    field.set(value, repaired);
                }
            } catch (IllegalAccessException ignored) {
                // Ignored: some fields are not writable but the serializer still works.
            }
        }
        return value;
    }

    private String repairText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (!containsMojibake(value)) {
            return value;
        }
        String repaired = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        return containsSpanishChars(repaired) ? repaired : value;
    }

    private boolean containsMojibake(String value) {
        return value.contains("Ã") || value.contains("Â") || value.contains("â") || value.contains("Ð") || value.contains("ð");
    }

    private boolean containsSpanishChars(String value) {
        return value.chars().anyMatch(ch -> "áéíóúÁÉÍÓÚñÑüÜäëïöüÄËÏÖÜ".indexOf(ch) >= 0);
    }
}
