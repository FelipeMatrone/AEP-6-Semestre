import { useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { anotacoesIniciais, categoriasIniciais, type Anotacao } from './dados'
import styles from './Categoria.module.css'

export default function Categoria() {
  const { id } = useParams()
  const categoria = categoriasIniciais.find((item) => item.id === Number(id))

  const [anotacoes, setAnotacoes] = useState(anotacoesIniciais)
  const [busca, setBusca] = useState('')
  const [anotacaoSelecionadaId, setAnotacaoSelecionadaId] = useState<
    number | null
  >(
    () =>
      anotacoesIniciais.find(
        (anotacao) => anotacao.categoriaId === categoria?.id,
      )?.id ?? null,
  )
  const [salvo, setSalvo] = useState(true)
  const areaTextoRef = useRef<HTMLDivElement>(null)

  const anotacoesDaCategoria = categoria
    ? anotacoes.filter((anotacao) => anotacao.categoriaId === categoria.id)
    : []

  const termo = busca.trim().toLowerCase()
  const anotacoesFiltradas = termo
    ? anotacoesDaCategoria.filter((anotacao) =>
        anotacao.titulo.toLowerCase().includes(termo),
      )
    : anotacoesDaCategoria

  const anotacaoSelecionada = anotacoes.find(
    (anotacao) => anotacao.id === anotacaoSelecionadaId,
  )

  useEffect(() => {
    if (salvo) return
    const temporizador = setTimeout(() => setSalvo(true), 600)
    return () => clearTimeout(temporizador)
  }, [salvo])

  function atualizarAnotacaoSelecionada(campos: Partial<Anotacao>) {
    if (!anotacaoSelecionada) return
    setAnotacoes((atuais) =>
      atuais.map((anotacao) =>
        anotacao.id === anotacaoSelecionada.id
          ? { ...anotacao, ...campos }
          : anotacao,
      ),
    )
    setSalvo(false)
  }

  function criarAnotacao() {
    if (!categoria) return

    const nova: Anotacao = {
      id: Date.now(),
      categoriaId: categoria.id,
      titulo: '',
      conteudo: '',
      atualizadaEm: 'Editado agora',
    }

    setAnotacoes((atuais) => [nova, ...atuais])
    setAnotacaoSelecionadaId(nova.id)
    setSalvo(true)
  }

  // document.execCommand é deprecated mas ainda suportado em todos os navegadores
  // relevantes; trocar por uma lib (Tiptap/Slate) só se a formatação básica
  // (negrito, itálico, lista) deixar de bastar.
  function aplicarFormatacao(comando: 'bold' | 'italic' | 'insertUnorderedList') {
    const areaTexto = areaTextoRef.current
    if (!areaTexto || !anotacaoSelecionada) return

    areaTexto.focus()
    document.execCommand(comando)
    atualizarAnotacaoSelecionada({ conteudo: areaTexto.innerHTML })
  }

  // Evita colar HTML de fora (ex: de outro site) dentro do contentEditable —
  // só o texto puro entra, o resto da formatação vem dos botões da barra.
  function aoColar(evento: React.ClipboardEvent<HTMLDivElement>) {
    evento.preventDefault()
    document.execCommand('insertText', false, evento.clipboardData.getData('text/plain'))
    if (areaTextoRef.current) {
      atualizarAnotacaoSelecionada({ conteudo: areaTextoRef.current.innerHTML })
    }
  }

  // Arrastar conteúdo de outra aba para cá é o mesmo buraco que aoColar fecha,
  // por um caminho diferente: o navegador insere o HTML da origem direto no
  // contentEditable, sem passar por sanitização nenhuma. Como o conteúdo volta
  // à tela por dangerouslySetInnerHTML, o drop fica bloqueado — arrastar dentro
  // do editor é conveniência marginal perto do risco.
  function aoSoltar(evento: React.DragEvent<HTMLDivElement>) {
    evento.preventDefault()
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
                    <span>{anotacao.atualizadaEm}</span>
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
                placeholder="Título da anotação"
                aria-label="Título da anotação"
              />

              <div className={styles.barraFormatacao}>
                <button
                  type="button"
                  className={styles.botaoFormatacao}
                  onMouseDown={(evento) => evento.preventDefault()}
                  onClick={() => aplicarFormatacao('bold')}
                  aria-label="Negrito"
                  title="Negrito"
                >
                  <strong>B</strong>
                </button>
                <button
                  type="button"
                  className={styles.botaoFormatacao}
                  onMouseDown={(evento) => evento.preventDefault()}
                  onClick={() => aplicarFormatacao('italic')}
                  aria-label="Itálico"
                  title="Itálico"
                >
                  <em>I</em>
                </button>
                <button
                  type="button"
                  className={styles.botaoFormatacao}
                  onMouseDown={(evento) => evento.preventDefault()}
                  onClick={() => aplicarFormatacao('insertUnorderedList')}
                  aria-label="Lista"
                  title="Lista"
                >
                  •
                </button>
              </div>

              <div
                key={anotacaoSelecionada.id}
                ref={areaTextoRef}
                className={styles.areaTexto}
                contentEditable
                suppressContentEditableWarning
                onInput={(evento) =>
                  atualizarAnotacaoSelecionada({
                    conteudo: evento.currentTarget.innerHTML,
                  })
                }
                onPaste={aoColar}
                onDrop={aoSoltar}
                dangerouslySetInnerHTML={{ __html: anotacaoSelecionada.conteudo }}
                data-placeholder="Comece a escrever…"
                aria-label="Conteúdo da anotação"
                role="textbox"
                aria-multiline="true"
              />

              <div className={styles.rodapeEditor}>
                <span className={styles.statusSalvamento}>
                  {salvo ? 'Salvo' : 'Salvando…'}
                </span>
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
