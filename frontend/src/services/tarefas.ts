import { requisitar } from './http'

export type Prioridade = 'alta' | 'media' | 'baixa'

export type TarefaResumo = {
  id: string
  titulo: string
  prazo: string
  prioridade: Prioridade
  observacao: string
  concluida: boolean
  categoriaId: string | null
}

export type Tarefa = TarefaResumo & {
  criadaEm: string
  atualizadaEm: string
}

export type TarefaCreateRequest = {
  titulo: string
  prazo: string
  prioridade: Prioridade
  observacao: string
  categoriaId: string | null
}

export type TarefaUpdateRequest = {
  titulo: string
  prazo: string
  prioridade: Prioridade
  observacao: string
  concluida: boolean
  categoriaId: string | null
}

const BASE_PATH = '/api/tarefas'

export function listarTarefas(): Promise<TarefaResumo[]> {
  return requisitar<TarefaResumo[]>(BASE_PATH)
}

export function criarTarefa(request: TarefaCreateRequest): Promise<Tarefa> {
  return requisitar<Tarefa>(BASE_PATH, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function atualizarTarefa(id: string, request: TarefaUpdateRequest): Promise<Tarefa> {
  return requisitar<Tarefa>(`${BASE_PATH}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function excluirTarefa(id: string): Promise<void> {
  return requisitar<void>(`${BASE_PATH}/${id}`, {
    method: 'DELETE',
  })
}
