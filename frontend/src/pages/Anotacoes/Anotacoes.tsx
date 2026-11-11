import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import Modal from '../../components/Modal'
import { CampoTexto } from '../../components/CampoTexto'
import {
  criarCategoria as criarCategoriaNaApi,
  listarCategorias,
  type CategoriaResumo,
  type Cor,
} from '../../services/categorias'
import styles from './Anotacoes.module.css'

const esquemaCategoria = z.object({
  nome: z.string().trim().min(1, 'Informe o nome da categoria'),
})

type DadosCategoria = z.infer<typeof esquemaCategoria>

const cores: Cor[] = ['roxo', 'magenta', 'verde', 'ambar']

function rotuloAnotacoes(quantidade: number) {
  if (quantidade === 0) {
    return 'Nenhuma anotação'
  }

  return quantidade === 1 ? '1 anotação' : `${quantidade} anotações`
}

export default function Anotacoes() {
  const [categorias, setCategorias] = useState<CategoriaResumo[]>([])
  const [busca, setBusca] = useState('')
  const [modalAberto, setModalAberto] = useState(false)
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
    defaultValues: { nome: '' },
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
        const resposta = await listarCategorias()
        if (telaAtiva) {
          setCategorias(resposta)
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

  function abrirModal() {
    setErroFormulario(null)
    setModalAberto(true)
  }

  function fecharModal() {
    setErroFormulario(null)
    setModalAberto(false)
    reset()
  }

  async function criarCategoria({ nome }: DadosCategoria) {
    setErroFormulario(null)

    try {
      const categoria = await criarCategoriaNaApi({
        nome,
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
        },
      ])
      fecharModal()
    } catch (erro) {
      setErroFormulario(
        erro instanceof Error ? erro.message : 'Não foi possível criar a categoria.',
      )
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
              onClick={abrirModal}
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
            onClick={abrirModal}
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
            <li key={categoria.id}>
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

                <span className={styles.quantidade}>
                  {rotuloAnotacoes(categoria.totalAnotacoes)}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}

      <Modal aberto={modalAberto} titulo="Nova categoria" aoFechar={fecharModal}>
        <form
          className={styles.formularioCategoria}
          onSubmit={handleSubmit(criarCategoria)}
          noValidate
        >
          <CampoTexto
            rotulo="Nome da categoria"
            placeholder="Ex: Engenharia de Software"
            autoFocus
            erro={errors.nome?.message}
            {...register('nome')}
          />

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
              Criar
            </button>
          </div>
        </form>
      </Modal>
    </>
  )
}
