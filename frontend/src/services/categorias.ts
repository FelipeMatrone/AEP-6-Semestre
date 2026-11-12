import { requisitar } from './http'

export type Cor = 'roxo' | 'magenta' | 'verde' | 'ambar'

export type Anotacao = {
  id: string
  titulo: string
  conteudo: string
  criadaEm: string
  atualizadaEm: string
}

export type CategoriaResumo = {
  id: string
  nome: string
  descricao: string | null
  cor: Cor
  totalAnotacoes: number
}

export type Categoria = {
  id: string
  nome: string
  descricao: string | null
  cor: Cor
  anotacoes: Anotacao[]
}

export type CategoriaRequest = {
  nome: string
  descricao?: string
  cor: Cor
}

export type AnotacaoRequest = {
  titulo: string
  conteudo: string
}

const BASE_PATH = '/api/categorias'

export function listarCategorias(): Promise<CategoriaResumo[]> {
  return requisitar<CategoriaResumo[]>(BASE_PATH)
}

export function buscarCategoria(id: string): Promise<Categoria> {
  return requisitar<Categoria>(`${BASE_PATH}/${id}`)
}

export function criarCategoria(request: CategoriaRequest): Promise<Categoria> {
  return requisitar<Categoria>(BASE_PATH, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function atualizarCategoria(id: string, request: CategoriaRequest): Promise<Categoria> {
  return requisitar<Categoria>(`${BASE_PATH}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function excluirCategoria(id: string): Promise<void> {
  return requisitar<void>(`${BASE_PATH}/${id}`, { method: 'DELETE' })
}

export function criarAnotacao(
  categoriaId: string,
  request: AnotacaoRequest,
): Promise<Anotacao> {
  return requisitar<Anotacao>(`${BASE_PATH}/${categoriaId}/anotacoes`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function atualizarAnotacao(
  categoriaId: string,
  anotacaoId: string,
  request: AnotacaoRequest,
): Promise<Anotacao> {
  return requisitar<Anotacao>(`${BASE_PATH}/${categoriaId}/anotacoes/${anotacaoId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function excluirAnotacao(categoriaId: string, anotacaoId: string): Promise<void> {
  return requisitar<void>(`${BASE_PATH}/${categoriaId}/anotacoes/${anotacaoId}`, {
    method: 'DELETE',
  })
}
