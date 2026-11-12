import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { CampoTexto } from '../../components/CampoTexto'
import {
  IconeCapelo,
  IconeOlho,
  IconeOlhoCortado,
} from '../../components/icones'
import { NOME_DO_SISTEMA } from '../../config'
import { ErroApi } from '../../services/http'
import { cadastrarUsuario } from '../../services/usuarios'
import loginStyles from '../Login/Login.module.css'
import styles from './Cadastro.module.css'

const esquemaCadastro = z
  .object({
    nome: z.string().trim().min(1, 'Informe seu nome'),
    email: z
      .string()
      .trim()
      .min(1, 'Informe seu e-mail')
      .pipe(z.email('Informe um e-mail válido')),
    senha: z
      .string()
      .min(1, 'Informe uma senha')
      .min(8, 'A senha deve ter ao menos 8 caracteres')
      .max(72, 'A senha deve ter no máximo 72 caracteres'),
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
  const navigate = useNavigate()
  const [erroCadastro, setErroCadastro] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<DadosCadastro>({
    resolver: zodResolver(esquemaCadastro),
    mode: 'onTouched',
    defaultValues: { nome: '', email: '', senha: '', confirmarSenha: '' },
  })

  async function aoSalvar(dados: DadosCadastro) {
    setErroCadastro(null)

    try {
      await cadastrarUsuario({
        nome: dados.nome.trim(),
        email: dados.email.trim(),
        senha: dados.senha,
      })
      navigate('/login', { replace: true, state: { emailCadastrado: dados.email.trim() } })
    } catch (erro) {
      if (erro instanceof ErroApi && erro.status === 409) {
        setError('email', { message: 'Este e-mail já está cadastrado' }, { shouldFocus: true })
        return
      }
      // 400 do Bean Validation: cada mensagem volta para o campo dela.
      const campos = erro instanceof ErroApi ? Object.entries(erro.errosDeCampo) : []
      const conhecidos = campos.filter(([campo]) => ['nome', 'email', 'senha'].includes(campo))
      if (conhecidos.length > 0) {
        for (const [campo, mensagem] of conhecidos) {
          setError(campo as 'nome' | 'email' | 'senha', { message: mensagem })
        }
        return
      }
      setErroCadastro(
        erro instanceof Error ? erro.message : 'Não foi possível concluir o cadastro.',
      )
    }
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
          <header>
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

            {erroCadastro && (
              <p role="alert" className={loginStyles.alerta}>{erroCadastro}</p>
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
