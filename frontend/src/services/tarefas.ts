export type Prioridade = 'alta' | 'media' | 'baixa'

export type TarefaResumo = {
  id: string
  titulo: string
  prazo: string
  prioridade: Prioridade
  observacao: string
  concluida: boolean
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
}

export type TarefaUpdateRequest = {
  titulo: string
  prazo: string
  prioridade: Prioridade
  observacao: string
  concluida: boolean
}

type ApiErrorResponse = {
  message?: string
}

const BASE_PATH = '/api/tarefas'

async function requisitar<T>(url: string, opcoes?: RequestInit): Promise<T> {
  const resposta = await fetch(url, {
    ...opcoes,
    headers: {
      'Content-Type': 'application/json',
      ...opcoes?.headers,
    },
  })

  if (!resposta.ok) {
    const erro = (await resposta.json().catch(() => null)) as ApiErrorResponse | null
    throw new Error(erro?.message ?? 'Não foi possível concluir a operação.')
  }

  if (resposta.status === 204) {
    return undefined as T
  }

  return resposta.json() as Promise<T>
}

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
