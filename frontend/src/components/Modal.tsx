import { useId, type ReactNode } from 'react'
import styles from './Modal.module.css'

type ModalProps = {
  aberto: boolean
  titulo: string
  aoFechar: () => void
  children: ReactNode
}

export default function Modal({
  aberto,
  titulo,
  aoFechar,
  children,
}: ModalProps) {
  const tituloId = useId()

  if (!aberto) {
    return null
  }

  function aoClicarNoFundo(evento: React.MouseEvent<HTMLDivElement>) {
    if (evento.target === evento.currentTarget) {
      aoFechar()
    }
  }

  return (
    <div className={styles.fundo} onClick={aoClicarNoFundo}>
      <section
        className={styles.janela}
        role="dialog"
        aria-modal="true"
        aria-labelledby={tituloId}
      >
        <header className={styles.cabecalho}>
          <h2 id={tituloId}>{titulo}</h2>

          <button
            type="button"
            className={styles.botaoFechar}
            onClick={aoFechar}
            aria-label="Fechar modal"
          >
            ×
          </button>
        </header>

        <div className={styles.conteudo}>{children}</div>
      </section>
    </div>
  )
}