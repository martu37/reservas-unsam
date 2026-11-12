export type Claustro = 'estudiante' | 'docente' | 'no docente'
export type Rol = 'usuario' | 'admin'

export type UsuarioJSON = {
  id: number
  mail: string
  apellido: string
  nombre: string
  claustro: Claustro
  rol: Rol
}

// Lo que se manda para darse de alta (el rol siempre es usuario)
export type NuevoUsuarioData = {
  mail: string
  nombre: string
  apellido: string
  claustro: Claustro
}

export class Usuario {
  id = 0
  mail = ''
  apellido = ''
  nombre = ''
  claustro: Claustro = 'estudiante'
  rol: Rol = 'usuario'

  static fromJson(json: UsuarioJSON): Usuario {
    return Object.assign(new Usuario(), json)
  }

  get nombreCompleto() {
    return `${this.nombre} ${this.apellido}`
  }

  esAdmin() {
    return this.rol === 'admin'
  }
}
