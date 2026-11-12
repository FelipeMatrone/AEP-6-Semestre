import { requisitar } from './http'
import type { Cor } from './categorias'

export type EventoCalendario = {
  id: string
  titulo: string
  descricao: string | null
  data: string
  horario: string | null
  cor: Cor
  tarefaId: string | null
}

export type EventoCalendarioRequest = Omit<EventoCalendario, 'id'>

const BASE_PATH = '/api/eventos-calendario'

export function listarEventosCalendario(): Promise<EventoCalendario[]> {
  return requisitar<EventoCalendario[]>(BASE_PATH)
}

export function criarEventoCalendario(
  request: Omit<EventoCalendarioRequest, 'tarefaId'>,
): Promise<EventoCalendario> {
  return requisitar<EventoCalendario>(BASE_PATH, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function atualizarEventoCalendario(
  id: string,
  request: EventoCalendarioRequest,
): Promise<EventoCalendario> {
  return requisitar<EventoCalendario>(`${BASE_PATH}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function excluirEventoCalendario(id: string): Promise<void> {
  return requisitar<void>(`${BASE_PATH}/${id}`, { method: 'DELETE' })
}
