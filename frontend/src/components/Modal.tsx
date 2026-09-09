import { useEffect, useId, useRef, type ReactNode } from 'react'
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
  const janelaRef = useRef<HTMLDialogElement>(null)

  // showModal() — e não show() — é o que coloca o diálogo na "top layer" do
  // navegador. De lá vêm de graça o fechamento por Esc, o ::backdrop, o foco
  // preso dentro da janela e o resto da página inerte para leitores de tela.
  // O <dialog> fica sempre montado para que a ref exista; o que some quando
  // fechado é o conteúdo.
  useEffect(() => {
    const janela = janelaRef.current
    if (!janela) return

    // As guardas de `janela.open` evitam o InvalidStateError de chamar
    // showModal() duas vezes — o StrictMode roda este efeito em duplicata.
    if (aberto && !janela.open) {
      janela.showModal()
    } else if (!aberto && janela.open) {
      janela.close()
    }
  }, [aberto])

  // No <dialog>, um clique no backdrop tem o próprio diálogo como alvo. Como a
  // janela não tem padding e o conteúdo a preenche por inteiro, alvo == janela
  // só acontece quando o clique foi no fundo.
  function aoClicarNoFundo(evento: React.MouseEvent<HTMLDialogElement>) {
    if (evento.target === evento.currentTarget) {
      aoFechar()
    }
  }

  return (
    <dialog
      ref={janelaRef}
      className={styles.janela}
      aria-labelledby={tituloId}
      onClose={aoFechar}
      onClick={aoClicarNoFundo}
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

      <div className={styles.conteudo}>{aberto && children}</div>
    </dialog>
  )
}
