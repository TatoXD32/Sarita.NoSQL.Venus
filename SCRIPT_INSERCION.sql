
-- =====================================================
-- 3. SCRIPT DE INSERCIÓN (SEEDER)
-- =====================================================

-- Estatus (5)
INSERT INTO cat_estatus (id_estatus, descripcion, categoria) VALUES ('ACTIVO', 'Operando normalmente', 'GENERAL');
INSERT INTO cat_estatus (id_estatus, descripcion, categoria) VALUES ('INACTIVO', 'Fuera de operación', 'GENERAL');
INSERT INTO cat_estatus (id_estatus, descripcion, categoria) VALUES ('ENFERMO', 'Incapacidad médica', 'EMPLEADO');
INSERT INTO cat_estatus (id_estatus, descripcion, categoria) VALUES ('COMISION', 'De comisión laboral', 'EMPLEADO');
INSERT INTO cat_estatus (id_estatus, descripcion, categoria) VALUES ('BAJA', 'Baja definitiva', 'EMPLEADO');

-- Sexo (3)
INSERT INTO sexo (id_sexo, descripcion, id_estatus) VALUES ('M', 'Masculino', 'ACTIVO');
INSERT INTO sexo (id_sexo, descripcion, id_estatus) VALUES ('F', 'Femenino', 'ACTIVO');
INSERT INTO sexo (id_sexo, descripcion, id_estatus) VALUES ('X', 'No Binario', 'ACTIVO');

-- País (3)
INSERT INTO pais (id_pais, nombre, id_estatus) VALUES ('MX', 'México', 'ACTIVO');
INSERT INTO pais (id_pais, nombre, id_estatus) VALUES ('US', 'Estados Unidos', 'ACTIVO');
INSERT INTO pais (id_pais, nombre, id_estatus) VALUES ('CA', 'Canadá', 'ACTIVO');

-- Estado (5)
INSERT INTO estado (id_estado, nombre, id_pais, id_estatus) VALUES ('MEX', 'Estado de México', 'MX', 'ACTIVO');
INSERT INTO estado (id_estado, nombre, id_pais, id_estatus) VALUES ('JAL', 'Jalisco', 'MX', 'ACTIVO');
INSERT INTO estado (id_estado, nombre, id_pais, id_estatus) VALUES ('CDMX', 'Ciudad de México', 'MX', 'ACTIVO');
INSERT INTO estado (id_estado, nombre, id_pais, id_estatus) VALUES ('NLE', 'Nuevo León', 'MX', 'ACTIVO');
INSERT INTO estado (id_estado, nombre, id_pais, id_estatus) VALUES ('TX', 'Texas', 'US', 'ACTIVO');

-- Cursos (5)
INSERT INTO curso (nombre, descripcion, id_estatus) VALUES ('Java Básico', 'Fundamentos de Java y POO', 'ACTIVO');
INSERT INTO curso (nombre, descripcion, id_estatus) VALUES ('Seguridad Web', 'OWASP y vulnerabilidades', 'ACTIVO');
INSERT INTO curso (nombre, descripcion, id_estatus) VALUES ('Flutter', 'Desarrollo de Apps móviles', 'ACTIVO');
INSERT INTO curso (nombre, descripcion, id_estatus) VALUES ('Bases de Datos', 'SQL avanzado y PL/SQL', 'ACTIVO');
INSERT INTO curso (nombre, descripcion, id_estatus) VALUES ('Administración Linux', 'Comandos y redes', 'ACTIVO');

-- Departamentos (5)
INSERT INTO departamento (descripcion, id_estatus) VALUES ('Recursos Humanos', 'ACTIVO');
INSERT INTO departamento (descripcion, id_estatus) VALUES ('Tecnologías de la Información', 'ACTIVO');
INSERT INTO departamento (descripcion, id_estatus) VALUES ('Ventas', 'ACTIVO');
INSERT INTO departamento (descripcion, id_estatus) VALUES ('Marketing', 'ACTIVO');
INSERT INTO departamento (descripcion, id_estatus) VALUES ('Finanzas', 'ACTIVO');

-- Puestos (5)
INSERT INTO puesto (descripcion_puesto, id_estatus) VALUES ('Desarrollador Backend', 'ACTIVO');
INSERT INTO puesto (descripcion_puesto, id_estatus) VALUES ('Desarrollador Frontend', 'ACTIVO');
INSERT INTO puesto (descripcion_puesto, id_estatus) VALUES ('Administrador de BD', 'ACTIVO');
INSERT INTO puesto (descripcion_puesto, id_estatus) VALUES ('Gerente de Proyecto', 'ACTIVO');
INSERT INTO puesto (descripcion_puesto, id_estatus) VALUES ('Analista QA', 'ACTIVO');

-- Empleado 1
INSERT INTO domicilio (calle, codigo_postal, numero_exterior, numero_interior, id_estado) VALUES ('Calle Torres', '86654', '172', NULL, 'CDMX');
INSERT INTO empleado (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_domicilio, id_depto, id_puesto, id_estatus) VALUES ('Ana', 'Garcia', 'Aguilar', TO_DATE('1999-05-21', 'YYYY-MM-DD'), 'ana.garcia1@empresa.mx', '5522045550', TO_DATE('2018-08-06', 'YYYY-MM-DD'), 'F', 1, 2, 4, 'ENFERMO');
INSERT INTO credencial (id_empleado, usuario, contrasena_hash) VALUES (1, 'anagar1', 'hash_sha256_1');
INSERT INTO empleado_curso (id_empleado, id_curso, fecha_inicio, fecha_fin) VALUES (1, 1, TO_DATE('2023-10-01', 'YYYY-MM-DD'), NULL);

-- Empleado 2
INSERT INTO domicilio (calle, codigo_postal, numero_exterior, numero_interior, id_estado) VALUES ('Calle Torres', '90336', '829', NULL, 'MEX');
INSERT INTO empleado (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_domicilio, id_depto, id_puesto, id_estatus) VALUES ('Luis', 'Reyes', 'Cruz', TO_DATE('1989-12-24', 'YYYY-MM-DD'), 'luis.reyes2@empresa.mx', '5545224240', TO_DATE('2023-03-02', 'YYYY-MM-DD'), 'M', 2, 2, 1, 'ACTIVO');
INSERT INTO credencial (id_empleado, usuario, contrasena_hash) VALUES (2, 'luirey2', 'hash_sha256_2');
INSERT INTO empleado_curso (id_empleado, id_curso, fecha_inicio, fecha_fin) VALUES (2, 2, TO_DATE('2023-10-01', 'YYYY-MM-DD'), NULL);

-- Empleado 3
INSERT INTO domicilio (calle, codigo_postal, numero_exterior, numero_interior, id_estado) VALUES ('Calle Ortiz', '65103', '452', NULL, 'NLE');
INSERT INTO empleado (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_domicilio, id_depto, id_puesto, id_estatus) VALUES ('Marta', 'Ramos', 'Silva', TO_DATE('2000-05-01', 'YYYY-MM-DD'), 'marta.ramos3@empresa.mx', '5521940733', TO_DATE('2019-11-24', 'YYYY-MM-DD'), 'F', 3, 5, 3, 'ACTIVO');
INSERT INTO credencial (id_empleado, usuario, contrasena_hash) VALUES (3, 'marram3', 'hash_sha256_3');
INSERT INTO empleado_curso (id_empleado, id_curso, fecha_inicio, fecha_fin) VALUES (3, 2, TO_DATE('2023-10-01', 'YYYY-MM-DD'), NULL);

-- Empleado 4
INSERT INTO domicilio (calle, codigo_postal, numero_exterior, numero_interior, id_estado) VALUES ('Calle Perez', '32232', '96', NULL, 'CDMX');
INSERT INTO empleado (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_domicilio, id_depto, id_puesto, id_estatus) VALUES ('Pedro', 'Vargas', 'Lopez', TO_DATE('1997-07-14', 'YYYY-MM-DD'), 'pedro.vargas4@empresa.mx', '5563279395', TO_DATE('2019-08-08', 'YYYY-MM-DD'), 'M', 4, 5, 1, 'COMISION');
INSERT INTO credencial (id_empleado, usuario, contrasena_hash) VALUES (4, 'pedvar4', 'hash_sha256_4');
INSERT INTO empleado_curso (id_empleado, id_curso, fecha_inicio, fecha_fin) VALUES (4, 1, TO_DATE('2023-01-10', 'YYYY-MM-DD'), TO_DATE('2023-03-15', 'YYYY-MM-DD'));

-- Empleado 5
INSERT INTO domicilio (calle, codigo_postal, numero_exterior, numero_interior, id_estado) VALUES ('Calle Garcia', '62382', '490', NULL, 'CDMX');
INSERT INTO empleado (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_domicilio, id_depto, id_puesto, id_estatus) VALUES ('Sofia', 'Castillo', 'Hernandez', TO_DATE('2000-12-02', 'YYYY-MM-DD'), 'sofia.castillo5@empresa.mx', '5544012051', TO_DATE('2023-10-16', 'YYYY-MM-DD'), 'F', 5, 4, 1, 'ACTIVO');
INSERT INTO credencial (id_empleado, usuario, contrasena_hash) VALUES (5, 'sofcas5', 'hash_sha256_5');
INSERT INTO empleado_curso (id_empleado, id_curso, fecha_inicio, fecha_fin) VALUES (5, 5, TO_DATE('2023-10-01', 'YYYY-MM-DD'), NULL);

-- Empleado 6
INSERT INTO domicilio (calle, codigo_postal, numero_exterior, numero_interior, id_estado) VALUES ('Calle Gonzalez', '91502', '720', NULL, 'MEX');
INSERT INTO empleado (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_domicilio, id_depto, id_puesto, id_estatus) VALUES ('Carlos', 'Gutierrez', 'Medina', TO_DATE('1984-06-22', 'YYYY-MM-DD'), 'carlos.gutierrez6@empresa.mx', '5576939048', TO_DATE('2018-03-11', 'YYYY-MM-DD'), 'M', 6, 3, 3, 'BAJA');
INSERT INTO credencial (id_empleado, usuario, contrasena_hash) VALUES (6, 'cargut6', 'hash_sha256_6');
INSERT INTO empleado_curso (id_empleado, id_curso, fecha_inicio, fecha_fin) VALUES (6, 3, TO_DATE('2023-01-10', 'YYYY-MM-DD'), TO_DATE('2023-03-15', 'YYYY-MM-DD'));

-- Empleado 7
INSERT INTO domicilio (calle, codigo_postal, numero_exterior, numero_interior, id_estado) VALUES ('Calle Ramos', '91916', '787', NULL, 'CDMX');
INSERT INTO empleado (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_domicilio, id_depto, id_puesto, id_estatus) VALUES ('Lucia', 'Chavez', 'Morales', TO_DATE('1986-10-23', 'YYYY-MM-DD'), 'lucia.chavez7@empresa.mx', '5523361046', TO_DATE('2018-12-18', 'YYYY-MM-DD'), 'F', 7, 2, 3, 'ACTIVO');
INSERT INTO credencial (id_empleado, usuario, contrasena_hash) VALUES (7, 'luccha7', 'hash_sha256_7');
INSERT INTO empleado_curso (id_empleado, id_curso, fecha_inicio, fecha_fin) VALUES (7, 2, TO_DATE('2023-10-01', 'YYYY-MM-DD'), NULL);

-- Empleado 8
INSERT INTO domicilio (calle, codigo_postal, numero_exterior, numero_interior, id_estado) VALUES ('Calle Perez', '46710', '725', NULL, 'MEX');
INSERT INTO empleado (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_domicilio, id_depto, id_puesto, id_estatus) VALUES ('Jorge', 'Garcia', 'Martinez', TO_DATE('1980-01-15', 'YYYY-MM-DD'), 'jorge.garcia8@empresa.mx', '5574306257', TO_DATE('2018-02-02', 'YYYY-MM-DD'), 'M', 8, 2, 5, 'ACTIVO');
INSERT INTO credencial (id_empleado, usuario, contrasena_hash) VALUES (8, 'jorgar8', 'hash_sha256_8');
INSERT INTO empleado_curso (id_empleado, id_curso, fecha_inicio, fecha_fin) VALUES (8, 3, TO_DATE('2023-10-01', 'YYYY-MM-DD'), NULL);

-- Empleado 9
INSERT INTO domicilio (calle, codigo_postal, numero_exterior, numero_interior, id_estado) VALUES ('Calle Ramos', '30391', '845', NULL, 'MEX');
INSERT INTO empleado (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_domicilio, id_depto, id_puesto, id_estatus) VALUES ('Elena', 'Aguilar', 'Mendez', TO_DATE('1983-02-05', 'YYYY-MM-DD'), 'elena.aguilar9@empresa.mx', '5530464687', TO_DATE('2021-12-19', 'YYYY-MM-DD'), 'F', 9, 4, 1, 'COMISION');
INSERT INTO credencial (id_empleado, usuario, contrasena_hash) VALUES (9, 'eleagu9', 'hash_sha256_9');
INSERT INTO empleado_curso (id_empleado, id_curso, fecha_inicio, fecha_fin) VALUES (9, 5, TO_DATE('2023-10-01', 'YYYY-MM-DD'), NULL);

-- Empleado 10
INSERT INTO domicilio (calle, codigo_postal, numero_exterior, numero_interior, id_estado) VALUES ('Calle Ortiz', '92842', '186', NULL, 'MEX');
INSERT INTO empleado (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_domicilio, id_depto, id_puesto, id_estatus) VALUES ('Raul', 'Ortiz', 'Cruz', TO_DATE('1991-06-27', 'YYYY-MM-DD'), 'raul.ortiz10@empresa.mx', '5583662562', TO_DATE('2019-07-26', 'YYYY-MM-DD'), 'M', 10, 2, 4, 'ACTIVO');
INSERT INTO credencial (id_empleado, usuario, contrasena_hash) VALUES (10, 'rauort10', 'hash_sha256_10');
INSERT INTO empleado_curso (id_empleado, id_curso, fecha_inicio, fecha_fin) VALUES (10, 3, TO_DATE('2023-01-10', 'YYYY-MM-DD'), TO_DATE('2023-03-15', 'YYYY-MM-DD'));

COMMIT;