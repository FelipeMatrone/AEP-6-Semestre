import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link } from 'react-router-dom'
import { CampoTexto } from '../../components/CampoTexto'
import {
  autenticar,
  CredenciaisInvalidasError,
  type Usuario,
} from '../../services/auth'
import {
  definirEmailLembrado,
  recuperarEmailLembrado,
} from '../../services/preferencias'
import { IconeCapelo, IconeGoogle, IconeOlho, IconeOlhoCortado } from './icones'
import styles from './Login.module.css'

// Identidade provisória: trocar quando o nome do sistema e o ODS forem definidos.
const NOME_DO_SISTEMA = 'Sistema AEP'

// O Spring Security expõe esta rota ao habilitar o oauth2-client. A troca do
// código por token acontece no servidor — o frontend só leva o usuário até lá.
const URL_OAUTH_GOOGLE = `${import.meta.env.VITE_API_URL}/oauth2/authorization/google`

const esquemaLogin = z.object({
  email: z
    .string()
    .trim()
    .min(1, 'Informe seu e-mail')
    .pipe(z.email('Informe um e-mail válido')),
  senha: z.string().min(1, 'Informe sua senha'),
  lembrarMe: z.boolean(),
})

type DadosLogin = z.infer<typeof esquemaLogin>

// Único estado para o resultado do envio: evita que uma mensagem de sucesso
// antiga fique visível junto com um erro de uma tentativa seguinte.
type StatusEnvio =
  | { tipo: 'ocioso' }
  | { tipo: 'erro'; mensagem: string }
  | { tipo: 'sucesso'; usuario: Usuario }

export default function Login() {
  const emailLembrado = recuperarEmailLembrado()
  const [senhaVisivel, setSenhaVisivel] = useState(false)
  const [status, setStatus] = useState<StatusEnvio>({ tipo: 'ocioso' })

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<DadosLogin>({
    resolver: zodResolver(esquemaLogin),
    // Valida ao sair do campo (e não a cada tecla): avisa cedo sem hostilizar
    // quem ainda está digitando.
    mode: 'onTouched',
    defaultValues: {
      email: emailLembrado,
      senha: '',
      lembrarMe: emailLembrado !== '',
    },
  })

  async function aoEnviar(dados: DadosLogin) {
    try {
      const usuario = await autenticar(dados.email, dados.senha)
      definirEmailLembrado(dados.lembrarMe ? dados.email : null)
      setStatus({ tipo: 'sucesso', usuario })
    } catch (erro) {
      setStatus({
        tipo: 'erro',
        mensagem:
          erro instanceof CredenciaisInvalidasError
            ? erro.message
            : 'Não foi possível entrar agora. Tente novamente em instantes.',
      })
    }
  }

  return (
    <div className={styles.pagina}>
      <aside className={styles.painel}>
        <div className={styles.marca}>
          <span className={styles.logo}>
            <IconeCapelo />
          </span>
          {NOME_DO_SISTEMA}
        </div>

        <div className={styles.chamada}>
          <h1 className={styles.tituloPainel}>
            Acompanhe tudo em um só lugar
          </h1>
          <p className={styles.textoPainel}>
            Entre para ver seus dados atualizados e continuar de onde parou.
          </p>
        </div>

        <div className={styles.decoracao} aria-hidden="true" />
      </aside>

      <main className={styles.conteudo}>
        <div className={styles.cartao}>
          <header className={styles.cabecalho}>
            <h2 className={styles.titulo}>Bem-vindo de volta</h2>
            <p className={styles.subtitulo}>
              Entre com sua conta para continuar.
            </p>
          </header>

          <form
            className={styles.formulario}
            onSubmit={handleSubmit(aoEnviar)}
            noValidate
          >
            <CampoTexto
              rotulo="E-mail"
              type="email"
              placeholder="voce@exemplo.com"
              autoComplete="email"
              erro={errors.email?.message}
              {...register('email')}
            />

            <CampoTexto
              rotulo="Senha"
              type={senhaVisivel ? 'text' : 'password'}
              placeholder="Sua senha"
              autoComplete="current-password"
              erro={errors.senha?.message}
              acao={
                <button
                  type="button"
                  className={styles.botaoOlho}
                  onClick={() => setSenhaVisivel((visivel) => !visivel)}
                  aria-label={senhaVisivel ? 'Ocultar senha' : 'Mostrar senha'}
                  aria-pressed={senhaVisivel}
                >
                  {senhaVisivel ? <IconeOlhoCortado /> : <IconeOlho />}
                </button>
              }
              {...register('senha')}
            />

            <label className={styles.lembreMe}>
              <input type="checkbox" {...register('lembrarMe')} />
              Lembre-me
            </label>

            {status.tipo === 'erro' && (
              <p role="alert" className={styles.alerta}>
                {status.mensagem}
              </p>
            )}

            {status.tipo === 'sucesso' && (
              <p role="status" className={styles.sucesso}>
                Tudo certo, {status.usuario.nome}! A tela inicial ainda não
                existe — veja o TODO.md.
              </p>
            )}

            <button
              type="submit"
              className={styles.botaoEntrar}
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Entrando…' : 'Entrar'}
            </button>
          </form>

          <div className={styles.divisor}>
            <span>ou continue com</span>
          </div>

          {/* Âncora, não botão: OAuth exige navegação de topo real. */}
          <a className={styles.botaoGoogle} href={URL_OAUTH_GOOGLE}>
            <IconeGoogle />
            Entrar com Google
          </a>

          <p className={styles.rodape}>
            Não tem uma conta? <Link to="/cadastro">Cadastre-se</Link>
          </p>
        </div>
      </main>
    </div>
  )
}
