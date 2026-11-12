import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { CampoTexto } from '../../components/CampoTexto'
import { autenticar, CredenciaisInvalidasError } from '../../services/auth'
import {
  definirEmailLembrado,
  recuperarEmailLembrado,
} from '../../services/preferencias'
import {
  IconeCapelo,
  IconeOlho,
  IconeOlhoCortado,
} from '../../components/icones'
import { NOME_DO_SISTEMA } from '../../config'
import styles from './Login.module.css'

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

type StatusEnvio =
  | { tipo: 'ocioso' }
  | { tipo: 'erro'; mensagem: string }

export default function Login() {
  const navigate = useNavigate()
  // Vindo do cadastro, o e-mail recém-criado tem prioridade sobre o lembrado.
  const emailCadastrado: string | undefined = useLocation().state?.emailCadastrado
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
      email: emailCadastrado ?? emailLembrado,
      senha: '',
      lembrarMe: emailLembrado !== '',
    },
  })

  async function aoEnviar(dados: DadosLogin) {
    // Limpa o alerta da tentativa anterior: sem isto, um erro de credenciais
    // continuaria na tela contradizendo o erro de campo do envio seguinte.
    setStatus({ tipo: 'ocioso' })

    try {
      await autenticar(dados.email, dados.senha)
      definirEmailLembrado(dados.lembrarMe ? dados.email : null)
      navigate('/inicio')
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
          <header>
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

            {emailCadastrado && status.tipo === 'ocioso' && (
              <p role="status" className={styles.sucesso}>
                Cadastro realizado! Entre com sua senha.
              </p>
            )}

            {status.tipo === 'erro' && (
              <p role="alert" className={styles.alerta}>
                {status.mensagem}
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

          <p className={styles.rodape}>
            Não tem uma conta? <Link to="/cadastro">Cadastre-se</Link>
          </p>
        </div>
      </main>
    </div>
  )
}