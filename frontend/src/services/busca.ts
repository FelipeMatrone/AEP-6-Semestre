import { requisitar } from './http'

export type ResultadoTarefa = {
  id: string
  titulo: string
  categoriaId: string | null
}

export type ResultadoCategoria = {
  id: string
  nome: string
}

export type ResultadoAnotacao = {
  categoriaId: string
  categoriaNome: string
  id: string
  titulo: string
}

export type ResultadoBusca = {
  tarefas: ResultadoTarefa[]
  categorias: ResultadoCategoria[]
  anotacoes: ResultadoAnotacao[]
}

export function buscar(termo: string): Promise<ResultadoBusca> {
  return requisitar<ResultadoBusca>(`/api/busca?termo=${encodeURIComponent(termo)}`)
}
