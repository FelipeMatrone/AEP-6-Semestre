import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link } from 'react-router-dom'
import { CampoTexto } from '../../components/CampoTexto'
import { IconeCapelo, IconeOlho, IconeOlhoCortado } from '../Login/icones'
import loginStyles from '../Login/Login.module.css'
import styles from './Cadastro.module.css'

const NOME_DO_SISTEMA = 'Sistema AEP'

const esquemaCadastro = z
  .object({
    nome: z.string().trim().min(1, 'Informe seu nome'),
    email: z
      .string()
      .trim()
      .min(1, 'Informe seu e-mail')
      .pipe(z.email('Informe um e-mail válido')),
    senha: z.string().min(1, 'Informe uma senha'),
    confirmarSenha: z.string().min(1, 'Confirme sua senha'),
  })
  .refine((dados) => dados.senha === dados.confirmarSenha, {
    message: 'As senhas não coincidem',
    path: ['confirmarSenha'],
  })

type DadosCadastro = z.infer<typeof esquemaCadastro>

export default function Cadastro() {
  const [senhaVisivel, setSenhaVisivel] = useState(false)
  const [confirmacaoVisivel, setConfirmacaoVisivel] = useState(false)
  const [cadastrado, setCadastrado] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<DadosCadastro>({
    resolver: zodResolver(esquemaCadastro),
    mode: 'onTouched',
    defaultValues: { nome: '', email: '', senha: '', confirmarSenha: '' },
  })

  // ponytail: sem backend de cadastro ainda — só valida e confirma na tela.
  // Quando o endpoint existir, chama ele aqui (como autenticar() em services/auth.ts).
  async function aoSalvar() {
    setCadastrado(true)
  }

  return (
    <div className={loginStyles.pagina}>
      <aside className={loginStyles.painel}>
        <div className={loginStyles.marca}>
          <span className={loginStyles.logo}>
            <IconeCapelo />
          </span>
          {NOME_DO_SISTEMA}
        </div>

        <div className={loginStyles.chamada}>
          <h1 className={loginStyles.tituloPainel}>Comece sua jornada</h1>
          <p className={loginStyles.textoPainel}>
            Crie sua conta para acompanhar tudo em um só lugar.
          </p>
        </div>

        <div className={loginStyles.decoracao} aria-hidden="true" />
      </aside>

      <main className={loginStyles.conteudo}>
        <div className={loginStyles.cartao}>
          <header className={loginStyles.cabecalho}>
            <h2 className={loginStyles.titulo}>Crie sua conta</h2>
            <p className={loginStyles.subtitulo}>
              Preencha os dados abaixo para começar.
            </p>
          </header>

          <form
            className={loginStyles.formulario}
            onSubmit={handleSubmit(aoSalvar)}
            noValidate
          >
            <CampoTexto
              rotulo="Nome"
              autoComplete="name"
              erro={errors.nome?.message}
              {...register('nome')}
            />

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
              autoComplete="new-password"
              erro={errors.senha?.message}
              acao={
                <button
                  type="button"
                  className={loginStyles.botaoOlho}
                  onClick={() => setSenhaVisivel((visivel) => !visivel)}
                  aria-label={senhaVisivel ? 'Ocultar senha' : 'Mostrar senha'}
                  aria-pressed={senhaVisivel}
                >
                  {senhaVisivel ? <IconeOlhoCortado /> : <IconeOlho />}
                </button>
              }
              {...register('senha')}
            />

            <CampoTexto
              rotulo="Confirmar senha"
              type={confirmacaoVisivel ? 'text' : 'password'}
              placeholder="Repita sua senha"
              autoComplete="new-password"
              erro={errors.confirmarSenha?.message}
              acao={
                <button
                  type="button"
                  className={loginStyles.botaoOlho}
                  onClick={() =>
                    setConfirmacaoVisivel((visivel) => !visivel)
                  }
                  aria-label={
                    confirmacaoVisivel ? 'Ocultar senha' : 'Mostrar senha'
                  }
                  aria-pressed={confirmacaoVisivel}
                >
                  {confirmacaoVisivel ? <IconeOlhoCortado /> : <IconeOlho />}
                </button>
              }
              {...register('confirmarSenha')}
            />

            {cadastrado && (
              <p role="status" className={loginStyles.sucesso}>
                Cadastro realizado! Você já pode entrar com sua conta.
              </p>
            )}

            <div className={styles.acoes}>
              <Link to="/login" className={styles.botaoCancelar}>
                Cancelar
              </Link>
              <button
                type="submit"
                className={loginStyles.botaoEntrar}
                disabled={isSubmitting}
              >
                Salvar
              </button>
            </div>
          </form>

          <p className={loginStyles.rodape}>
            Já tem uma conta? <Link to="/login">Entrar</Link>
          </p>
        </div>
      </main>
    </div>
  )
}
