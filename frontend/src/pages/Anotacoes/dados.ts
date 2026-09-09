export type Categoria = {
  id: number
  nome: string
  anotacoes: number
  cor: 'roxo' | 'magenta' | 'verde' | 'ambar'
}

export const cores: Categoria['cor'][] = ['roxo', 'magenta', 'verde', 'ambar']

export const categoriasIniciais: Categoria[] = [
  { id: 1, nome: 'Engenharia de Software', anotacoes: 12, cor: 'roxo' },
  { id: 2, nome: 'Banco de Dados', anotacoes: 7, cor: 'magenta' },
  { id: 3, nome: 'Redes de Computadores', anotacoes: 4, cor: 'verde' },
  { id: 4, nome: 'Interface Homem-Máquina', anotacoes: 1, cor: 'ambar' },
  { id: 5, nome: 'Ideias para a AEP', anotacoes: 0, cor: 'roxo' },
]

export type Anotacao = {
  id: number
  categoriaId: number
  titulo: string
  conteudo: string
  atualizadaEm: string
}

export const anotacoesIniciais: Anotacao[] = [
  {
    id: 101,
    categoriaId: 1,
    titulo: 'Requisitos funcionais',
    conteudo:
      '<div>Anotações sobre o <b>levantamento inicial</b> do projeto.</div><ul><li>Descrevem o que o sistema deve fazer</li><li>Vêm de entrevistas com os stakeholders</li><li>Servem de base para os casos de uso</li></ul>',
    atualizadaEm: 'Editado hoje',
  },
  {
    id: 102,
    categoriaId: 1,
    titulo: 'Ciclo de vida do software',
    conteudo:
      '<div>Etapas clássicas: levantamento, análise, projeto, implementação, testes e <i>manutenção</i>.</div>',
    atualizadaEm: 'Editado ontem',
  },
  {
    id: 103,
    categoriaId: 1,
    titulo: 'Padrões de projeto',
    conteudo:
      '<div>Revisar <b>Factory</b>, <b>Observer</b> e <b>Strategy</b> para a prova do bimestre.</div>',
    atualizadaEm: 'Editado em 20 ago.',
  },
  {
    id: 201,
    categoriaId: 2,
    titulo: 'Modelo relacional',
    conteudo:
      '<div>Tabelas, chaves primárias e estrangeiras. Cada linha é uma <i>tupla</i>, cada coluna um <i>atributo</i>.</div>',
    atualizadaEm: 'Editado hoje',
  },
  {
    id: 202,
    categoriaId: 2,
    titulo: 'Normalização',
    conteudo:
      '<div><b>1FN</b>, <b>2FN</b> e <b>3FN</b>. Objetivo: eliminar redundância e anomalias.</div>',
    atualizadaEm: 'Editado há 2 dias',
  },
  {
    id: 301,
    categoriaId: 3,
    titulo: 'Camadas OSI',
    conteudo:
      '<ul><li>Física</li><li>Enlace</li><li>Rede</li><li>Transporte</li><li>Sessão</li><li>Apresentação</li><li>Aplicação</li></ul>',
    atualizadaEm: 'Editado há 3 dias',
  },
  {
    id: 401,
    categoriaId: 4,
    titulo: 'Heurísticas de Nielsen',
    conteudo:
      '<div>As <b>10 heurísticas</b> de usabilidade — revisar antes da entrega.</div>',
    atualizadaEm: 'Editado há 1 semana',
  },
]
