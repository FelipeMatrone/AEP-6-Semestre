import { useEffect, useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { CampoTexto } from '../../components/CampoTexto'
import type { ContextoLayout } from '../../components/Layout'
import { atualizarPerfil, trocarSenha } from '../../services/auth'
import { listarCategorias } from '../../services/categorias'
import { ErroApi } from '../../services/http'
import { buscarPainel } from '../../services/painel'
import loginStyles from '../Login/Login.module.css'
import styles from './Perfil.module.css'

const esquemaDados = z.object({
  nome: z.string().trim().min(1, 'Informe seu nome').max(100, 'Use no máximo 100 caracteres'),
  metaSemanal: z
    .number({ error: 'Informe um número' })
    .int('Use um número inteiro')
    .min(1, 'A meta deve ser de 1 a 100 tarefas')
    .max(100, 'A meta deve ser de 1 a 100 tarefas'),
})

const esquemaSenha = z
  .object({
    senhaAtual: z.string().min(1, 'Informe a senha atual'),
    novaSenha: z
      .string()
      .min(8, 'A senha deve ter ao menos 8 caracteres')
      .max(72, 'A senha deve ter no máximo 72 caracteres'),
    confirmarSenha: z.string().min(1, 'Confirme a nova senha'),
  })
  .refine((dados) => dados.novaSenha === dados.confirmarSenha, {
    message: 'As senhas não coincidem',
    path: ['confirmarSenha'],
  })

type DadosPerfil = z.infer<typeof esquemaDados>
type DadosSenha = z.infer<typeof esquemaSenha>

type Estatisticas = {
  concluidas: number
  pendentes: number
  atrasadas: number
  concluidasNaSemana: number
  categorias: number
  anotacoes: number
}

type Aviso = { tipo: 'sucesso' | 'alerta'; texto: string } | null

const membroDesde = new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' })

async function buscarEstatisticas(): Promise<Estatisticas> {
  const [painel, categorias] = await Promise.all([buscarPainel(), listarCategorias()])
  return {
    concluidas: painel.concluidas,
    pendentes: painel.pendentes,
    atrasadas: painel.atrasadas,
    concluidasNaSemana: painel.concluidasNaSemana,
    categorias: categorias.length,
    anotacoes: categorias.reduce((total, categoria) => total + categoria.totalAnotacoes, 0),
  }
}

export default function Perfil() {
  const { usuario, setUsuario } = useOutletContext<ContextoLayout>()
  const [estatisticas, setEstatisticas] = useState<Estatisticas | null>(null)
  const [erroEstatisticas, setErroEstatisticas] = useState(false)
  const [avisoDados, setAvisoDados] = useState<Aviso>(null)
  const [avisoSenha, setAvisoSenha] = useState<Aviso>(null)

  useEffect(() => {
    buscarEstatisticas().then(setEstatisticas).catch(() => setErroEstatisticas(true))
  }, [])

  const formDados = useForm<DadosPerfil>({
    resolver: zodResolver(esquemaDados),
    mode: 'onTouched',
    defaultValues: { nome: usuario.nome, metaSemanal: usuario.metaSemanal },
  })

  const formSenha = useForm<DadosSenha>({
    resolver: zodResolver(esquemaSenha),
    mode: 'onTouched',
  })

  async function salvarDados(dados: DadosPerfil) {
    setAvisoDados(null)
    try {
      const atualizado = await atualizarPerfil(dados)
      setUsuario(atualizado)
      formDados.reset({ nome: atualizado.nome, metaSemanal: atualizado.metaSemanal })
      setAvisoDados({ tipo: 'sucesso', texto: 'Dados atualizados.' })
    } catch (erro) {
      if (erro instanceof ErroApi && erro.errosDeCampo) {
        for (const campo of ['nome', 'metaSemanal'] as const) {
          const mensagem = erro.errosDeCampo[campo]
          if (mensagem) formDados.setError(campo, { message: mensagem })
        }
        return
      }
      setAvisoDados({ tipo: 'alerta', texto: 'Não foi possível salvar. Tente novamente.' })
    }
  }

  async function salvarSenha(dados: DadosSenha) {
    setAvisoSenha(null)
    try {
      await trocarSenha({ senhaAtual: dados.senhaAtual, novaSenha: dados.novaSenha })
      formSenha.reset({ senhaAtual: '', novaSenha: '', confirmarSenha: '' })
      setAvisoSenha({ tipo: 'sucesso', texto: 'Senha alterada.' })
    } catch (erro) {
      if (erro instanceof ErroApi && erro.errosDeCampo) {
        for (const campo of ['senhaAtual', 'novaSenha'] as const) {
          const mensagem = erro.errosDeCampo[campo]
          if (mensagem) formSenha.setError(campo, { message: mensagem }, { shouldFocus: true })
        }
        return
      }
      setAvisoSenha({ tipo: 'alerta', texto: 'Não foi possível trocar a senha. Tente novamente.' })
    }
  }

  const itensEstatistica = estatisticas && [
    { rotulo: 'Tarefas concluídas', valor: estatisticas.concluidas },
    { rotulo: 'Tarefas pendentes', valor: estatisticas.pendentes },
    { rotulo: 'Atrasadas', valor: estatisticas.atrasadas },
    {
      rotulo: 'Meta da semana',
      valor: `${estatisticas.concluidasNaSemana}/${usuario.metaSemanal}`,
    },
    { rotulo: 'Categorias', valor: estatisticas.categorias },
    { rotulo: 'Anotações', valor: estatisticas.anotacoes },
  ]

  return (
    <div className={styles.perfil}>
      <section className={styles.cabecalho}>
        <span className={styles.avatar} aria-hidden="true">
          {usuario.nome.trim().charAt(0).toUpperCase()}
        </span>
        <div>
          <h1>{usuario.nome}</h1>
          <p>{usuario.email}</p>
          {usuario.criadoEm && (
            <p className={styles.membro}>
              Membro desde {membroDesde.format(new Date(usuario.criadoEm))}
            </p>
          )}
        </div>
      </section>

      <section className={styles.cartao} aria-labelledby="titulo-estatisticas">
        <h2 id="titulo-estatisticas">Suas estatísticas</h2>
        {itensEstatistica ? (
          <dl className={styles.estatisticas}>
            {itensEstatistica.map((item) => (
              <div key={item.rotulo}>
                <dt>{item.rotulo}</dt>
                <dd>{item.valor}</dd>
              </div>
            ))}
          </dl>
        ) : (
          <p className={styles.vazio}>
            {erroEstatisticas ? 'Não foi possível carregar as estatísticas.' : 'Carregando…'}
          </p>
        )}
      </section>

      <div className={styles.colunas}>
        <section className={styles.cartao} aria-labelledby="titulo-dados">
          <h2 id="titulo-dados">Dados pessoais</h2>
          <form
            className={loginStyles.formulario}
            onSubmit={formDados.handleSubmit(salvarDados)}
            noValidate
          >
            <CampoTexto
              rotulo="Nome"
              autoComplete="name"
              erro={formDados.formState.errors.nome?.message}
              {...formDados.register('nome')}
            />
            <CampoTexto
              rotulo="Meta semanal (tarefas concluídas)"
              type="number"
              min={1}
              max={100}
              erro={formDados.formState.errors.metaSemanal?.message}
              {...formDados.register('metaSemanal', { valueAsNumber: true })}
            />
            {avisoDados && (
              <p role="status" className={loginStyles[avisoDados.tipo]}>{avisoDados.texto}</p>
            )}
            <button
              type="submit"
              className={loginStyles.botaoEntrar}
              disabled={formDados.formState.isSubmitting}
            >
              Salvar dados
            </button>
          </form>
        </section>

        <section className={styles.cartao} aria-labelledby="titulo-senha">
          <h2 id="titulo-senha">Trocar senha</h2>
          <form
            className={loginStyles.formulario}
            onSubmit={formSenha.handleSubmit(salvarSenha)}
            noValidate
          >
            <CampoTexto
              rotulo="Senha atual"
              type="password"
              autoComplete="current-password"
              erro={formSenha.formState.errors.senhaAtual?.message}
              {...formSenha.register('senhaAtual')}
            />
            <CampoTexto
              rotulo="Nova senha"
              type="password"
              autoComplete="new-password"
              erro={formSenha.formState.errors.novaSenha?.message}
              {...formSenha.register('novaSenha')}
            />
            <CampoTexto
              rotulo="Confirmar nova senha"
              type="password"
              autoComplete="new-password"
              erro={formSenha.formState.errors.confirmarSenha?.message}
              {...formSenha.register('confirmarSenha')}
            />
            {avisoSenha && (
              <p role="status" className={loginStyles[avisoSenha.tipo]}>{avisoSenha.texto}</p>
            )}
            <button
              type="submit"
              className={loginStyles.botaoEntrar}
              disabled={formSenha.formState.isSubmitting}
            >
              Trocar senha
            </button>
          </form>
        </section>
      </div>
    </div>
  )
}
