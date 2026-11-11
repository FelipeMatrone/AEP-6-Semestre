import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  buscarCategoria,
  criarAnotacao as criarAnotacaoNaApi,
  atualizarAnotacao as atualizarAnotacaoNaApi,
  type Anotacao,
  type Categoria as CategoriaDaApi,
} from '../../services/categorias'
import styles from './Categoria.module.css'

export default function Categoria() {
  const { id } = useParams()
  const [categoria, setCategoria] = useState<CategoriaDaApi | null>(null)
  const [busca, setBusca] = useState('')
  const [anotacaoSelecionadaId, setAnotacaoSelecionadaId] = useState<string | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [tentativa, setTentativa] = useState(0)
  const [salvo, setSalvo] = useState(true)
  const [salvando, setSalvando] = useState(false)
  const [erroSalvamento, setErroSalvamento] = useState<string | null>(null)

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
