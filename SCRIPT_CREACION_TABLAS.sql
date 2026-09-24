
-- =====================================================
-- 2. SCRIPT DE CREACIÓN DE TABLAS
-- =====================================================

CREATE TABLE cat_estatus (
    id_estatus VARCHAR2(20) PRIMARY KEY,
    descripcion VARCHAR2(100) NOT NULL,
    categoria VARCHAR2(50) NOT NULL
);

CREATE TABLE sexo (
    id_sexo CHAR(3) PRIMARY KEY,
    descripcion VARCHAR2(20) NOT NULL UNIQUE,
    id_estatus VARCHAR2(20) DEFAULT 'ACTIVO' NOT NULL,
    CONSTRAINT fk_sexo_estatus FOREIGN KEY (id_estatus) REFERENCES cat_estatus(id_estatus)
);

CREATE TABLE pais (
    id_pais VARCHAR2(3) PRIMARY KEY, 
    nombre VARCHAR2(50) NOT NULL UNIQUE,
    id_estatus VARCHAR2(20) DEFAULT 'ACTIVO' NOT NULL,
    CONSTRAINT fk_pais_estatus FOREIGN KEY (id_estatus) REFERENCES cat_estatus(id_estatus)
);

CREATE TABLE estado (
    id_estado VARCHAR2(5) PRIMARY KEY,
    nombre VARCHAR2(50) NOT NULL,
    id_pais VARCHAR2(3) NOT NULL,
    id_estatus VARCHAR2(20) DEFAULT 'ACTIVO' NOT NULL,
    CONSTRAINT fk_estado_pais FOREIGN KEY (id_pais) REFERENCES pais(id_pais),
    CONSTRAINT fk_estado_estatus FOREIGN KEY (id_estatus) REFERENCES cat_estatus(id_estatus)
);

CREATE TABLE domicilio (
    id_domicilio NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    calle VARCHAR2(100) NOT NULL,
    codigo_postal VARCHAR2(5) NOT NULL,
    numero_exterior VARCHAR2(10) NOT NULL,
    numero_interior VARCHAR2(10),
    id_estado VARCHAR2(5) NOT NULL,
    CONSTRAINT fk_domicilio_estado FOREIGN KEY (id_estado) REFERENCES estado(id_estado)
);

CREATE TABLE curso (
    id_curso NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR2(100) NOT NULL UNIQUE,
    descripcion VARCHAR2(255),
    id_estatus VARCHAR2(20) DEFAULT 'ACTIVO' NOT NULL,
    CONSTRAINT fk_curso_estatus FOREIGN KEY (id_estatus) REFERENCES cat_estatus(id_estatus)
);

CREATE TABLE departamento (
    id_depto NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    descripcion VARCHAR2(100) NOT NULL UNIQUE,
    id_estatus VARCHAR2(20) DEFAULT 'ACTIVO' NOT NULL,
    CONSTRAINT fk_depto_estatus FOREIGN KEY (id_estatus) REFERENCES cat_estatus(id_estatus)
);

CREATE TABLE puesto (
    id_puesto NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    descripcion_puesto VARCHAR2(100) NOT NULL UNIQUE,
    id_estatus VARCHAR2(20) DEFAULT 'ACTIVO' NOT NULL,
    CONSTRAINT fk_puesto_estatus FOREIGN KEY (id_estatus) REFERENCES cat_estatus(id_estatus)
);

CREATE TABLE empleado (
    id_empleado NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR2(50) NOT NULL,
    apellido_paterno VARCHAR2(50) NOT NULL,
    apellido_materno VARCHAR2(50),
    fecha_nacimiento DATE NOT NULL,
    correo_institucional VARCHAR2(100) NOT NULL UNIQUE,
    telefono_celular VARCHAR2(15),
    fecha_ingreso DATE DEFAULT SYSDATE NOT NULL,
    id_sexo CHAR(3) NOT NULL, 
    id_domicilio NUMBER NOT NULL UNIQUE,
    id_depto NUMBER NOT NULL,   
    id_puesto NUMBER NOT NULL,  
    id_estatus VARCHAR2(20) DEFAULT 'ACTIVO' NOT NULL,
    CONSTRAINT fk_empleado_sexo FOREIGN KEY (id_sexo) REFERENCES sexo(id_sexo),
    CONSTRAINT fk_empleado_domicilio FOREIGN KEY (id_domicilio) REFERENCES domicilio(id_domicilio),
    CONSTRAINT fk_empleado_depto FOREIGN KEY (id_depto) REFERENCES departamento(id_depto),
    CONSTRAINT fk_empleado_puesto FOREIGN KEY (id_puesto) REFERENCES puesto(id_puesto),
    CONSTRAINT fk_empleado_estatus FOREIGN KEY (id_estatus) REFERENCES cat_estatus(id_estatus)
);

CREATE TABLE credencial (
    id_empleado NUMBER PRIMARY KEY,
    usuario VARCHAR2(30) NOT NULL UNIQUE,
    contrasena_hash VARCHAR2(255) NOT NULL,
    CONSTRAINT fk_credencial_empleado FOREIGN KEY (id_empleado) REFERENCES empleado(id_empleado)
);

CREATE TABLE empleado_curso (
    id_empleado_curso NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_empleado NUMBER NOT NULL,
    id_curso NUMBER NOT NULL,
    fecha_inicio DATE DEFAULT SYSDATE NOT NULL,
    fecha_fin DATE,
    CONSTRAINT fk_empcurso_empleado FOREIGN KEY (id_empleado) REFERENCES empleado(id_empleado),
    CONSTRAINT fk_empcurso_curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso),
    CONSTRAINT chk_fechas_curso CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);
COMMIT;

