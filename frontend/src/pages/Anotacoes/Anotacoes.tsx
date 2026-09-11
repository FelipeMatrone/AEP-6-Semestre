import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import Modal from '../../components/Modal'
import { CampoTexto } from '../../components/CampoTexto'
import { cores, categoriasIniciais } from './dados'
import styles from './Anotacoes.module.css'

const esquemaCategoria = z.object({
  nome: z.string().trim().min(1, 'Informe o nome da categoria'),
})

type DadosCategoria = z.infer<typeof esquemaCategoria>

function rotuloAnotacoes(quantidade: number) {
  if (quantidade === 0) {
    return 'Nenhuma anotação'
  }

  return quantidade === 1 ? '1 anotação' : `${quantidade} anotações`
}

export default function Anotacoes() {
  const [categorias, setCategorias] = useState(categoriasIniciais)
  const [busca, setBusca] = useState('')
  const [modalAberto, setModalAberto] = useState(false)

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

  function abrirModal() {
    setModalAberto(true)
  }

  function fecharModal() {
    setModalAberto(false)
    reset()
  }

  async function criarCategoria({ nome }: DadosCategoria) {
    setCategorias((atuais) => [
      ...atuais,
      { id: Date.now(), nome, anotacoes: 0, cor: cores[atuais.length % cores.length] },
    ])
    fecharModal()
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

      {categorias.length === 0 ? (
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
                  {rotuloAnotacoes(categoria.anotacoes)}
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
