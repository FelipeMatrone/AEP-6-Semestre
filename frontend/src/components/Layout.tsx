import { useState } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import { IconeCapelo } from './icones'
import { NOME_DO_SISTEMA } from '../config'
import styles from './Layout.module.css'

function classeItem({ isActive }: { isActive: boolean }) {
  return isActive ? styles.itemAtivo : ''
}

export default function Layout() {
  const [menuRecolhido, setMenuRecolhido] = useState(false)

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

          <button type="button" aria-label="Tarefas" title="Tarefas">
            <span className={styles.iconeMenu}>✓</span>
            <span className={styles.textoMenu}>Tarefas</span>
          </button>
        </nav>

        <Link to="/login" className={styles.sair} title="Sair">
          <span className={styles.iconeMenu}>↪</span>
          <span className={styles.textoMenu}>Sair</span>
        </Link>
      </aside>

      <main className={styles.conteudo}>
        <header className={styles.cabecalho}>
          <label className={styles.busca}>
            <span className={styles.textoOculto}>Buscar</span>
            <input type="search" placeholder="Busque por tarefas ou anotações" />
          </label>

          <div className={styles.acoesCabecalho}>
            <button
              type="button"
              className={styles.botaoNotificacao}
              aria-label="Ver notificações"
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M18 9a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
                <path d="M10 21h4" />
              </svg>
              <span className={styles.indicadorNotificacao}>3</span>
            </button>

            <button
              type="button"
              className={styles.perfil}
              aria-label="Abrir perfil de Aluno AEP"
            >
              <span className={styles.nomePerfil}>Aluno AEP</span>
              <span className={styles.avatar}>AA</span>
            </button>
          </div>
        </header>

        <Outlet />
      </main>
    </div>
  )
}
