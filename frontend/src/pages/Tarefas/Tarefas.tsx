import { useEffect, useState, type FormEvent } from 'react'
import Modal from '../../components/Modal'
import {
  criarTarefa,
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

export default function Tarefas() {
  const [tarefas, setTarefas] = useState<TarefaResumo[]>([])
  const [modalAberto, setModalAberto] = useState(false)
  const [carregando, setCarregando] = useState(true)
  const [salvando, setSalvando] = useState(false)
  const [erroLista, setErroLista] = useState<string | null>(null)
  const [erroFormulario, setErroFormulario] = useState<string | null>(null)

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

  function fecharModal() {
    setErroFormulario(null)
    setModalAberto(false)
  }

  async function aoCriarTarefa(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const dados = new FormData(evento.currentTarget)
    const titulo = String(dados.get('titulo') ?? '').trim()
    const dono = String(dados.get('dono') ?? '').trim()
    const prazo = String(dados.get('prazo') ?? '')
    const prioridade = String(dados.get('prioridade') ?? '') as Prioridade

    if (!titulo || !dono || !prazo) {
      setErroFormulario('Preencha todos os campos obrigatórios.')
      return
    }

    setSalvando(true)
    setErroFormulario(null)

    try {
      const tarefaCriada = await criarTarefa({ titulo, dono, prazo, prioridade })
      setTarefas((tarefasAtuais) => [...tarefasAtuais, tarefaCriada])
      fecharModal()
    } catch (erro) {
      setErroFormulario(
        erro instanceof Error ? erro.message : 'Não foi possível salvar a tarefa.',
      )
    } finally {
      setSalvando(false)
    }
  }

  return (
    <section className={styles.pagina}>
      <header className={styles.cabecalho}>
        <h1>Tarefas</h1>
        <button
          type="button"
          className={styles.botaoNovaTarefa}
          onClick={() => setModalAberto(true)}
        >
          + Nova tarefa
        </button>
      </header>

      <div className={styles.resumo} aria-label="Resumo das tarefas">
        <article>
          <strong>12</strong>
          <span>Tarefas</span>
        </article>
        <article>
          <strong>4</strong>
          <span>Pendentes</span>
        </article>
        <article>
          <strong>5</strong>
          <span>Atrasadas</span>
        </article>
        <article>
          <strong>124</strong>
          <span>Concluídas</span>
        </article>
      </div>

      <section className={styles.lista} aria-labelledby="titulo-lista">
        <header className={styles.cabecalhoLista}>
          <h2 id="titulo-lista">Tarefas</h2>
          <input type="search" placeholder="Buscar" aria-label="Buscar tarefas" />
        </header>

        <div className={styles.tabelaResponsiva}>
          <table>
            <thead>
              <tr>
                <th scope="col">Tarefa</th>
                <th scope="col">Prazo</th>
                <th scope="col">Prioridade</th>
              </tr>
            </thead>
            <tbody>
              {carregando && (
                <tr>
                  <td colSpan={3} className={styles.estadoLista}>Carregando tarefas...</td>
                </tr>
              )}
              {erroLista && !carregando && (
                <tr>
                  <td colSpan={3} className={styles.estadoLista}>{erroLista}</td>
                </tr>
              )}
              {!carregando && !erroLista && tarefas.length === 0 && (
                <tr>
                  <td colSpan={3} className={styles.estadoLista}>Nenhuma tarefa cadastrada.</td>
                </tr>
              )}
              {tarefas.map((tarefa) => (
                <tr key={tarefa.id}>
                  <td>{tarefa.titulo}</td>
                  <td>{formatarPrazo(tarefa.prazo)}</td>
                  <td>
                    <span className={`${styles.prioridade} ${classesPrioridade[tarefa.prioridade]}`}>
                      {rotulosPrioridade[tarefa.prioridade]}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <Modal aberto={modalAberto} titulo="Nova tarefa" aoFechar={fecharModal}>
        <form className={styles.formularioNovaTarefa} onSubmit={aoCriarTarefa}>
          <div className={styles.linhaFormulario}>
            <label className={styles.grupoCampo}>
              <span>Prioridade</span>
              <select name="prioridade" defaultValue="media">
                <option value="alta">Alta</option>
                <option value="media">Média</option>
                <option value="baixa">Baixa</option>
              </select>
            </label>

            <label className={styles.grupoCampo}>
              <span>Prazo</span>
              <input name="prazo" type="date" required />
            </label>
          </div>

          <label className={styles.grupoCampo}>
            <span>Título</span>
            <input name="titulo" type="text" placeholder="Informe o título da tarefa" required />
          </label>

          <label className={styles.grupoCampo}>
            <span>Dono</span>
            <input name="dono" type="text" placeholder="Informe o responsável pela tarefa" required />
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
