import { requisitar } from './http'
import type { Usuario } from './auth'

export type UsuarioCreateRequest = {
  nome: string
  email: string
  senha: string
}

export function cadastrarUsuario(dados: UsuarioCreateRequest): Promise<Usuario> {
  return requisitar<Usuario>('/api/usuarios', {
    method: 'POST',
    body: JSON.stringify(dados),
  })
}
