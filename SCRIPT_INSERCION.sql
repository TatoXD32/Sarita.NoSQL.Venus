-- Datos ficticios para esquema nuevo vacio. No ejecutar sobre SARITA existente.
-- No se crean credenciales de prueba. El administrador se aprovisiona desde el backend seguro.

INSERT INTO ESTATUS_EMPLEADOS (ID_ESTATUS, DESCRIPCION) VALUES ('ACTIVO', 'Operando normalmente');
INSERT INTO ESTATUS_EMPLEADOS (ID_ESTATUS, DESCRIPCION) VALUES ('INACTIVO', 'Fuera de operación');
INSERT INTO ESTATUS_EMPLEADOS (ID_ESTATUS, DESCRIPCION) VALUES ('ENFERMO', 'Incapacidad médica');
INSERT INTO ESTATUS_EMPLEADOS (ID_ESTATUS, DESCRIPCION) VALUES ('COMISION', 'De comisión laboral');
INSERT INTO ESTATUS_EMPLEADOS (ID_ESTATUS, DESCRIPCION) VALUES ('BAJA', 'Baja definitiva');
INSERT INTO SEXOS (ID_SEXO, DESCRIPCION) VALUES ('M', 'Masculino');
INSERT INTO SEXOS (ID_SEXO, DESCRIPCION) VALUES ('F', 'Femenino');
INSERT INTO SEXOS (ID_SEXO, DESCRIPCION) VALUES ('X', 'No Binario');
INSERT INTO PAISES (ID_PAIS, NOMBRE) VALUES ('MX', 'México');
INSERT INTO PAISES (ID_PAIS, NOMBRE) VALUES ('US', 'Estados Unidos');
INSERT INTO PAISES (ID_PAIS, NOMBRE) VALUES ('CA', 'Canadá');
INSERT INTO ESTADOS (ID_ESTADO, NOMBRE, ID_PAIS) VALUES ('MEX', 'Estado de México', 'MX');
INSERT INTO ESTADOS (ID_ESTADO, NOMBRE, ID_PAIS) VALUES ('JAL', 'Jalisco', 'MX');
INSERT INTO ESTADOS (ID_ESTADO, NOMBRE, ID_PAIS) VALUES ('CDMX', 'Ciudad de México', 'MX');
INSERT INTO ESTADOS (ID_ESTADO, NOMBRE, ID_PAIS) VALUES ('NLE', 'Nuevo León', 'MX');
INSERT INTO ESTADOS (ID_ESTADO, NOMBRE, ID_PAIS) VALUES ('TX', 'Texas', 'US');
INSERT INTO DEPARTAMENTOS (DESCRIPCION) VALUES ('Recursos Humanos');
INSERT INTO DEPARTAMENTOS (DESCRIPCION) VALUES ('Tecnologías de la Información');
INSERT INTO DEPARTAMENTOS (DESCRIPCION) VALUES ('Ventas');
INSERT INTO DEPARTAMENTOS (DESCRIPCION) VALUES ('Marketing');
INSERT INTO DEPARTAMENTOS (DESCRIPCION) VALUES ('Finanzas');
INSERT INTO PUESTOS (DESCRIPCION_PUESTO) VALUES ('Desarrollador Backend');
INSERT INTO PUESTOS (DESCRIPCION_PUESTO) VALUES ('Desarrollador Frontend');
INSERT INTO PUESTOS (DESCRIPCION_PUESTO) VALUES ('Administrador de BD');
INSERT INTO PUESTOS (DESCRIPCION_PUESTO) VALUES ('Gerente de Proyecto');
INSERT INTO PUESTOS (DESCRIPCION_PUESTO) VALUES ('Analista QA');

-- Empleado 1
INSERT INTO EMPLEADOS (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_depto, id_puesto, id_estatus) VALUES ('Ana', 'Garcia', 'Aguilar', TO_DATE('1999-05-21', 'YYYY-MM-DD'), 'ana.garcia1@empresa.mx', '5522045550', TO_DATE('2018-08-06', 'YYYY-MM-DD'), 'F', 2, 4, 'ENFERMO');
INSERT INTO DOMICILIOS (ID_EMPLEADO, CALLE, CODIGO_POSTAL, NUMERO_EXTERIOR, NUMERO_INTERIOR, ID_ESTADO) SELECT ID_EMPLEADO, 'Calle Torres', '86654', '172', NULL, 'CDMX' FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'ana.garcia1@empresa.mx';
INSERT INTO EMPLEADO_CURSOS (ID_EMPLEADO, NOMBRE_CURSO, DESCRIPCION, FECHA_INICIO, FECHA_FIN) SELECT ID_EMPLEADO, 'Java Básico', 'Fundamentos de Java y POO', TO_DATE('2023-10-01', 'YYYY-MM-DD'), NULL FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'ana.garcia1@empresa.mx';

-- Empleado 2
INSERT INTO EMPLEADOS (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_depto, id_puesto, id_estatus) VALUES ('Luis', 'Reyes', 'Cruz', TO_DATE('1989-12-24', 'YYYY-MM-DD'), 'luis.reyes2@empresa.mx', '5545224240', TO_DATE('2023-03-02', 'YYYY-MM-DD'), 'M', 2, 1, 'ACTIVO');
INSERT INTO DOMICILIOS (ID_EMPLEADO, CALLE, CODIGO_POSTAL, NUMERO_EXTERIOR, NUMERO_INTERIOR, ID_ESTADO) SELECT ID_EMPLEADO, 'Calle Torres', '90336', '829', NULL, 'MEX' FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'luis.reyes2@empresa.mx';
INSERT INTO EMPLEADO_CURSOS (ID_EMPLEADO, NOMBRE_CURSO, DESCRIPCION, FECHA_INICIO, FECHA_FIN) SELECT ID_EMPLEADO, 'Seguridad Web', 'OWASP y vulnerabilidades', TO_DATE('2023-10-01', 'YYYY-MM-DD'), NULL FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'luis.reyes2@empresa.mx';

-- Empleado 3
INSERT INTO EMPLEADOS (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_depto, id_puesto, id_estatus) VALUES ('Marta', 'Ramos', 'Silva', TO_DATE('2000-05-01', 'YYYY-MM-DD'), 'marta.ramos3@empresa.mx', '5521940733', TO_DATE('2019-11-24', 'YYYY-MM-DD'), 'F', 5, 3, 'ACTIVO');
INSERT INTO DOMICILIOS (ID_EMPLEADO, CALLE, CODIGO_POSTAL, NUMERO_EXTERIOR, NUMERO_INTERIOR, ID_ESTADO) SELECT ID_EMPLEADO, 'Calle Ortiz', '65103', '452', NULL, 'NLE' FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'marta.ramos3@empresa.mx';
INSERT INTO EMPLEADO_CURSOS (ID_EMPLEADO, NOMBRE_CURSO, DESCRIPCION, FECHA_INICIO, FECHA_FIN) SELECT ID_EMPLEADO, 'Seguridad Web', 'OWASP y vulnerabilidades', TO_DATE('2023-10-01', 'YYYY-MM-DD'), NULL FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'marta.ramos3@empresa.mx';

-- Empleado 4
INSERT INTO EMPLEADOS (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_depto, id_puesto, id_estatus) VALUES ('Pedro', 'Vargas', 'Lopez', TO_DATE('1997-07-14', 'YYYY-MM-DD'), 'pedro.vargas4@empresa.mx', '5563279395', TO_DATE('2019-08-08', 'YYYY-MM-DD'), 'M', 5, 1, 'COMISION');
INSERT INTO DOMICILIOS (ID_EMPLEADO, CALLE, CODIGO_POSTAL, NUMERO_EXTERIOR, NUMERO_INTERIOR, ID_ESTADO) SELECT ID_EMPLEADO, 'Calle Perez', '32232', '96', NULL, 'CDMX' FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'pedro.vargas4@empresa.mx';
INSERT INTO EMPLEADO_CURSOS (ID_EMPLEADO, NOMBRE_CURSO, DESCRIPCION, FECHA_INICIO, FECHA_FIN) SELECT ID_EMPLEADO, 'Java Básico', 'Fundamentos de Java y POO', TO_DATE('2023-01-10', 'YYYY-MM-DD'), TO_DATE('2023-03-15', 'YYYY-MM-DD') FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'pedro.vargas4@empresa.mx';

-- Empleado 5
INSERT INTO EMPLEADOS (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_depto, id_puesto, id_estatus) VALUES ('Sofia', 'Castillo', 'Hernandez', TO_DATE('2000-12-02', 'YYYY-MM-DD'), 'sofia.castillo5@empresa.mx', '5544012051', TO_DATE('2023-10-16', 'YYYY-MM-DD'), 'F', 4, 1, 'ACTIVO');
INSERT INTO DOMICILIOS (ID_EMPLEADO, CALLE, CODIGO_POSTAL, NUMERO_EXTERIOR, NUMERO_INTERIOR, ID_ESTADO) SELECT ID_EMPLEADO, 'Calle Garcia', '62382', '490', NULL, 'CDMX' FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'sofia.castillo5@empresa.mx';
INSERT INTO EMPLEADO_CURSOS (ID_EMPLEADO, NOMBRE_CURSO, DESCRIPCION, FECHA_INICIO, FECHA_FIN) SELECT ID_EMPLEADO, 'Administración Linux', 'Comandos y redes', TO_DATE('2023-10-01', 'YYYY-MM-DD'), NULL FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'sofia.castillo5@empresa.mx';

-- Empleado 6
INSERT INTO EMPLEADOS (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_depto, id_puesto, id_estatus) VALUES ('Carlos', 'Gutierrez', 'Medina', TO_DATE('1984-06-22', 'YYYY-MM-DD'), 'carlos.gutierrez6@empresa.mx', '5576939048', TO_DATE('2018-03-11', 'YYYY-MM-DD'), 'M', 3, 3, 'BAJA');
INSERT INTO DOMICILIOS (ID_EMPLEADO, CALLE, CODIGO_POSTAL, NUMERO_EXTERIOR, NUMERO_INTERIOR, ID_ESTADO) SELECT ID_EMPLEADO, 'Calle Gonzalez', '91502', '720', NULL, 'MEX' FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'carlos.gutierrez6@empresa.mx';
INSERT INTO EMPLEADO_CURSOS (ID_EMPLEADO, NOMBRE_CURSO, DESCRIPCION, FECHA_INICIO, FECHA_FIN) SELECT ID_EMPLEADO, 'Flutter', 'Desarrollo de Apps móviles', TO_DATE('2023-01-10', 'YYYY-MM-DD'), TO_DATE('2023-03-15', 'YYYY-MM-DD') FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'carlos.gutierrez6@empresa.mx';

-- Empleado 7
INSERT INTO EMPLEADOS (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_depto, id_puesto, id_estatus) VALUES ('Lucia', 'Chavez', 'Morales', TO_DATE('1986-10-23', 'YYYY-MM-DD'), 'lucia.chavez7@empresa.mx', '5523361046', TO_DATE('2018-12-18', 'YYYY-MM-DD'), 'F', 2, 3, 'ACTIVO');
INSERT INTO DOMICILIOS (ID_EMPLEADO, CALLE, CODIGO_POSTAL, NUMERO_EXTERIOR, NUMERO_INTERIOR, ID_ESTADO) SELECT ID_EMPLEADO, 'Calle Ramos', '91916', '787', NULL, 'CDMX' FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'lucia.chavez7@empresa.mx';
INSERT INTO EMPLEADO_CURSOS (ID_EMPLEADO, NOMBRE_CURSO, DESCRIPCION, FECHA_INICIO, FECHA_FIN) SELECT ID_EMPLEADO, 'Seguridad Web', 'OWASP y vulnerabilidades', TO_DATE('2023-10-01', 'YYYY-MM-DD'), NULL FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'lucia.chavez7@empresa.mx';

-- Empleado 8
INSERT INTO EMPLEADOS (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_depto, id_puesto, id_estatus) VALUES ('Jorge', 'Garcia', 'Martinez', TO_DATE('1980-01-15', 'YYYY-MM-DD'), 'jorge.garcia8@empresa.mx', '5574306257', TO_DATE('2018-02-02', 'YYYY-MM-DD'), 'M', 2, 5, 'ACTIVO');
INSERT INTO DOMICILIOS (ID_EMPLEADO, CALLE, CODIGO_POSTAL, NUMERO_EXTERIOR, NUMERO_INTERIOR, ID_ESTADO) SELECT ID_EMPLEADO, 'Calle Perez', '46710', '725', NULL, 'MEX' FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'jorge.garcia8@empresa.mx';
INSERT INTO EMPLEADO_CURSOS (ID_EMPLEADO, NOMBRE_CURSO, DESCRIPCION, FECHA_INICIO, FECHA_FIN) SELECT ID_EMPLEADO, 'Flutter', 'Desarrollo de Apps móviles', TO_DATE('2023-10-01', 'YYYY-MM-DD'), NULL FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'jorge.garcia8@empresa.mx';

-- Empleado 9
INSERT INTO EMPLEADOS (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_depto, id_puesto, id_estatus) VALUES ('Elena', 'Aguilar', 'Mendez', TO_DATE('1983-02-05', 'YYYY-MM-DD'), 'elena.aguilar9@empresa.mx', '5530464687', TO_DATE('2021-12-19', 'YYYY-MM-DD'), 'F', 4, 1, 'COMISION');
INSERT INTO DOMICILIOS (ID_EMPLEADO, CALLE, CODIGO_POSTAL, NUMERO_EXTERIOR, NUMERO_INTERIOR, ID_ESTADO) SELECT ID_EMPLEADO, 'Calle Ramos', '30391', '845', NULL, 'MEX' FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'elena.aguilar9@empresa.mx';
INSERT INTO EMPLEADO_CURSOS (ID_EMPLEADO, NOMBRE_CURSO, DESCRIPCION, FECHA_INICIO, FECHA_FIN) SELECT ID_EMPLEADO, 'Administración Linux', 'Comandos y redes', TO_DATE('2023-10-01', 'YYYY-MM-DD'), NULL FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'elena.aguilar9@empresa.mx';

-- Empleado 10
INSERT INTO EMPLEADOS (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_depto, id_puesto, id_estatus) VALUES ('Raul', 'Ortiz', 'Cruz', TO_DATE('1991-06-27', 'YYYY-MM-DD'), 'raul.ortiz10@empresa.mx', '5583662562', TO_DATE('2019-07-26', 'YYYY-MM-DD'), 'M', 2, 4, 'ACTIVO');
INSERT INTO DOMICILIOS (ID_EMPLEADO, CALLE, CODIGO_POSTAL, NUMERO_EXTERIOR, NUMERO_INTERIOR, ID_ESTADO) SELECT ID_EMPLEADO, 'Calle Ortiz', '92842', '186', NULL, 'MEX' FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'raul.ortiz10@empresa.mx';
INSERT INTO EMPLEADO_CURSOS (ID_EMPLEADO, NOMBRE_CURSO, DESCRIPCION, FECHA_INICIO, FECHA_FIN) SELECT ID_EMPLEADO, 'Flutter', 'Desarrollo de Apps móviles', TO_DATE('2023-01-10', 'YYYY-MM-DD'), TO_DATE('2023-03-15', 'YYYY-MM-DD') FROM EMPLEADOS WHERE CORREO_INSTITUCIONAL = 'raul.ortiz10@empresa.mx';

COMMIT;