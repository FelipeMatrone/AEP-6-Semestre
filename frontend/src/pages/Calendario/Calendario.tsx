import { useEffect, useState, type FormEvent, type MouseEvent } from 'react'
import Modal from '../../components/Modal'
import { listarCategorias, type CategoriaResumo, type Cor } from '../../services/categorias'
import { atualizarEventoCalendario, criarEventoCalendario, excluirEventoCalendario, listarEventosCalendario, type EventoCalendario } from '../../services/eventosCalendario'
import { criarTarefa, listarTarefas, type TarefaResumo } from '../../services/tarefas'
import styles from './Calendario.module.css'

const nomesMeses = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro']
const diasDaSemana = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom']
const cores: Cor[] = ['roxo', 'magenta', 'verde', 'ambar']

type CriacaoRapida = { data: string; esquerda: number; topo: number }

function primeiroDiaDoMes(data: Date) { return new Date(data.getFullYear(), data.getMonth(), 1) }
function chaveDaData(data: Date) { return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}-${String(data.getDate()).padStart(2, '0')}` }
function diasDoCalendario(mes: Date) {
  const primeiroDia = primeiroDiaDoMes(mes)
  const deslocamento = (primeiroDia.getDay() + 6) % 7
  return Array.from({ length: 42 }, (_, indice) => new Date(mes.getFullYear(), mes.getMonth(), 1 - deslocamento + indice))
}
function formatarData(data: string) { return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'long' }).format(new Date(`${data}T00:00:00`)) }

export default function Calendario() {
  const [tarefas, setTarefas] = useState<TarefaResumo[]>([])
  const [eventos, setEventos] = useState<EventoCalendario[]>([])
  const [categorias, setCategorias] = useState<CategoriaResumo[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [mesExibido, setMesExibido] = useState(() => primeiroDiaDoMes(new Date()))
  const [categoriaSelecionada, setCategoriaSelecionada] = useState('')
  const [criacaoRapida, setCriacaoRapida] = useState<CriacaoRapida | null>(null)
  const [tituloRapido, setTituloRapido] = useState('')
  const [eventoAberto, setEventoAberto] = useState<EventoCalendario | null>(null)
  const [eventoEmEdicao, setEventoEmEdicao] = useState<EventoCalendario | null>(null)
  const [salvando, setSalvando] = useState(false)
  const [erroAcao, setErroAcao] = useState<string | null>(null)

  useEffect(() => {
    let telaAtiva = true
    Promise.allSettled([listarTarefas(), listarEventosCalendario(), listarCategorias()])
      .then(([resultadoTarefas, resultadoEventos, resultadoCategorias]) => {
        if (!telaAtiva) return

        if (resultadoTarefas.status !== 'fulfilled' || resultadoCategorias.status !== 'fulfilled') {
          setErro('Não foi possível carregar o calendário. Tente novamente.')
          return
        }

        setTarefas(resultadoTarefas.value)
        setCategorias(resultadoCategorias.value)
        setEventos(resultadoEventos.status === 'fulfilled' ? resultadoEventos.value : [])
      })
      .finally(() => telaAtiva && setCarregando(false))
    return () => { telaAtiva = false }
  }, [])

  const eventosPorData = eventos.reduce((agrupados, evento) => {
    const itens = agrupados.get(evento.data) ?? []
    itens.push(evento); agrupados.set(evento.data, itens); return agrupados
  }, new Map<string, EventoCalendario[]>())
  const tarefasPorData = tarefas.filter((tarefa) => !categoriaSelecionada || tarefa.categoriaId === categoriaSelecionada).reduce((agrupadas, tarefa) => {
    const itens = agrupadas.get(tarefa.prazo) ?? []
    itens.push(tarefa); agrupadas.set(tarefa.prazo, itens); return agrupadas
  }, new Map<string, TarefaResumo[]>())

  function alterarMes(deslocamento: number) { setMesExibido((mes) => new Date(mes.getFullYear(), mes.getMonth() + deslocamento, 1)); setEventoAberto(null) }
  function abrirCriacaoRapida(evento: MouseEvent<HTMLElement>, data: Date) {
    evento.preventDefault()
    const calendario = evento.currentTarget.closest(`.${styles.calendario}`)
    if (!calendario) return
    const limite = calendario.getBoundingClientRect()
    setCriacaoRapida({ data: chaveDaData(data), esquerda: Math.max(14, Math.min(evento.clientX - limite.left, limite.width - 286)), topo: Math.max(14, Math.min(evento.clientY - limite.top, limite.height - 160)) })
    setTituloRapido(''); setErroAcao(null)
  }
  async function salvarCriacaoRapida(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (!criacaoRapida || !tituloRapido.trim()) return
    setSalvando(true); setErroAcao(null)
    try {
      const criado = await criarEventoCalendario({ titulo: tituloRapido.trim(), descricao: null, data: criacaoRapida.data, horario: null, cor: 'roxo' })
      setEventos((atuais) => [...atuais, criado]); setCriacaoRapida(null)
    } catch (causa) { setErroAcao(causa instanceof Error ? causa.message : 'Não foi possível criar o evento.') } finally { setSalvando(false) }
  }
  async function excluirEvento() {
    if (!eventoAberto || !window.confirm(`Excluir “${eventoAberto.titulo}”?`)) return
    setSalvando(true)
    try { await excluirEventoCalendario(eventoAberto.id); setEventos((atuais) => atuais.filter((evento) => evento.id !== eventoAberto.id)); setEventoAberto(null) }
    catch (causa) { setErroAcao(causa instanceof Error ? causa.message : 'Não foi possível excluir o evento.') } finally { setSalvando(false) }
  }
  async function adicionarComoTarefa() {
    if (!eventoAberto || eventoAberto.tarefaId) return
    setSalvando(true)
    try {
      const tarefa = await criarTarefa({ titulo: eventoAberto.titulo, observacao: eventoAberto.descricao || eventoAberto.titulo, prazo: eventoAberto.data, prioridade: 'media', categoriaId: categoriaSelecionada || null })
      const atualizado = await atualizarEventoCalendario(eventoAberto.id, { ...eventoAberto, tarefaId: tarefa.id })
      setTarefas((atuais) => [...atuais, tarefa]); setEventos((atuais) => atuais.map((evento) => evento.id === atualizado.id ? atualizado : evento)); setEventoAberto(atualizado)
    } catch (causa) { setErroAcao(causa instanceof Error ? causa.message : 'Não foi possível adicionar como tarefa.') } finally { setSalvando(false) }
  }
  async function salvarEdicao(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (!eventoEmEdicao) return
    const dados = new FormData(evento.currentTarget); const titulo = String(dados.get('titulo') ?? '').trim()
    if (!titulo) return
    setSalvando(true)
    try {
      const atualizado = await atualizarEventoCalendario(eventoEmEdicao.id, { ...eventoEmEdicao, titulo, descricao: String(dados.get('descricao') ?? '').trim() || null, horario: String(dados.get('horario') ?? '') || null, cor: String(dados.get('cor')) as Cor })
      setEventos((atuais) => atuais.map((item) => item.id === atualizado.id ? atualizado : item)); setEventoAberto(atualizado); setEventoEmEdicao(null)
    } catch (causa) { setErroAcao(causa instanceof Error ? causa.message : 'Não foi possível salvar o evento.') } finally { setSalvando(false) }
  }

  return <section className={styles.pagina}>
    <header className={styles.topo}>
      <h1>Calendário</h1>
      <label className={styles.filtro}><span>Disciplina</span><select value={categoriaSelecionada} onChange={(evento) => setCategoriaSelecionada(evento.target.value)}><option value="">Todas</option>{categorias.map((categoria) => <option key={categoria.id} value={categoria.id}>{categoria.nome}</option>)}</select></label>
    </header>
    {carregando && <p className={styles.estado}>Carregando calendário...</p>}
    {erro && <p className={styles.estado}>{erro}</p>}
    {!carregando && !erro && <section className={styles.calendario} aria-label="Calendário mensal">
      <header className={styles.cabecalhoCalendario}><h2>{nomesMeses[mesExibido.getMonth()]} de {mesExibido.getFullYear()}</h2><div><button type="button" onClick={() => alterarMes(-1)} aria-label="Mês anterior">‹</button><button type="button" onClick={() => alterarMes(1)} aria-label="Próximo mês">›</button></div></header>
      <div className={styles.gradeCalendario}>
        {diasDaSemana.map((dia) => <span key={dia} className={styles.nomeDia}>{dia}</span>)}
        {diasDoCalendario(mesExibido).map((dia) => {
          const chave = chaveDaData(dia); const eventosDoDia = eventosPorData.get(chave) ?? []; const tarefasDoDia = tarefasPorData.get(chave) ?? []
          return <div key={chave} className={`${styles.dia} ${dia.getMonth() === mesExibido.getMonth() ? '' : styles.diaForaDoMes}`} onContextMenu={(evento) => abrirCriacaoRapida(evento, dia)}>
            <span className={styles.numeroDia}>{dia.getDate()}</span><div className={styles.itensDia}>
              {eventosDoDia.map((evento) => <button key={evento.id} type="button" className={`${styles.evento} ${styles[evento.cor]}`} onClick={() => { setEventoAberto(evento); setCriacaoRapida(null) }}>{evento.horario && <span>{evento.horario}</span>}{evento.titulo}</button>)}
              {tarefasDoDia.map((tarefa) => <span key={tarefa.id} className={styles.tarefa}>{tarefa.titulo}</span>)}
            </div>
          </div>
        })}
      </div>
      {criacaoRapida && <form className={styles.criacaoRapida} style={{ left: criacaoRapida.esquerda, top: criacaoRapida.topo }} onSubmit={salvarCriacaoRapida}><header><strong>Novo evento</strong><button type="button" onClick={() => setCriacaoRapida(null)} aria-label="Fechar">×</button></header><span>{formatarData(criacaoRapida.data)}</span><input autoFocus value={tituloRapido} onChange={(evento) => setTituloRapido(evento.target.value)} placeholder="Descreva o evento" /><button type="submit" disabled={salvando || !tituloRapido.trim()}>{salvando ? 'Salvando...' : 'Salvar'}</button></form>}
      {eventoAberto && <aside className={styles.painelEvento}><button type="button" className={styles.fecharPainel} onClick={() => setEventoAberto(null)} aria-label="Fechar detalhes">×</button><span className={`${styles.marcadorCor} ${styles[eventoAberto.cor]}`} /><h3>{eventoAberto.titulo}</h3><p>{formatarData(eventoAberto.data)}{eventoAberto.horario ? ` · ${eventoAberto.horario}` : ''}</p>{eventoAberto.descricao && <p className={styles.descricaoEvento}>{eventoAberto.descricao}</p>}<div className={styles.acoesEvento}><button type="button" onClick={() => setEventoEmEdicao(eventoAberto)}>Editar</button><button type="button" onClick={excluirEvento} disabled={salvando}>Excluir</button><button type="button" onClick={adicionarComoTarefa} disabled={salvando || Boolean(eventoAberto.tarefaId)}>{eventoAberto.tarefaId ? 'Adicionado como tarefa' : 'Adicionar como tarefa'}</button></div></aside>}
    </section>}
    <Modal aberto={Boolean(eventoEmEdicao)} titulo="Editar evento" aoFechar={() => setEventoEmEdicao(null)}>{eventoEmEdicao && <form className={styles.formularioEvento} onSubmit={salvarEdicao}><label>Título<input name="titulo" defaultValue={eventoEmEdicao.titulo} required /></label><label>Horário<input name="horario" type="time" defaultValue={eventoEmEdicao.horario ?? ''} /></label><label>Cor<select name="cor" defaultValue={eventoEmEdicao.cor}>{cores.map((cor) => <option key={cor} value={cor}>{cor}</option>)}</select></label><label>Descrição detalhada<textarea name="descricao" rows={5} defaultValue={eventoEmEdicao.descricao ?? ''} placeholder="Adicione uma descrição mais completa" /></label>{erroAcao && <p className={styles.erroAcao}>{erroAcao}</p>}<button type="submit" disabled={salvando}>{salvando ? 'Salvando...' : 'Salvar alterações'}</button></form>}</Modal>
  </section>
}
