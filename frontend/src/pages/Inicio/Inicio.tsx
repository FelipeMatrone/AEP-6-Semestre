import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  atualizarTarefa,
  listarTarefas,
  type Prioridade,
  type TarefaResumo,
} from '../../services/tarefas'
import { buscarPainel, type Painel } from '../../services/painel'
import styles from './Inicio.module.css'

const anotacoes = [
  {
    id: 1,
    titulo: 'Requisitos funcionais',
    data: 'Criada hoje',
    descricao: 'Anotações sobre o levantamento inicial do projeto.',
  },
  {
    id: 2,
    titulo: 'Banco de Dados NoSQL',
    data: 'Criada ontem',
    descricao: 'Conceitos principais para revisar antes da atividade.',
  },
  {
    id: 3,
    titulo: 'Ideias para a AEP',
    data: 'Criada em 20 ago.',
    descricao: 'Possíveis temas e funcionalidades para a PoC.',
  },
]

const rotulosPrioridade: Record<Prioridade, string> = {
  alta: 'Alta',
  media: 'Média',
  baixa: 'Baixa',
}

const classesPrioridade: Record<Prioridade, string> = {
  alta: styles.prioridadeAlta,
  media: styles.prioridadeMedia,
  baixa: styles.prioridadeBaixa,
}

function obterDataLocalAtual() {
  const agora = new Date()
  const mes = String(agora.getMonth() + 1).padStart(2, '0')
  const dia = String(agora.getDate()).padStart(2, '0')

  return `${agora.getFullYear()}-${mes}-${dia}`
}

function formatarPrazo(prazo: string, hoje: string) {
  if (prazo === hoje) return 'Hoje'

  return new Intl.DateTimeFormat('pt-BR', { timeZone: 'UTC' }).format(
    new Date(`${prazo}T00:00:00Z`),
  )
}

export default function Inicio() {
  const [tarefas, setTarefas] = useState<TarefaResumo[]>([])
  const [painel, setPainel] = useState<Painel | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [carregandoPainel, setCarregandoPainel] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [erroPainel, setErroPainel] = useState<string | null>(null)
  const [processandoId, setProcessandoId] = useState<string | null>(null)

  useEffect(() => {
    let telaAtiva = true

    async function carregarDashboard() {
      const [resultadoTarefas, resultadoPainel] = await Promise.allSettled([
        listarTarefas(),
        buscarPainel(),
      ])

      if (!telaAtiva) return

      if (resultadoTarefas.status === 'fulfilled') {
        setTarefas(resultadoTarefas.value)
      } else {
        setErro('Não foi possível carregar as tarefas.')
      }

      if (resultadoPainel.status === 'fulfilled') {
        setPainel(resultadoPainel.value)
      } else {
        setErroPainel('Não foi possível carregar as metas e o progresso.')
      }

      setCarregando(false)
      setCarregandoPainel(false)
    }

    carregarDashboard()

    return () => {
      telaAtiva = false
    }
  }, [])

  async function atualizarDadosDoPainel() {
    try {
      setErroPainel(null)
      setPainel(await buscarPainel())
    } catch {
      setErroPainel('Não foi possível atualizar as metas e o progresso.')
    }
  }

  const hoje = obterDataLocalAtual()
  const tarefasDeHoje = tarefas.filter((tarefa) => tarefa.prazo === hoje)
  const concluidasHoje = tarefasDeHoje.filter((tarefa) => tarefa.concluida).length
  const pendentesHoje = tarefasDeHoje.length - concluidasHoje
  const proximasTarefas = tarefas
    .filter((tarefa) => !tarefa.concluida && tarefa.prazo > hoje)
    .sort((primeira, segunda) => primeira.prazo.localeCompare(segunda.prazo))
    .slice(0, 4)

  async function alternarTarefa(tarefa: TarefaResumo) {
    setProcessandoId(tarefa.id)
    setErro(null)

    try {
      const tarefaAtualizada = await atualizarTarefa(tarefa.id, {
        titulo: tarefa.titulo,
        prazo: tarefa.prazo,
        prioridade: tarefa.prioridade,
        observacao: tarefa.observacao,
        concluida: !tarefa.concluida,
        categoriaId: tarefa.categoriaId,
      })
      setTarefas((tarefasAtuais) =>
        tarefasAtuais.map((tarefaAtual) =>
          tarefaAtual.id === tarefaAtualizada.id ? tarefaAtualizada : tarefaAtual,
        ),
      )
      await atualizarDadosDoPainel()
    } catch (erroAoAtualizar) {
      setErro(
        erroAoAtualizar instanceof Error
          ? erroAoAtualizar.message
          : 'Não foi possível atualizar a tarefa.',
      )
    } finally {
      setProcessandoId(null)
    }
  }

  return (
    <>
      <section className={styles.boasVindas}>
        <p className={styles.saudacao}>Olá, estudante!</p>
        <h1>Seu painel acadêmico</h1>
        <p>Acompanhe suas tarefas e anotações em um só lugar.</p>
      </section>

      <section className={styles.painelPrincipal} aria-label="Resumo do painel">
        <div className={styles.colunaEsquerda}>
          <article className={styles.cartao}>
            <div className={styles.tituloCartao}>
              <h2>Próximas tarefas</h2>
              <Link to="/tarefas">Ver todas</Link>
            </div>

            <ul className={styles.lista}>
              {!carregando && !erro && proximasTarefas.length === 0 && (
                <li className={styles.mensagemVazia}>Nenhuma tarefa futura.</li>
              )}
              {proximasTarefas.map((tarefa) => (
                <li key={tarefa.id} className={styles.itemLista}>
                  <span className={`${styles.pontoPrioridade} ${classesPrioridade[tarefa.prioridade]}`} />
                  <div>
                    <strong>{tarefa.titulo}</strong>
                    <span>Prazo: {formatarPrazo(tarefa.prazo, hoje)}</span>
                  </div>
                </li>
              ))}
            </ul>
          </article>

          <article className={styles.cartao}>
            <div className={styles.tituloCartao}>
              <h2>Últimas anotações criadas</h2>
              <button type="button">Ver todas</button>
            </div>

            <ul className={styles.lista}>
              {anotacoes.map((anotacao) => (
                <li key={anotacao.id} className={styles.itemAnotacao}>
                  <div className={styles.iconeAnotacao}>✎</div>
                  <div>
                    <strong>{anotacao.titulo}</strong>
                    <p>{anotacao.descricao}</p>
                    <span>{anotacao.data}</span>
                  </div>
                </li>
              ))}
            </ul>
          </article>
        </div>

        <div className={styles.colunaDireita}>
          <article className={`${styles.cartao} ${styles.metasProgresso}`}>
            <div className={styles.tituloCartao}>
              <h2>Metas e progresso</h2>
              {!carregandoPainel && painel && (
                <span>{painel.percentualMetaSemanal}% da meta</span>
              )}
            </div>

            {carregandoPainel && <p className={styles.mensagemVazia}>Carregando metas...</p>}
            {erroPainel && <p className={styles.mensagemVazia} role="alert">{erroPainel}</p>}
            {!carregandoPainel && !erroPainel && painel && (
              <>
                <section className={styles.metaSemanal} aria-label="Meta semanal">
                  <div>
                    <span>Meta semanal</span>
                    <strong>{painel.concluidasNaSemana} de {painel.metaSemanal} tarefas concluídas</strong>
                  </div>
                  <progress value={painel.percentualMetaSemanal} max={100}>
                    {painel.percentualMetaSemanal}%
                  </progress>
                </section>

                <ul className={styles.indicadores} aria-label="Indicadores das tarefas">
                  <li><span>Pendentes</span><strong>{painel.pendentes}</strong></li>
                  <li><span>Concluídas</span><strong>{painel.concluidas}</strong></li>
                  <li><span>Atrasadas</span><strong>{painel.atrasadas}</strong></li>
                </ul>

                <section className={styles.progressoCategorias} aria-labelledby="titulo-progresso-categorias">
                  <h3 id="titulo-progresso-categorias">Por disciplina</h3>
                  {painel.progresso.length === 0 ? (
                    <p>Nenhuma tarefa categorizada ainda.</p>
                  ) : (
                    <ul>
                      {painel.progresso.map((progresso) => (
                        <li key={progresso.categoriaId ?? 'sem-categoria'}>
                          <div>
                            <span className={`${styles.marcadorCategoria} ${styles[progresso.cor]}`} />
                            <strong>{progresso.nome}</strong>
                            <span>{progresso.concluidas}/{progresso.total}</span>
                          </div>
                          <progress value={progresso.percentual} max={100}>
                            {progresso.percentual}%
                          </progress>
                        </li>
                      ))}
                    </ul>
                  )}
                </section>
              </>
            )}
          </article>

          <article className={`${styles.cartao} ${styles.resumo}`}>
            <h2>Resumo de hoje</h2>

            <div className={styles.metricas}>
              <div>
                <span>Concluídas</span>
                <strong>{concluidasHoje}</strong>
              </div>

              <div>
                <span>Pendentes</span>
                <strong>{pendentesHoje}</strong>
              </div>
            </div>
          </article>

          <article className={`${styles.cartao} ${styles.tarefasHoje}`}>
            <div className={styles.tituloCartao}>
              <h2>Tarefas de hoje</h2>
              <span>{tarefasDeHoje.length} tarefas</span>
            </div>

            {erro && <p className={styles.mensagemVazia} role="alert">{erro}</p>}
            {carregando && <p className={styles.mensagemVazia}>Carregando tarefas...</p>}
            {!carregando && !erro && tarefasDeHoje.length === 0 && (
              <p className={styles.mensagemVazia}>Nenhuma tarefa para hoje.</p>
            )}
            <ul className={styles.listaHoje}>
              {tarefasDeHoje.map((tarefa) => (
                <li key={tarefa.id}>
                  <label>
                    <input
                      type="checkbox"
                      checked={tarefa.concluida}
                      onChange={() => alternarTarefa(tarefa)}
                      disabled={processandoId === tarefa.id}
                    />
                    <span className={tarefa.concluida ? styles.concluida : ''}>
                      {tarefa.titulo}
                    </span>
                  </label>
                  <span className={`${styles.etiqueta} ${classesPrioridade[tarefa.prioridade]}`}>
                    {rotulosPrioridade[tarefa.prioridade]}
                  </span>
                </li>
              ))}
            </ul>
          </article>
        </div>
      </section>
    </>
  )
}
