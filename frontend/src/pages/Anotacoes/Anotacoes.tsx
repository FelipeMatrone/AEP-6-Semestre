import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import Modal from '../../components/Modal'
import { CampoTexto } from '../../components/CampoTexto'
import {
  atualizarCategoria as atualizarCategoriaNaApi,
  criarCategoria as criarCategoriaNaApi,
  excluirCategoria as excluirCategoriaNaApi,
  listarCategorias,
  type CategoriaResumo,
  type Cor,
} from '../../services/categorias'
import { listarTarefas, type TarefaResumo } from '../../services/tarefas'
import styles from './Anotacoes.module.css'

const esquemaCategoria = z.object({
  nome: z.string().trim().min(1, 'Informe o nome da categoria'),
  descricao: z.string().trim().optional(),
})

type DadosCategoria = z.infer<typeof esquemaCategoria>

const cores: Cor[] = ['roxo', 'magenta', 'verde', 'ambar']

type CategoriaComResumo = CategoriaResumo & {
  tarefasPendentes: number
  proximoPrazo: string | null
}

function rotuloAnotacoes(quantidade: number) {
  if (quantidade === 0) {
    return 'Nenhuma anotação'
  }

  return quantidade === 1 ? '1 anotação' : `${quantidade} anotações`
}

export default function Anotacoes() {
  const [categorias, setCategorias] = useState<CategoriaComResumo[]>([])
  const [busca, setBusca] = useState('')
  const [modalAberto, setModalAberto] = useState(false)
  const [categoriaEmEdicao, setCategoriaEmEdicao] = useState<CategoriaComResumo | null>(null)
  const [menuCategoriaAberto, setMenuCategoriaAberto] = useState<string | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erroLista, setErroLista] = useState<string | null>(null)
  const [erroFormulario, setErroFormulario] = useState<string | null>(null)
  const [tentativa, setTentativa] = useState(0)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<DadosCategoria>({
    resolver: zodResolver(esquemaCategoria),
    mode: 'onTouched',
    defaultValues: { nome: '', descricao: '' },
  })

  const termo = busca.trim().toLowerCase()

  const encontradas = termo
    ? categorias.filter((categoria) =>
        categoria.nome.toLowerCase().includes(termo),
      )
    : categorias

  useEffect(() => {
    let telaAtiva = true

    async function carregarCategorias() {
      setCarregando(true)
      setErroLista(null)

      try {
        const [resposta, tarefas] = await Promise.all([listarCategorias(), listarTarefas()])
        if (telaAtiva) {
          setCategorias(resposta.map((categoria) => resumirCategoria(categoria, tarefas)))
        }
      } catch {
        if (telaAtiva) {
          setErroLista('Não foi possível carregar as categorias. Tente novamente.')
        }
      } finally {
        if (telaAtiva) {
          setCarregando(false)
        }
      }
    }

    carregarCategorias()

    return () => {
      telaAtiva = false
    }
  }, [tentativa])

  function abrirModalCriacao() {
    setErroFormulario(null)
    setCategoriaEmEdicao(null)
    reset({ nome: '', descricao: '' })
    setModalAberto(true)
  }

  function abrirModalEdicao(categoria: CategoriaComResumo) {
    setErroFormulario(null)
    setCategoriaEmEdicao(categoria)
    setMenuCategoriaAberto(null)
    reset({ nome: categoria.nome, descricao: categoria.descricao ?? '' })
    setModalAberto(true)
  }

  function fecharModal() {
    setErroFormulario(null)
    setModalAberto(false)
    setCategoriaEmEdicao(null)
    reset({ nome: '', descricao: '' })
  }

  async function salvarCategoria({ nome, descricao }: DadosCategoria) {
    setErroFormulario(null)

    try {
      if (categoriaEmEdicao) {
        const categoriaAtualizada = await atualizarCategoriaNaApi(categoriaEmEdicao.id, {
          nome,
          descricao: descricao ?? '',
          cor: categoriaEmEdicao.cor,
        })
        setCategorias((atuais) => atuais.map((categoria) =>
          categoria.id === categoriaAtualizada.id
            ? {
                ...categoria,
                nome: categoriaAtualizada.nome,
                descricao: categoriaAtualizada.descricao,
                cor: categoriaAtualizada.cor,
                totalAnotacoes: categoriaAtualizada.anotacoes.length,
              }
            : categoria,
        ))
        fecharModal()
        return
      }

      const categoria = await criarCategoriaNaApi({
        nome,
        descricao: descricao || undefined,
        cor: cores[categorias.length % cores.length],
      })
      setCategorias((atuais) => [
        ...atuais,
        {
          id: categoria.id,
          nome: categoria.nome,
          descricao: categoria.descricao,
          cor: categoria.cor,
          totalAnotacoes: categoria.anotacoes.length,
          tarefasPendentes: 0,
          proximoPrazo: null,
        },
      ])
      fecharModal()
    } catch (erro) {
      setErroFormulario(
        erro instanceof Error ? erro.message : 'Não foi possível criar a categoria.',
      )
    }
  }

  async function excluirCategoria(categoria: CategoriaComResumo) {
    setMenuCategoriaAberto(null)
    if (!window.confirm(`Excluir a categoria “${categoria.nome}”?`)) return

    try {
      await excluirCategoriaNaApi(categoria.id)
      setCategorias((atuais) => atuais.filter((item) => item.id !== categoria.id))
    } catch (erro) {
      setErroLista(erro instanceof Error ? erro.message : 'Não foi possível excluir a categoria.')
    }
  }

  return (
    <>
      <section className={styles.topo}>
        <div>
          <p className={styles.secao}>Suas anotações</p>
          <h1>Categorias</h1>
          <p className={styles.descricao}>
            Organize suas anotações por disciplina, matéria ou assunto.
          </p>
        </div>

        {categorias.length > 0 && (
          <div className={styles.acoes}>
            <input
              type="search"
              className={styles.busca}
              value={busca}
              onChange={(evento) => setBusca(evento.target.value)}
              placeholder="Buscar categoria"
              aria-label="Buscar categoria"
            />

            <button
              type="button"
              className={styles.botaoNovaCategoria}
              onClick={abrirModalCriacao}
            >
              <span aria-hidden="true">+</span> Nova categoria
            </button>
          </div>
        )}
      </section>

      {carregando ? (
        <p className={styles.semResultado}>Carregando categorias...</p>
      ) : erroLista ? (
        <section className={styles.vazio}>
          <h2>Não foi possível carregar as categorias</h2>
          <p>{erroLista}</p>
          <button
            type="button"
            className={styles.botaoNovaCategoria}
            onClick={() => setTentativa((atual) => atual + 1)}
          >
            Tentar novamente
          </button>
        </section>
      ) : categorias.length === 0 ? (
        <section className={styles.vazio}>
          <div className={styles.iconeVazio} aria-hidden="true">
            ✎
          </div>
          <h2>Você ainda não tem categorias</h2>
          <p>
            Crie sua primeira categoria para começar a organizar suas anotações.
          </p>
          <button
            type="button"
            className={styles.botaoNovaCategoria}
            onClick={abrirModalCriacao}
          >
            <span aria-hidden="true">+</span> Nova categoria
          </button>
        </section>
      ) : encontradas.length === 0 ? (
        <p className={styles.semResultado}>
          Nenhuma categoria encontrada para “{busca.trim()}”.
        </p>
      ) : (
        <ul className={styles.grade}>
          {encontradas.map((categoria) => (
            <li key={categoria.id} className={styles.itemCategoria}>
              <Link
                to={`/anotacoes/${categoria.id}`}
                className={styles.cartaoCategoria}
              >
                <span
                  className={`${styles.icone} ${styles[categoria.cor]}`}
                  aria-hidden="true"
                >
                  {categoria.nome.charAt(0)}
                </span>

                <strong className={styles.nome}>{categoria.nome}</strong>

                {categoria.descricao && <span className={styles.descricaoCategoria}>{categoria.descricao}</span>}

                <span className={styles.quantidade}>
                  {rotuloAnotacoes(categoria.totalAnotacoes)}
                </span>
                <span className={styles.quantidade}>
                  {categoria.tarefasPendentes === 1
                    ? '1 tarefa pendente'
                    : `${categoria.tarefasPendentes} tarefas pendentes`}
                </span>
                {categoria.proximoPrazo && (
                  <span className={styles.proximoPrazo}>
                    Próximo prazo: {formatarPrazo(categoria.proximoPrazo)}
                  </span>
                )}
              </Link>

              <div className={styles.menuCategoria}>
                <button
                  type="button"
                  className={styles.botaoMenuCategoria}
                  aria-label={`Ações da categoria ${categoria.nome}`}
                  aria-expanded={menuCategoriaAberto === categoria.id}
                  aria-controls={`acoes-categoria-${categoria.id}`}
                  onClick={() => setMenuCategoriaAberto((aberto) =>
                    aberto === categoria.id ? null : categoria.id,
                  )}
                >
                  <span aria-hidden="true">⋯</span>
                </button>

                {menuCategoriaAberto === categoria.id && (
                  <div id={`acoes-categoria-${categoria.id}`} className={styles.opcoesCategoria}>
                    <button type="button" onClick={() => abrirModalEdicao(categoria)}>
                      Editar categoria
                    </button>
                    <button
                      type="button"
                      className={styles.opcaoExcluirCategoria}
                      onClick={() => excluirCategoria(categoria)}
                    >
                      Excluir categoria
                    </button>
                  </div>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}

      <Modal
        aberto={modalAberto}
        titulo={categoriaEmEdicao ? 'Editar categoria' : 'Nova categoria'}
        aoFechar={fecharModal}
      >
        <form
          className={styles.formularioCategoria}
          onSubmit={handleSubmit(salvarCategoria)}
          noValidate
        >
          <CampoTexto
            rotulo="Nome da categoria"
            placeholder="Ex: Engenharia de Software"
            autoFocus
            erro={errors.nome?.message}
            {...register('nome')}
          />

          {categoriaEmEdicao && (
            <label className={styles.campoDescricao}>
              Descrição
              <textarea
                placeholder="Descreva esta categoria"
                {...register('descricao')}
              />
            </label>
          )}

          {erroFormulario && <p className={styles.erroFormulario} role="alert">{erroFormulario}</p>}

          <div className={styles.acoesModal}>
            <button
              type="button"
              className={styles.botaoCancelar}
              onClick={fecharModal}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className={styles.botaoNovaCategoria}
              disabled={isSubmitting}
            >
              {categoriaEmEdicao ? 'Salvar' : 'Criar'}
            </button>
          </div>
        </form>
      </Modal>
    </>
  )
}

function resumirCategoria(categoria: CategoriaResumo, tarefas: TarefaResumo[]): CategoriaComResumo {
  const pendentes = tarefas
    .filter((tarefa) => tarefa.categoriaId === categoria.id && !tarefa.concluida)
    .sort((primeira, segunda) => primeira.prazo.localeCompare(segunda.prazo))

  return {
    ...categoria,
    tarefasPendentes: pendentes.length,
    proximoPrazo: pendentes[0]?.prazo ?? null,
  }
}

function formatarPrazo(prazo: string) {
  return new Intl.DateTimeFormat('pt-BR', { timeZone: 'UTC' }).format(
    new Date(`${prazo}T00:00:00Z`),
  )
}
