// 3. DATOS
db.departamentos.insertMany([
  { _id: 1, descripcion: 'Recursos Humanos' },
  { _id: 2, descripcion: 'Tecnologías de la Información' },
  { _id: 3, descripcion: 'Ventas' },
  { _id: 4, descripcion: 'Marketing' },
  { _id: 5, descripcion: 'Finanzas' }
])

db.puestos.insertMany([
  { _id: 1, descripcion_puesto: 'Desarrollador Backend' },
  { _id: 2, descripcion_puesto: 'Desarrollador Frontend' },
  { _id: 3, descripcion_puesto: 'Administrador de BD' },
  { _id: 4, descripcion_puesto: 'Gerente de Proyecto' },
  { _id: 5, descripcion_puesto: 'Analista QA' }
])

db.catalogos.insertMany([
  { _id: 'estatus_empleados', valores: [
      { id_estatus: 'ACTIVO',   descripcion: 'Operando normalmente' },
      { id_estatus: 'INACTIVO', descripcion: 'Fuera de operación' },
      { id_estatus: 'ENFERMO',  descripcion: 'Incapacidad médica' },
      { id_estatus: 'COMISION', descripcion: 'De comisión laboral' },
      { id_estatus: 'BAJA',     descripcion: 'Baja definitiva' }
  ] },
  { _id: 'sexos', valores: [
      { id_sexo: 'M', descripcion: 'Masculino' },
      { id_sexo: 'F', descripcion: 'Femenino' },
      { id_sexo: 'X', descripcion: 'No Binario' }
  ] },
  { _id: 'ubicaciones', paises: [
      { id_pais: 'MX', nombre: 'México', estados: [
          { id_estado: 'MEX',  nombre: 'Estado de México' },
          { id_estado: 'JAL',  nombre: 'Jalisco' },
          { id_estado: 'CDMX', nombre: 'Ciudad de México' },
          { id_estado: 'NLE',  nombre: 'Nuevo León' }
      ] },
      { id_pais: 'US', nombre: 'Estados Unidos', estados: [
          { id_estado: 'TX', nombre: 'Texas' }
      ] },
      { id_pais: 'CA', nombre: 'Canadá', estados: [] }
  ] }
])

db.empleados.insertMany([
  { _id: 1, nombre: 'Ana', apellido_paterno: 'Garcia', apellido_materno: 'Aguilar',
    fecha_nacimiento: ISODate('1999-05-21T00:00:00Z'), correo_institucional: 'ana.garcia1@empresa.mx',
    telefono_celular: '5522045550', fecha_ingreso: ISODate('2018-08-06T00:00:00Z'),
    sexo: { id_sexo: 'F', descripcion: 'Femenino' }, id_depto: 2, id_puesto: 4,
    estatus: { id_estatus: 'ENFERMO', descripcion: 'Incapacidad médica' },
    domicilio: { calle: 'Calle Torres', codigo_postal: '86654', numero_exterior: '172', numero_interior: null,
      estado: { id_estado: 'CDMX', nombre: 'Ciudad de México', pais: { id_pais: 'MX', nombre: 'México' } } },
    cursos: [ { id_curso: new ObjectId(), nombre_curso: 'Java Básico', descripcion: 'Fundamentos de Java y POO',
                fecha_inicio: ISODate('2023-10-01T00:00:00Z'), fecha_fin: null, constancia: null } ] },

  { _id: 2, nombre: 'Luis', apellido_paterno: 'Reyes', apellido_materno: 'Cruz',
    fecha_nacimiento: ISODate('1989-12-24T00:00:00Z'), correo_institucional: 'luis.reyes2@empresa.mx',
    telefono_celular: '5545224240', fecha_ingreso: ISODate('2023-03-02T00:00:00Z'),
    sexo: { id_sexo: 'M', descripcion: 'Masculino' }, id_depto: 2, id_puesto: 1,
    estatus: { id_estatus: 'ACTIVO', descripcion: 'Operando normalmente' },
    domicilio: { calle: 'Calle Torres', codigo_postal: '90336', numero_exterior: '829', numero_interior: null,
      estado: { id_estado: 'MEX', nombre: 'Estado de México', pais: { id_pais: 'MX', nombre: 'México' } } },
    cursos: [ { id_curso: new ObjectId(), nombre_curso: 'Seguridad Web', descripcion: 'OWASP y vulnerabilidades',
                fecha_inicio: ISODate('2023-10-01T00:00:00Z'), fecha_fin: null, constancia: null } ] },

  { _id: 3, nombre: 'Marta', apellido_paterno: 'Ramos', apellido_materno: 'Silva',
    fecha_nacimiento: ISODate('2000-05-01T00:00:00Z'), correo_institucional: 'marta.ramos3@empresa.mx',
    telefono_celular: '5521940733', fecha_ingreso: ISODate('2019-11-24T00:00:00Z'),
    sexo: { id_sexo: 'F', descripcion: 'Femenino' }, id_depto: 5, id_puesto: 3,
    estatus: { id_estatus: 'ACTIVO', descripcion: 'Operando normalmente' },
    domicilio: { calle: 'Calle Ortiz', codigo_postal: '65103', numero_exterior: '452', numero_interior: null,
      estado: { id_estado: 'NLE', nombre: 'Nuevo León', pais: { id_pais: 'MX', nombre: 'México' } } },
    cursos: [ { id_curso: new ObjectId(), nombre_curso: 'Seguridad Web', descripcion: 'OWASP y vulnerabilidades',
                fecha_inicio: ISODate('2023-10-01T00:00:00Z'), fecha_fin: null, constancia: null } ] },

  { _id: 4, nombre: 'Pedro', apellido_paterno: 'Vargas', apellido_materno: 'Lopez',
    fecha_nacimiento: ISODate('1997-07-14T00:00:00Z'), correo_institucional: 'pedro.vargas4@empresa.mx',
    telefono_celular: '5563279395', fecha_ingreso: ISODate('2019-08-08T00:00:00Z'),
    sexo: { id_sexo: 'M', descripcion: 'Masculino' }, id_depto: 5, id_puesto: 1,
    estatus: { id_estatus: 'COMISION', descripcion: 'De comisión laboral' },
    domicilio: { calle: 'Calle Perez', codigo_postal: '32232', numero_exterior: '96', numero_interior: null,
      estado: { id_estado: 'CDMX', nombre: 'Ciudad de México', pais: { id_pais: 'MX', nombre: 'México' } } },
    cursos: [ { id_curso: new ObjectId(), nombre_curso: 'Java Básico', descripcion: 'Fundamentos de Java y POO',
                fecha_inicio: ISODate('2023-01-10T00:00:00Z'), fecha_fin: ISODate('2023-03-15T00:00:00Z'), constancia: null } ] },

  { _id: 5, nombre: 'Sofia', apellido_paterno: 'Castillo', apellido_materno: 'Hernandez',
    fecha_nacimiento: ISODate('2000-12-02T00:00:00Z'), correo_institucional: 'sofia.castillo5@empresa.mx',
    telefono_celular: '5544012051', fecha_ingreso: ISODate('2023-10-16T00:00:00Z'),
    sexo: { id_sexo: 'F', descripcion: 'Femenino' }, id_depto: 4, id_puesto: 1,
    estatus: { id_estatus: 'ACTIVO', descripcion: 'Operando normalmente' },
    domicilio: { calle: 'Calle Garcia', codigo_postal: '62382', numero_exterior: '490', numero_interior: null,
      estado: { id_estado: 'CDMX', nombre: 'Ciudad de México', pais: { id_pais: 'MX', nombre: 'México' } } },
    cursos: [ { id_curso: new ObjectId(), nombre_curso: 'Administración Linux', descripcion: 'Comandos y redes',
                fecha_inicio: ISODate('2023-10-01T00:00:00Z'), fecha_fin: null, constancia: null } ] },

  { _id: 6, nombre: 'Carlos', apellido_paterno: 'Gutierrez', apellido_materno: 'Medina',
    fecha_nacimiento: ISODate('1984-06-22T00:00:00Z'), correo_institucional: 'carlos.gutierrez6@empresa.mx',
    telefono_celular: '5576939048', fecha_ingreso: ISODate('2018-03-11T00:00:00Z'),
    sexo: { id_sexo: 'M', descripcion: 'Masculino' }, id_depto: 3, id_puesto: 3,
    estatus: { id_estatus: 'BAJA', descripcion: 'Baja definitiva' },
    domicilio: { calle: 'Calle Gonzalez', codigo_postal: '91502', numero_exterior: '720', numero_interior: null,
      estado: { id_estado: 'MEX', nombre: 'Estado de México', pais: { id_pais: 'MX', nombre: 'México' } } },
    cursos: [ { id_curso: new ObjectId(), nombre_curso: 'Flutter', descripcion: 'Desarrollo de Apps móviles',
                fecha_inicio: ISODate('2023-01-10T00:00:00Z'), fecha_fin: ISODate('2023-03-15T00:00:00Z'), constancia: null } ] },

  { _id: 7, nombre: 'Lucia', apellido_paterno: 'Chavez', apellido_materno: 'Morales',
    fecha_nacimiento: ISODate('1986-10-23T00:00:00Z'), correo_institucional: 'lucia.chavez7@empresa.mx',
    telefono_celular: '5523361046', fecha_ingreso: ISODate('2018-12-18T00:00:00Z'),
    sexo: { id_sexo: 'F', descripcion: 'Femenino' }, id_depto: 2, id_puesto: 3,
    estatus: { id_estatus: 'ACTIVO', descripcion: 'Operando normalmente' },
    domicilio: { calle: 'Calle Ramos', codigo_postal: '91916', numero_exterior: '787', numero_interior: null,
      estado: { id_estado: 'CDMX', nombre: 'Ciudad de México', pais: { id_pais: 'MX', nombre: 'México' } } },
    cursos: [ { id_curso: new ObjectId(), nombre_curso: 'Seguridad Web', descripcion: 'OWASP y vulnerabilidades',
                fecha_inicio: ISODate('2023-10-01T00:00:00Z'), fecha_fin: null, constancia: null } ] },

  { _id: 8, nombre: 'Jorge', apellido_paterno: 'Garcia', apellido_materno: 'Martinez',
    fecha_nacimiento: ISODate('1980-01-15T00:00:00Z'), correo_institucional: 'jorge.garcia8@empresa.mx',
    telefono_celular: '5574306257', fecha_ingreso: ISODate('2018-02-02T00:00:00Z'),
    sexo: { id_sexo: 'M', descripcion: 'Masculino' }, id_depto: 2, id_puesto: 5,
    estatus: { id_estatus: 'ACTIVO', descripcion: 'Operando normalmente' },
    domicilio: { calle: 'Calle Perez', codigo_postal: '46710', numero_exterior: '725', numero_interior: null,
      estado: { id_estado: 'MEX', nombre: 'Estado de México', pais: { id_pais: 'MX', nombre: 'México' } } },
    cursos: [ { id_curso: new ObjectId(), nombre_curso: 'Flutter', descripcion: 'Desarrollo de Apps móviles',
                fecha_inicio: ISODate('2023-10-01T00:00:00Z'), fecha_fin: null, constancia: null } ] },

  { _id: 9, nombre: 'Elena', apellido_paterno: 'Aguilar', apellido_materno: 'Mendez',
    fecha_nacimiento: ISODate('1983-02-05T00:00:00Z'), correo_institucional: 'elena.aguilar9@empresa.mx',
    telefono_celular: '5530464687', fecha_ingreso: ISODate('2021-12-19T00:00:00Z'),
    sexo: { id_sexo: 'F', descripcion: 'Femenino' }, id_depto: 4, id_puesto: 1,
    estatus: { id_estatus: 'COMISION', descripcion: 'De comisión laboral' },
    domicilio: { calle: 'Calle Ramos', codigo_postal: '30391', numero_exterior: '845', numero_interior: null,
      estado: { id_estado: 'MEX', nombre: 'Estado de México', pais: { id_pais: 'MX', nombre: 'México' } } },
    cursos: [ { id_curso: new ObjectId(), nombre_curso: 'Administración Linux', descripcion: 'Comandos y redes',
                fecha_inicio: ISODate('2023-10-01T00:00:00Z'), fecha_fin: null, constancia: null } ] },

  { _id: 10, nombre: 'Raul', apellido_paterno: 'Ortiz', apellido_materno: 'Cruz',
    fecha_nacimiento: ISODate('1991-06-27T00:00:00Z'), correo_institucional: 'raul.ortiz10@empresa.mx',
    telefono_celular: '5583662562', fecha_ingreso: ISODate('2019-07-26T00:00:00Z'),
    sexo: { id_sexo: 'M', descripcion: 'Masculino' }, id_depto: 2, id_puesto: 4,
    estatus: { id_estatus: 'ACTIVO', descripcion: 'Operando normalmente' },
    domicilio: { calle: 'Calle Ortiz', codigo_postal: '92842', numero_exterior: '186', numero_interior: null,
      estado: { id_estado: 'MEX', nombre: 'Estado de México', pais: { id_pais: 'MX', nombre: 'México' } } },
    cursos: [ { id_curso: new ObjectId(), nombre_curso: 'Flutter', descripcion: 'Desarrollo de Apps móviles',
                fecha_inicio: ISODate('2023-01-10T00:00:00Z'), fecha_fin: ISODate('2023-03-15T00:00:00Z'), constancia: null } ] }
])