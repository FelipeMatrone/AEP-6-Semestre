import type { Cor } from './categorias'
import { requisitar } from './http'

export type Progresso = {
  categoriaId: string | null
  nome: string
  cor: Cor
  total: number
  concluidas: number
  percentual: number
}

export type Painel = {
  pendentes: number
  concluidas: number
  atrasadas: number
  metaSemanal: number
  concluidasNaSemana: number
  percentualMetaSemanal: number
  progresso: Progresso[]
}

export function buscarPainel(): Promise<Painel> {
  return requisitar<Painel>('/api/painel')
}
