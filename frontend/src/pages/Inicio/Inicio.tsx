import { Link } from 'react-router-dom'
import { IconeCapelo } from '../Login/icones'
import styles from './Inicio.module.css'

export default function Inicio() {
  return (
    <div className={styles.pagina}>
      <aside className={styles.menuLateral}>
        <div className={styles.marca}>
          <span className={styles.logo}>
            <IconeCapelo />
          </span>
          Sistema AEP
        </div>

        <nav className={styles.navegacao} aria-label="Navegação principal">
          <button type="button" className={styles.itemAtivo}>
            Dashboard
          </button>
          <button type="button">Anotações</button>
          <button type="button">Tarefas</button>
        </nav>

        <Link to="/login" className={styles.sair}>
          Sair
        </Link>
      </aside>

      <main className={styles.conteudo}>
        <header className={styles.cabecalho}>
          <label className={styles.busca}>
            <span className={styles.textoOculto}>Buscar</span>
            <input
              type="search"
              placeholder="Busque por aulas, tarefas ou anotações"
            />
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

        <section className={styles.boasVindas}>
          <p className={styles.saudacao}>Olá, estudante!</p>
          <h1>Seu painel acadêmico</h1>
          <p>
            Acompanhe seus compromissos, desempenho e atividades em um só
            lugar.
          </p>
        </section>

        <section className={styles.areaCards} aria-label="Resumo do painel">
          <article>Resumo de desempenho</article>
          <article>Aulas de hoje</article>
          <article>Próximos prazos</article>
          <article>Lista de tarefas</article>
        </section>
      </main>
    </div>
  )
}