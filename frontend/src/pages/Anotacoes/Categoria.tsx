import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  buscarCategoria,
  criarAnotacao as criarAnotacaoNaApi,
  atualizarAnotacao as atualizarAnotacaoNaApi,
  atualizarCategoria as atualizarCategoriaNaApi,
  excluirCategoria as excluirCategoriaNaApi,
  excluirAnotacao as excluirAnotacaoNaApi,
  type Anotacao,
  type Categoria as CategoriaDaApi,
} from '../../services/categorias'
import styles from './Categoria.module.css'

export default function Categoria() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [categoria, setCategoria] = useState<CategoriaDaApi | null>(null)
  const [busca, setBusca] = useState('')
  const [anotacaoSelecionadaId, setAnotacaoSelecionadaId] = useState<string | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [tentativa, setTentativa] = useState(0)
  const [salvo, setSalvo] = useState(true)
  const [salvando, setSalvando] = useState(false)
  const [excluindo, setExcluindo] = useState(false)
  const [erroSalvamento, setErroSalvamento] = useState<string | null>(null)
  const [editandoCategoria, setEditandoCategoria] = useState(false)
  const [nomeCategoria, setNomeCategoria] = useState('')
  const [descricaoCategoria, setDescricaoCategoria] = useState('')
  const [salvandoCategoria, setSalvandoCategoria] = useState(false)

  const anotacoesDaCategoria = categoria?.anotacoes ?? []

  const termo = busca.trim().toLowerCase()
  const anotacoesFiltradas = termo
    ? anotacoesDaCategoria.filter((anotacao) =>
        anotacao.titulo.toLowerCase().includes(termo),
      )
    : anotacoesDaCategoria

  const anotacaoSelecionada = anotacoesDaCategoria.find(
    (anotacao) => anotacao.id === anotacaoSelecionadaId,
  )

  useEffect(() => {
    let telaAtiva = true

    async function carregarCategoria() {
      if (!id) {
        setCarregando(false)
        return
      }

      setCarregando(true)
      setErro(null)

      try {
        const resposta = await buscarCategoria(id)
        if (telaAtiva) {
          setCategoria(resposta)
          setNomeCategoria(resposta.nome)
          setDescricaoCategoria(resposta.descricao ?? '')
          setAnotacaoSelecionadaId(resposta.anotacoes[0]?.id ?? null)
        }
      } catch {
        if (telaAtiva) {
          setErro('Não foi possível carregar esta categoria. Tente novamente.')
        }
      } finally {
        if (telaAtiva) {
          setCarregando(false)
        }
      }
    }

    carregarCategoria()

    return () => {
      telaAtiva = false
    }
  }, [id, tentativa])

  function atualizarAnotacaoSelecionada(campos: Partial<Anotacao>) {
    if (!anotacaoSelecionada) return
    setCategoria((atual) => atual && ({
      ...atual,
      anotacoes: atual.anotacoes.map((anotacao) =>
        anotacao.id === anotacaoSelecionada.id ? { ...anotacao, ...campos } : anotacao,
      ),
    }))
    setSalvo(false)
    setErroSalvamento(null)
  }

  async function salvarAnotacaoSelecionada() {
    if (!categoria || !anotacaoSelecionada || salvo || salvando) return

    const titulo = anotacaoSelecionada.titulo.trim()
    const conteudo = anotacaoSelecionada.conteudo.trim()
    if (!titulo || !conteudo) {
      setErroSalvamento('Título e conteúdo são obrigatórios para salvar.')
      return
    }

    setSalvando(true)
    setErroSalvamento(null)

    try {
      const anotacao = await atualizarAnotacaoNaApi(categoria.id, anotacaoSelecionada.id, {
        titulo,
        conteudo,
      })
      setCategoria((atual) => atual && ({
        ...atual,
        anotacoes: atual.anotacoes.map((item) =>
          item.id === anotacao.id ? anotacao : item,
        ),
      }))
      setSalvo(true)
    } catch (erro) {
      setErroSalvamento(
        erro instanceof Error ? erro.message : 'Não foi possível salvar a anotação.',
      )
    } finally {
      setSalvando(false)
    }
  }

  async function criarAnotacao() {
    if (!categoria) return

    try {
      const nova = await criarAnotacaoNaApi(categoria.id, {
        titulo: 'Nova anotação',
        conteudo: 'Comece a escrever...',
      })
      setCategoria((atual) => atual && ({
        ...atual,
        anotacoes: [nova, ...atual.anotacoes],
      }))
      setAnotacaoSelecionadaId(nova.id)
      setSalvo(true)
    } catch {
      setErro('Não foi possível criar a anotação. Tente novamente.')
    }
  }

  async function excluirAnotacaoSelecionada() {
    if (!categoria || !anotacaoSelecionada) return

    const confirmou = window.confirm(`Excluir a anotação “${anotacaoSelecionada.titulo}”?`)
    if (!confirmou) return

    setExcluindo(true)
    setErroSalvamento(null)

    try {
      await excluirAnotacaoNaApi(categoria.id, anotacaoSelecionada.id)
      setCategoria((atual) => atual && ({
        ...atual,
        anotacoes: atual.anotacoes.filter((item) => item.id !== anotacaoSelecionada.id),
      }))
      setAnotacaoSelecionadaId(
        anotacoesDaCategoria.find((item) => item.id !== anotacaoSelecionada.id)?.id ?? null,
      )
      setSalvo(true)
    } catch (erro) {
      setErroSalvamento(
        erro instanceof Error ? erro.message : 'Não foi possível excluir a anotação.',
      )
    } finally {
      setExcluindo(false)
    }
  }

  async function salvarCategoria() {
    if (!categoria || !nomeCategoria.trim()) return

    setSalvandoCategoria(true)
    setErro(null)

    try {
      const atualizada = await atualizarCategoriaNaApi(categoria.id, {
        nome: nomeCategoria.trim(),
        descricao: descricaoCategoria.trim(),
        cor: categoria.cor,
      })
      setCategoria(atualizada)
      setEditandoCategoria(false)
    } catch (erro) {
      setErro(erro instanceof Error ? erro.message : 'Não foi possível salvar a categoria.')
    } finally {
      setSalvandoCategoria(false)
    }
  }

  async function excluirCategoria() {
    if (!categoria || !window.confirm(`Excluir a categoria “${categoria.nome}”?`)) return

    setSalvandoCategoria(true)
    setErro(null)

    try {
      await excluirCategoriaNaApi(categoria.id)
      navigate('/anotacoes')
    } catch (erro) {
      setErro(erro instanceof Error ? erro.message : 'Não foi possível excluir a categoria.')
      setSalvandoCategoria(false)
    }
  }

  function formatarAtualizacao(data: string) {
    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(data))
  }

  if (carregando) {
    return <p className={styles.mensagemLateral}>Carregando categoria...</p>
  }

  if (erro) {
    return (
      <div className={styles.naoEncontrada}>
        <h2>Não foi possível carregar a categoria</h2>
        <p>{erro}</p>
        <button type="button" className={styles.botaoNovaAnotacao} onClick={() => setTentativa((atual) => atual + 1)}>
          Tentar novamente
        </button>
      </div>
    )
  }

  if (!categoria) {
    return (
      <>
        <section className={styles.topo}>
          <Link to="/anotacoes" className={styles.voltar}>
            ← Voltar para categorias
          </Link>
        </section>

        <div className={styles.naoEncontrada}>
          <h2>Categoria não encontrada</h2>
          <p>Ela pode ter sido removida ou o link está incorreto.</p>
        </div>
      </>
    )
  }

  return (
    <>
      <section className={styles.topo}>
        <Link to="/anotacoes" className={styles.voltar}>
          ← Voltar para categorias
        </Link>
      </section>

      <div className={styles.tela}>
        <aside className={styles.barraLateral}>
          <div className={styles.cabecalhoLateral}>
            <span
              className={`${styles.icone} ${styles[categoria.cor]}`}
              aria-hidden="true"
            >
              {categoria.nome.charAt(0)}
            </span>
            <h1>{categoria.nome}</h1>
          </div>

          {editandoCategoria ? (
            <div className={styles.formularioCategoria}>
              <input
                value={nomeCategoria}
                onChange={(evento) => setNomeCategoria(evento.target.value)}
                aria-label="Nome da categoria"
              />
              <textarea
                value={descricaoCategoria}
                onChange={(evento) => setDescricaoCategoria(evento.target.value)}
                placeholder="Descrição da categoria"
                aria-label="Descrição da categoria"
              />
              <div>
                <button type="button" onClick={() => setEditandoCategoria(false)}>Cancelar</button>
                <button type="button" onClick={salvarCategoria} disabled={salvandoCategoria || !nomeCategoria.trim()}>
                  {salvandoCategoria ? 'Salvando...' : 'Salvar'}
                </button>
              </div>
            </div>
          ) : (
            <div className={styles.acoesCategoria}>
              <button type="button" onClick={() => setEditandoCategoria(true)}>Editar categoria</button>
              <button type="button" onClick={excluirCategoria} disabled={salvandoCategoria}>
                Excluir categoria
              </button>
            </div>
          )}

          <input
            type="search"
            className={styles.busca}
            value={busca}
            onChange={(evento) => setBusca(evento.target.value)}
            placeholder="Buscar anotação"
            aria-label="Buscar anotação"
          />

          <button
            type="button"
            className={styles.botaoNovaAnotacao}
            onClick={criarAnotacao}
          >
            <span aria-hidden="true">+</span> Nova anotação
          </button>

          {anotacoesDaCategoria.length === 0 ? (
            <p className={styles.mensagemLateral}>
              Nenhuma anotação nesta categoria ainda.
            </p>
          ) : anotacoesFiltradas.length === 0 ? (
            <p className={styles.mensagemLateral}>
              Nenhuma anotação encontrada para “{busca.trim()}”.
            </p>
          ) : (
            <ul className={styles.listaAnotacoes}>
              {anotacoesFiltradas.map((anotacao) => (
                <li key={anotacao.id}>
                  <button
                    type="button"
                    className={`${styles.itemAnotacao} ${
                      anotacao.id === anotacaoSelecionadaId
                        ? styles.itemAnotacaoAtiva
                        : ''
                    }`}
                    aria-current={anotacao.id === anotacaoSelecionadaId}
                    onClick={() => setAnotacaoSelecionadaId(anotacao.id)}
                  >
                    <strong>{anotacao.titulo || 'Sem título'}</strong>
                    <span>{formatarAtualizacao(anotacao.atualizadaEm)}</span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </aside>

        <section className={styles.editor}>
          {anotacaoSelecionada ? (
            <>
              <input
                type="text"
                className={styles.tituloAnotacao}
                value={anotacaoSelecionada.titulo}
                onChange={(evento) =>
                  atualizarAnotacaoSelecionada({ titulo: evento.target.value })
                }
                onBlur={salvarAnotacaoSelecionada}
                placeholder="Título da anotação"
                aria-label="Título da anotação"
              />

              <textarea
                key={anotacaoSelecionada.id}
                className={styles.areaTexto}
                value={anotacaoSelecionada.conteudo}
                onChange={(evento) =>
                  atualizarAnotacaoSelecionada({
                    conteudo: evento.target.value,
                  })
                }
                onBlur={salvarAnotacaoSelecionada}
                placeholder="Comece a escrever…"
                aria-label="Conteúdo da anotação"
              />

              <div className={styles.rodapeEditor}>
                <button
                  type="button"
                  className={styles.botaoExcluir}
                  onClick={excluirAnotacaoSelecionada}
                  disabled={excluindo || salvando}
                >
                  {excluindo ? 'Excluindo...' : 'Excluir anotação'}
                </button>
                <span className={styles.statusSalvamento}>
                  {salvando ? 'Salvando…' : salvo ? 'Salvo' : 'Alterações não salvas'}
                </span>
                {erroSalvamento && <span className={styles.erroSalvamento}>{erroSalvamento}</span>}
              </div>
            </>
          ) : (
            <div className={styles.vazioEditor}>
              <h2>Nenhuma anotação selecionada</h2>
              <p>Escolha uma anotação na lista ou crie uma nova para começar.</p>
            </div>
          )}
        </section>
      </div>
    </>
  )
}
