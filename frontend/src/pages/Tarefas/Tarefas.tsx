import { useEffect, useState, type FormEvent } from 'react'
import Modal from '../../components/Modal'
import {
  atualizarTarefa,
  criarTarefa,
  excluirTarefa,
  listarTarefas,
  type Prioridade,
  type TarefaResumo,
} from '../../services/tarefas'
import styles from './Tarefas.module.css'

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

function formatarPrazo(prazo: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    timeZone: 'UTC',
  }).format(new Date(`${prazo}T00:00:00Z`))
}

function obterDataLocalAtual() {
  const agora = new Date()
  const mes = String(agora.getMonth() + 1).padStart(2, '0')
  const dia = String(agora.getDate()).padStart(2, '0')

  return `${agora.getFullYear()}-${mes}-${dia}`
}

function mensagemDoErro(erro: unknown) {
  return erro instanceof Error ? erro.message : 'Não foi possível concluir a operação.'
}

export default function Tarefas() {
  const [tarefas, setTarefas] = useState<TarefaResumo[]>([])
  const [tarefaEmEdicao, setTarefaEmEdicao] = useState<TarefaResumo | null>(null)
  const [modalAberto, setModalAberto] = useState(false)
  const [termoBusca, setTermoBusca] = useState('')
  const [carregando, setCarregando] = useState(true)
  const [salvando, setSalvando] = useState(false)
  const [processandoId, setProcessandoId] = useState<string | null>(null)
  const [erroLista, setErroLista] = useState<string | null>(null)
  const [erroFormulario, setErroFormulario] = useState<string | null>(null)
  const [erroAcao, setErroAcao] = useState<string | null>(null)

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
          setErroLista('Não foi possível carregar as tarefas. Tente novamente mais tarde.')
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
  const totalTarefas = tarefas.length
  const tarefasPendentes = tarefas.filter((tarefa) => !tarefa.concluida).length
  const tarefasAtrasadas = tarefas.filter(
    (tarefa) => !tarefa.concluida && tarefa.prazo < hoje,
  ).length
  const tarefasConcluidas = tarefas.filter((tarefa) => tarefa.concluida).length
  const tarefasFiltradas = tarefas.filter((tarefa) =>
    tarefa.titulo.toLocaleLowerCase().includes(termoBusca.trim().toLocaleLowerCase()),
  )

  function abrirCriacao() {
    setTarefaEmEdicao(null)
    setErroFormulario(null)
    setModalAberto(true)
  }

  function abrirEdicao(tarefa: TarefaResumo) {
    setTarefaEmEdicao(tarefa)
    setErroFormulario(null)
    setModalAberto(true)
  }

  function fecharModal() {
    setErroFormulario(null)
    setModalAberto(false)
  }

  async function aoSalvarTarefa(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const dados = new FormData(evento.currentTarget)
    const titulo = String(dados.get('titulo') ?? '').trim()
    const prazo = String(dados.get('prazo') ?? '')
    const prioridade = String(dados.get('prioridade') ?? '') as Prioridade
    const observacao = String(dados.get('observacao') ?? '').trim()

    if (!titulo || !prazo || !observacao) {
      setErroFormulario('Preencha todos os campos obrigatórios.')
      return
    }

    setSalvando(true)
    setErroFormulario(null)

    try {
      if (tarefaEmEdicao) {
        const tarefaAtualizada = await atualizarTarefa(tarefaEmEdicao.id, {
          titulo,
          prazo,
          prioridade,
          observacao,
          concluida: tarefaEmEdicao.concluida,
        })
        setTarefas((tarefasAtuais) =>
          tarefasAtuais.map((tarefa) =>
            tarefa.id === tarefaAtualizada.id ? tarefaAtualizada : tarefa,
          ),
        )
      } else {
        const tarefaCriada = await criarTarefa({ titulo, observacao, prazo, prioridade })
        setTarefas((tarefasAtuais) => [...tarefasAtuais, tarefaCriada])
      }

      fecharModal()
    } catch (erro) {
      setErroFormulario(mensagemDoErro(erro))
    } finally {
      setSalvando(false)
    }
  }

  async function alternarConclusao(tarefa: TarefaResumo) {
    setProcessandoId(tarefa.id)
    setErroAcao(null)

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
    } catch (erro) {
      setErroAcao(mensagemDoErro(erro))
    } finally {
      setProcessandoId(null)
    }
  }

  async function removerTarefa(tarefa: TarefaResumo) {
    const confirmou = window.confirm(`Excluir a tarefa “${tarefa.titulo}”?`)
    if (!confirmou) return

    setProcessandoId(tarefa.id)
    setErroAcao(null)

    try {
      await excluirTarefa(tarefa.id)
      setTarefas((tarefasAtuais) =>
        tarefasAtuais.filter((tarefaAtual) => tarefaAtual.id !== tarefa.id),
      )
    } catch (erro) {
      setErroAcao(mensagemDoErro(erro))
    } finally {
      setProcessandoId(null)
    }
  }

  return (
    <section className={styles.pagina}>
      <header className={styles.cabecalho}>
        <h1>Tarefas</h1>
        <button type="button" className={styles.botaoNovaTarefa} onClick={abrirCriacao}>
          + Nova tarefa
        </button>
      </header>

      <div className={styles.resumo} aria-label="Resumo das tarefas">
        <article>
          <strong>{totalTarefas}</strong>
          <span>Tarefas</span>
        </article>
        <article>
          <strong>{tarefasPendentes}</strong>
          <span>Pendentes</span>
        </article>
        <article>
          <strong>{tarefasAtrasadas}</strong>
          <span>Atrasadas</span>
        </article>
        <article>
          <strong>{tarefasConcluidas}</strong>
          <span>Concluídas</span>
        </article>
      </div>

      <section className={styles.lista} aria-labelledby="titulo-lista">
        <header className={styles.cabecalhoLista}>
          <h2 id="titulo-lista">Tarefas</h2>
          <input
            type="search"
            placeholder="Buscar"
            aria-label="Buscar tarefas"
            value={termoBusca}
            onChange={(evento) => setTermoBusca(evento.target.value)}
          />
        </header>

        {erroAcao && <p className={styles.erroAcao} role="alert">{erroAcao}</p>}

        <div className={styles.tabelaResponsiva}>
          <table>
            <thead>
              <tr>
                <th scope="col">Tarefa</th>
                <th scope="col">Prazo</th>
                <th scope="col">Prioridade</th>
                <th scope="col">Situação</th>
                <th scope="col">Ações</th>
              </tr>
            </thead>
            <tbody>
              {carregando && (
                <tr>
                  <td colSpan={5} className={styles.estadoLista}>Carregando tarefas...</td>
                </tr>
              )}
              {erroLista && !carregando && (
                <tr>
                  <td colSpan={5} className={styles.estadoLista}>{erroLista}</td>
                </tr>
              )}
              {!carregando && !erroLista && tarefas.length === 0 && (
                <tr>
                  <td colSpan={5} className={styles.estadoLista}>Nenhuma tarefa cadastrada.</td>
                </tr>
              )}
              {!carregando && !erroLista && tarefas.length > 0 && tarefasFiltradas.length === 0 && (
                <tr>
                  <td colSpan={5} className={styles.estadoLista}>Nenhuma tarefa encontrada.</td>
                </tr>
              )}
              {tarefasFiltradas.map((tarefa) => (
                <tr key={tarefa.id}>
                  <td>{tarefa.titulo}</td>
                  <td>{formatarPrazo(tarefa.prazo)}</td>
                  <td>
                    <span className={`${styles.prioridade} ${classesPrioridade[tarefa.prioridade]}`}>
                      {rotulosPrioridade[tarefa.prioridade]}
                    </span>
                  </td>
                  <td>
                    <span className={tarefa.concluida ? styles.concluida : styles.pendente}>
                      {tarefa.concluida ? 'Concluída' : 'Pendente'}
                    </span>
                  </td>
                  <td>
                    <div className={styles.acoesTabela}>
                      <button
                        type="button"
                        className={styles.botaoAcao}
                        onClick={() => abrirEdicao(tarefa)}
                        disabled={processandoId === tarefa.id}
                      >
                        Editar
                      </button>
                      <button
                        type="button"
                        className={styles.botaoAcao}
                        onClick={() => alternarConclusao(tarefa)}
                        disabled={processandoId === tarefa.id}
                      >
                        {tarefa.concluida ? 'Reabrir' : 'Concluir'}
                      </button>
                      <button
                        type="button"
                        className={`${styles.botaoAcao} ${styles.botaoExcluir}`}
                        onClick={() => removerTarefa(tarefa)}
                        disabled={processandoId === tarefa.id}
                      >
                        Excluir
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <Modal
        aberto={modalAberto}
        titulo={tarefaEmEdicao ? 'Editar tarefa' : 'Nova tarefa'}
        aoFechar={fecharModal}
      >
        <form className={styles.formularioNovaTarefa} onSubmit={aoSalvarTarefa}>
          <div className={styles.linhaFormulario}>
            <label className={styles.grupoCampo}>
              <span>Prioridade</span>
              <select name="prioridade" defaultValue={tarefaEmEdicao?.prioridade ?? 'media'}>
                <option value="alta">Alta</option>
                <option value="media">Média</option>
                <option value="baixa">Baixa</option>
              </select>
            </label>

            <label className={styles.grupoCampo}>
              <span>Prazo</span>
              <input name="prazo" type="date" defaultValue={tarefaEmEdicao?.prazo} required />
            </label>
          </div>

          <label className={styles.grupoCampo}>
            <span>Título</span>
            <input
              name="titulo"
              type="text"
              placeholder="Informe o título da tarefa"
              defaultValue={tarefaEmEdicao?.titulo}
              required
            />
          </label>

          <label className={styles.grupoCampo}>
            <span>Observação</span>
            <textarea
              name="observacao"
              placeholder="Adicione uma observação sobre a tarefa"
              rows={4}
              defaultValue={tarefaEmEdicao?.observacao}
              required
            />
          </label>

          {erroFormulario && <p className={styles.erroFormulario} role="alert">{erroFormulario}</p>}

          <div className={styles.acoesFormulario}>
            <button type="submit" className={styles.botaoSalvar} disabled={salvando}>
              {salvando ? 'Salvando...' : 'Salvar'}
            </button>
          </div>
        </form>
      </Modal>
    </section>
  )
}
