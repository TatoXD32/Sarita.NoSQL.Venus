
use Nosql

db.empleados.drop()
db.credenciales.drop()
db.departamentos.drop()
db.puestos.drop()
db.catalogos.drop()


// 1. CREACIÓN DE COLECCIONES 
db.createCollection('departamentos', { validator: { $jsonSchema: {
  bsonType: 'object',
  properties: {
    _id:         { bsonType: 'number' },
    descripcion: { bsonType: 'string' }
  } } } })

db.createCollection('puestos', { validator: { $jsonSchema: {
  bsonType: 'object',
  properties: {
    _id:                { bsonType: 'number' },
    descripcion_puesto: { bsonType: 'string' }
  } } } })

db.createCollection('empleados', { validator: { $jsonSchema: {
  bsonType: 'object',
  properties: {
    _id:                  { bsonType: 'number' },
    nombre:               { bsonType: 'string' },
    apellido_paterno:     { bsonType: 'string' },
    apellido_materno:     { bsonType: ['string', 'null'] },
    fecha_nacimiento:     { bsonType: 'date' },
    correo_institucional: { bsonType: 'string' },
    telefono_celular:     { bsonType: 'string' },
    fecha_ingreso:        { bsonType: 'date' },
    id_depto:             { bsonType: 'number' },     
    id_puesto:            { bsonType: 'number' },     
    sexo: {
      bsonType: 'object',
      properties: {
        id_sexo:     { bsonType: 'string' },
        descripcion: { bsonType: 'string' }
      }
    },
    estatus: {
      bsonType: 'object',
      properties: {
        id_estatus:  { bsonType: 'string' },
        descripcion: { bsonType: 'string' }
      }
    },
    domicilio: {
      bsonType: 'object',
      properties: {
        calle:           { bsonType: 'string' },
        codigo_postal:   { bsonType: 'string' },
        numero_exterior: { bsonType: 'string' },
        numero_interior: { bsonType: ['string', 'null'] },
        estado: {
          bsonType: 'object',
          properties: {
            id_estado: { bsonType: 'string' },
            nombre:    { bsonType: 'string' },
            pais: {
              bsonType: 'object',
              properties: {
                id_pais: { bsonType: 'string' },
                nombre:  { bsonType: 'string' }
              }
            }
          }
        }
      }
    },
    cursos: {
      bsonType: 'array',
      items: {
        bsonType: 'object',
        properties: {
          id_curso:     { bsonType: 'objectId' },
          nombre_curso: { bsonType: 'string' },
          descripcion:  { bsonType: ['string', 'null'] },
          fecha_inicio: { bsonType: 'date' },
          fecha_fin:    { bsonType: ['date', 'null'] },
          constancia:   { bsonType: ['string', 'null'] }
        }
      }
    }
  } } } })

db.createCollection('credenciales', { validator: { $jsonSchema: {
  bsonType: 'object',
  properties: {
    _id:             { bsonType: 'number' },     
    usuario:         { bsonType: 'string' },
    contrasena_hash: { bsonType: 'string' }
  } } } })

db.createCollection('catalogos')


// 2. ÍNDICES 
db.departamentos.createIndex({ descripcion: 1 },  { unique: true })
db.puestos.createIndex({ descripcion_puesto: 1 }, { unique: true })

db.empleados.createIndex({ correo_institucional: 1 }, { unique: true })
db.empleados.createIndex({ telefono_celular: 1 },     { unique: true })
db.empleados.createIndex({ id_depto: 1 })
db.empleados.createIndex({ id_puesto: 1 })
db.empleados.createIndex({ 'estatus.id_estatus': 1 })
db.empleados.createIndex({ 'domicilio.estado.id_estado': 1 })
db.empleados.createIndex({ 'cursos.nombre_curso': 1 })

db.credenciales.createIndex({ usuario: 1 }, { unique: true })


