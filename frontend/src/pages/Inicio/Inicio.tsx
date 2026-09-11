import { useState } from 'react'
import { Link } from 'react-router-dom'
import styles from './Inicio.module.css'

type Tarefa = {
  id: number
  titulo: string
  prazo: string
  prioridade: 'Alta' | 'Média' | 'Baixa'
  concluida: boolean
  paraHoje: boolean
}

const anotacoes = [
  {
    id: 1,
    titulo: 'Requisitos funcionais',
    data: 'Criada hoje',
    descricao: 'Anotações sobre o levantamento inicial do projeto.',
  },
  {
    id: 2,
    titulo: 'Banco de Dados NoSQL',
    data: 'Criada ontem',
    descricao: 'Conceitos principais para revisar antes da atividade.',
  },
  {
    id: 3,
    titulo: 'Ideias para a AEP',
    data: 'Criada em 20 ago.',
    descricao: 'Possíveis temas e funcionalidades para a PoC.',
  },
]

const tarefasIniciais: Tarefa[] = [
  {
    id: 1,
    titulo: 'Finalizar layout do dashboard',
    prazo: 'Hoje',
    prioridade: 'Alta',
    concluida: false,
    paraHoje: true,
  },
  {
    id: 2,
    titulo: 'Revisar conceitos de NoSQL',
    prazo: 'Hoje',
    prioridade: 'Média',
    concluida: false,
    paraHoje: true,
  },
  {
    id: 3,
    titulo: 'Criar anotações da aula',
    prazo: 'Hoje',
    prioridade: 'Baixa',
    concluida: true,
    paraHoje: true,
  },
  {
    id: 4,
    titulo: 'Entregar atividade de requisitos',
    prazo: 'Amanhã',
    prioridade: 'Alta',
    concluida: false,
    paraHoje: false,
  },
  {
    id: 5,
    titulo: 'Organizar tarefas da semana',
    prazo: 'Em 3 dias',
    prioridade: 'Média',
    concluida: false,
    paraHoje: false,
  },
]

export default function Inicio() {
  const [tarefas, setTarefas] = useState(tarefasIniciais)

  const tarefasDeHoje = tarefas.filter((tarefa) => tarefa.paraHoje)

  const concluidasHoje = tarefasDeHoje.filter(
    (tarefa) => tarefa.concluida,
  ).length

  const pendentesHoje = tarefasDeHoje.length - concluidasHoje

  const proximasTarefas = tarefas
    .filter((tarefa) => !tarefa.concluida)
    .filter((tarefa) => !tarefa.paraHoje)
    .slice(0, 4)

  function alternarTarefa(id: number) {
    setTarefas((tarefasAtuais) =>
      tarefasAtuais.map((tarefa) =>
        tarefa.id === id
          ? { ...tarefa, concluida: !tarefa.concluida }
          : tarefa,
      ),
    )
  }

  return (
    <>
      <section className={styles.boasVindas}>
        <p className={styles.saudacao}>Olá, estudante!</p>
        <h1>Seu painel acadêmico</h1>
        <p>Acompanhe suas tarefas e anotações em um só lugar.</p>
      </section>

      <section className={styles.painelPrincipal} aria-label="Resumo do painel">
        <div className={styles.colunaEsquerda}>
          <article className={styles.cartao}>
            <div className={styles.tituloCartao}>
              <h2>Próximas tarefas</h2>
              <Link to="/tarefas">Ver todas</Link>
            </div>

            <ul className={styles.lista}>
              {proximasTarefas.map((tarefa) => (
                <li key={tarefa.id} className={styles.itemLista}>
                  <span
                    className={`${styles.pontoPrioridade} ${
                      styles[`prioridade${tarefa.prioridade}`]
                    }`}
                  />
                  <div>
                    <strong>{tarefa.titulo}</strong>
                    <span>Prazo: {tarefa.prazo}</span>
                  </div>
                </li>
              ))}
            </ul>
          </article>

          <article className={styles.cartao}>
            <div className={styles.tituloCartao}>
              <h2>Últimas anotações criadas</h2>
              <button type="button">Ver todas</button>
            </div>

            <ul className={styles.lista}>
              {anotacoes.map((anotacao) => (
                <li key={anotacao.id} className={styles.itemAnotacao}>
                  <div className={styles.iconeAnotacao}>✎</div>
                  <div>
                    <strong>{anotacao.titulo}</strong>
                    <p>{anotacao.descricao}</p>
                    <span>{anotacao.data}</span>
                  </div>
                </li>
              ))}
            </ul>
          </article>
        </div>

        <div className={styles.colunaDireita}>
          <article className={`${styles.cartao} ${styles.resumo}`}>
            <h2>Resumo de hoje</h2>

            <div className={styles.metricas}>
              <div>
                <span>Concluídas</span>
                <strong>{concluidasHoje}</strong>
              </div>

              <div>
                <span>Pendentes</span>
                <strong>{pendentesHoje}</strong>
              </div>
            </div>
          </article>

          <article className={`${styles.cartao} ${styles.tarefasHoje}`}>
            <div className={styles.tituloCartao}>
              <h2>Tarefas de hoje</h2>
              <span>{tarefasDeHoje.length} tarefas</span>
            </div>

            <ul className={styles.listaHoje}>
              {tarefasDeHoje.map((tarefa) => (
                <li key={tarefa.id}>
                  <label>
                    <input
                      type="checkbox"
                      checked={tarefa.concluida}
                      onChange={() => alternarTarefa(tarefa.id)}
                    />
                    <span className={tarefa.concluida ? styles.concluida : ''}>
                      {tarefa.titulo}
                    </span>
                  </label>
                  <span
                    className={`${styles.etiqueta} ${
                      styles[`prioridade${tarefa.prioridade}`]
                    }`}
                  >
                    {tarefa.prioridade}
                  </span>
                </li>
              ))}
            </ul>
          </article>
        </div>
      </section>

    </>
  )
}
