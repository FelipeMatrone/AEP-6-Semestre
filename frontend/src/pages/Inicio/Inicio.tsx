import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  atualizarTarefa,
  listarTarefas,
  type Prioridade,
  type TarefaResumo,
} from '../../services/tarefas'
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
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [processandoId, setProcessandoId] = useState<string | null>(null)

  useEffect(() => {
    let telaAtiva = true

    async function carregarTarefas() {
      try {
        const resposta = await listarTarefas()
        if (telaAtiva) {
          setTarefas(resposta)
        }
      } catch {
        if (telaAtiva) {
          setErro('Não foi possível carregar as tarefas.')
        }
      } finally {
        if (telaAtiva) {
          setCarregando(false)
        }
      }
    }

    carregarTarefas()

    return () => {
      telaAtiva = false
    }
  }, [])

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
      })
      setTarefas((tarefasAtuais) =>
        tarefasAtuais.map((tarefaAtual) =>
          tarefaAtual.id === tarefaAtualizada.id ? tarefaAtualizada : tarefaAtual,
        ),
      )
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
