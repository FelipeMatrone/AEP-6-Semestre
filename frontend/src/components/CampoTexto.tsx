import { useId, type ComponentPropsWithRef, type ReactNode } from 'react'
import styles from './CampoTexto.module.css'

type CampoTextoProps = ComponentPropsWithRef<'input'> & {
  rotulo: string
  erro?: string
  /** Elemento renderizado dentro da moldura, à direita (ex: o botão do olho). */
  acao?: ReactNode
}

/**
 * Campo de formulário com rótulo, mensagem de erro e ligações de acessibilidade
 * já resolvidas. Todas as demais props vão direto para o <input>, o que permite
 * espalhar o retorno de register() do react-hook-form (inclusive a ref).
 */
export function CampoTexto({
  rotulo,
  erro,
  acao,
  id,
  ...propsDoInput
}: CampoTextoProps) {
  const idGerado = useId()
  const idCampo = id ?? idGerado
  const idErro = `${idCampo}-erro`

  return (
    <div className={styles.campo}>
      <label className={styles.rotulo} htmlFor={idCampo}>
        {rotulo}
      </label>

      <div className={`${styles.moldura} ${erro ? styles.molduraInvalida : ''}`}>
        <input
          {...propsDoInput}
          id={idCampo}
          className={styles.entrada}
          aria-invalid={erro ? true : undefined}
          aria-describedby={erro ? idErro : undefined}
        />
        {acao}
      </div>

      {erro && (
        <p id={idErro} className={styles.erro}>
          {erro}
        </p>
      )}
    </div>
  )
}
