import { useEffect, useState } from 'react'
import { Link, Navigate, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { IconeCapelo } from './icones'
import { NOME_DO_SISTEMA } from '../config'
import { buscar, type ResultadoBusca } from '../services/busca'
import { buscarSessao, sair, type Usuario } from '../services/auth'
import styles from './Layout.module.css'

export type ContextoLayout = {
  usuario: Usuario
  setUsuario: (usuario: Usuario) => void
}

function classeItem({ isActive }: { isActive: boolean }) {
  return isActive ? styles.itemAtivo : ''
}

function iniciais(nome: string) {
  const partes = nome.trim().split(/\s+/)
  const ultima = partes.length > 1 ? partes[partes.length - 1][0] : ''
  return (partes[0][0] + ultima).toUpperCase()
}

export default function Layout() {
  const navigate = useNavigate()
  // undefined = ainda conferindo; null = sem sessão.
  const [usuario, setUsuario] = useState<Usuario | null | undefined>(undefined)
  const [menuRecolhido, setMenuRecolhido] = useState(false)
  const [termoBusca, setTermoBusca] = useState('')
  const [resultadoBusca, setResultadoBusca] = useState<ResultadoBusca | null>(null)
  const [estadoBusca, setEstadoBusca] = useState<'ocioso' | 'carregando' | 'pronto' | 'vazio' | 'erro'>('ocioso')
  const [buscaAberta, setBuscaAberta] = useState(false)

  useEffect(() => {
    let ativa = true
    buscarSessao()
      .then((atual) => ativa && setUsuario(atual))
      .catch(() => ativa && setUsuario(null))
    return () => {
      ativa = false
    }
  }, [])

  useEffect(() => {
    const termo = termoBusca.trim()
    if (!termo) {
      return
    }

    let ativa = true
    const temporizador = window.setTimeout(async () => {
      setEstadoBusca('carregando')

      try {
        const resposta = await buscar(termo)
        if (!ativa) return

        setResultadoBusca(resposta)
        const total = resposta.tarefas.length + resposta.categorias.length + resposta.anotacoes.length
        setEstadoBusca(total === 0 ? 'vazio' : 'pronto')
      } catch {
        if (ativa) {
          setResultadoBusca(null)
          setEstadoBusca('erro')
        }
      }
    }, 350)

    return () => {
      ativa = false
      window.clearTimeout(temporizador)
    }
  }, [termoBusca])

  const quantidadeResultados = resultadoBusca
    ? resultadoBusca.tarefas.length + resultadoBusca.categorias.length + resultadoBusca.anotacoes.length
    : 0

  function atualizarBusca(valor: string) {
    setTermoBusca(valor)
    setBuscaAberta(Boolean(valor.trim()))

    if (!valor.trim()) {
      setResultadoBusca(null)
      setEstadoBusca('ocioso')
    }
  }

  function fecharBusca() {
    setBuscaAberta(false)
  }

  async function aoSair() {
    // Mesmo se a API falhar, o usuário pediu para sair: segue para o login.
    await sair().catch(() => undefined)
    navigate('/login', { replace: true })
  }

  if (usuario === undefined) return null
  if (usuario === null) return <Navigate to="/login" replace />

  return (
    <div
      className={`${styles.pagina} ${
        menuRecolhido ? styles.paginaMenuRecolhido : ''
      }`}
    >
      <aside className={styles.menuLateral}>
        <button
          type="button"
          className={styles.botaoRecolher}
          onClick={() => setMenuRecolhido((recolhido) => !recolhido)}
          aria-label={menuRecolhido ? 'Expandir menu' : 'Recolher menu'}
          title={menuRecolhido ? 'Expandir menu' : 'Recolher menu'}
        >
          {menuRecolhido ? '›' : '‹'}
        </button>

        <div className={styles.marca}>
          <span className={styles.logo}>
            <IconeCapelo />
          </span>
          <span className={styles.textoMenu}>{NOME_DO_SISTEMA}</span>
        </div>

        <nav className={styles.navegacao} aria-label="Navegação principal">
          <NavLink to="/inicio" className={classeItem} title="Dashboard">
            <span className={styles.iconeMenu}>⌂</span>
            <span className={styles.textoMenu}>Dashboard</span>
          </NavLink>

          <NavLink to="/anotacoes" className={classeItem} title="Anotações">
            <span className={styles.iconeMenu}>✎</span>
            <span className={styles.textoMenu}>Anotações</span>
          </NavLink>

          <NavLink to="/tarefas" className={classeItem} title="Tarefas">
            <span className={styles.iconeMenu}>✓</span>
            <span className={styles.textoMenu}>Tarefas</span>
          </NavLink>

          <NavLink to="/calendario" className={classeItem} title="Calendário">
            <span className={styles.iconeMenu}>▦</span>
            <span className={styles.textoMenu}>Calendário</span>
          </NavLink>

          <NavLink to="/perfil" className={classeItem} title="Perfil">
            <span className={styles.iconeMenu}>☺</span>
            <span className={styles.textoMenu}>Perfil</span>
          </NavLink>
        </nav>

        <button type="button" className={styles.sair} title="Sair" onClick={aoSair}>
          <span className={styles.iconeMenu}>↪</span>
          <span className={styles.textoMenu}>Sair</span>
        </button>
      </aside>

      <main className={styles.conteudo}>
        <header className={styles.cabecalho}>
          <div className={styles.busca}>
            <label htmlFor="busca-global" className={styles.textoOculto}>Buscar</label>
            <input
              id="busca-global"
              type="search"
              placeholder="Busque por tarefas ou anotações"
              value={termoBusca}
              onChange={(evento) => atualizarBusca(evento.target.value)}
              onFocus={() => setBuscaAberta(Boolean(termoBusca.trim()))}
              onBlur={() => window.setTimeout(fecharBusca, 150)}
              onKeyDown={(evento) => {
                if (evento.key === 'Escape') {
                  setBuscaAberta(false)
                  evento.currentTarget.blur()
                }
              }}
              aria-expanded={buscaAberta}
              aria-controls="resultados-busca"
            />
            {buscaAberta && estadoBusca !== 'ocioso' && (
              <div id="resultados-busca" className={styles.estadoBusca}>
                {estadoBusca === 'carregando' && <p role="status">Buscando...</p>}
                {estadoBusca === 'vazio' && <p role="status">Nenhum resultado encontrado.</p>}
                {estadoBusca === 'erro' && <p role="status">Não foi possível realizar a busca.</p>}
                {estadoBusca === 'pronto' && resultadoBusca && (
                  <>
                    <p className={styles.resumoBusca} role="status">
                      {quantidadeResultados} resultado(s) encontrado(s)
                    </p>

                    {resultadoBusca.tarefas.length > 0 && (
                      <section className={styles.grupoResultados} aria-labelledby="resultados-tarefas">
                        <h2 id="resultados-tarefas">Tarefas</h2>
                        <ul>
                          {resultadoBusca.tarefas.map((tarefa) => (
                            <li key={tarefa.id}>
                              <Link to={`/tarefas?destaque=${tarefa.id}`} onClick={fecharBusca}>
                                {tarefa.titulo}
                              </Link>
                            </li>
                          ))}
                        </ul>
                      </section>
                    )}

                    {resultadoBusca.categorias.length > 0 && (
                      <section className={styles.grupoResultados} aria-labelledby="resultados-categorias">
                        <h2 id="resultados-categorias">Categorias</h2>
                        <ul>
                          {resultadoBusca.categorias.map((categoria) => (
                            <li key={categoria.id}>
                              <Link to={`/anotacoes/${categoria.id}`} onClick={fecharBusca}>
                                {categoria.nome}
                              </Link>
                            </li>
                          ))}
                        </ul>
                      </section>
                    )}

                    {resultadoBusca.anotacoes.length > 0 && (
                      <section className={styles.grupoResultados} aria-labelledby="resultados-anotacoes">
                        <h2 id="resultados-anotacoes">Anotações</h2>
                        <ul>
                          {resultadoBusca.anotacoes.map((anotacao) => (
                            <li key={anotacao.id}>
                              <Link
                                to={`/anotacoes/${anotacao.categoriaId}?anotacao=${anotacao.id}`}
                                onClick={fecharBusca}
                              >
                                <strong>{anotacao.titulo}</strong>
                                <span>{anotacao.categoriaNome}</span>
                              </Link>
                            </li>
                          ))}
                        </ul>
                      </section>
                    )}
                  </>
                )}
              </div>
            )}
          </div>

          <div className={styles.acoesCabecalho}>
            <Link to="/perfil" className={styles.perfil} title={usuario.email}>
              <span className={styles.nomePerfil}>{usuario.nome}</span>
              <span className={styles.avatar} aria-hidden="true">{iniciais(usuario.nome)}</span>
            </Link>
          </div>
        </header>

        <Outlet context={{ usuario, setUsuario } satisfies ContextoLayout} />
      </main>
    </div>
  )
}
