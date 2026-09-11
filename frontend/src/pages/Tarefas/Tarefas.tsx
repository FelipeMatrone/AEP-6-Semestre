import { useState } from 'react'
import Modal from '../../components/Modal'
import styles from './Tarefas.module.css'

const tarefas = [
  {
    id: 1,
    titulo: 'Finalizar atividade de requisitos',
    prazo: '17/04/2026',
    prioridade: 'Alta',
  },
  {
    id: 2,
    titulo: 'Estudar para prova de Java',
    prazo: '19/04/2026',
    prioridade: 'Média',
  },
  {
    id: 3,
    titulo: 'Organizar tarefas da semana',
    prazo: '22/04/2026',
    prioridade: 'Baixa',
  },
]

export default function Tarefas() {
  const [modalAberto, setModalAberto] = useState(false)

  return (
    <section className={styles.pagina}>
      <header className={styles.cabecalho}>
        <h1>Tarefas</h1>
        <button
          type="button"
          className={styles.botaoNovaTarefa}
          onClick={() => setModalAberto(true)}
        >
          + Nova tarefa
        </button>
      </header>

      <div className={styles.resumo} aria-label="Resumo das tarefas">
        <article>
          <strong>12</strong>
          <span>Tarefas</span>
        </article>
        <article>
          <strong>4</strong>
          <span>Pendentes</span>
        </article>
        <article>
          <strong>5</strong>
          <span>Atrasadas</span>
        </article>
        <article>
          <strong>124</strong>
          <span>Concluídas</span>
        </article>
      </div>

      <section className={styles.lista} aria-labelledby="titulo-lista">
        <header className={styles.cabecalhoLista}>
          <h2 id="titulo-lista">Tarefas</h2>
          <input type="search" placeholder="Buscar" aria-label="Buscar tarefas" />
        </header>

        <div className={styles.tabelaResponsiva}>
          <table>
            <thead>
              <tr>
                <th scope="col">Tarefa</th>
                <th scope="col">Prazo</th>
                <th scope="col">Prioridade</th>
              </tr>
            </thead>
            <tbody>
              {tarefas.map((tarefa) => (
                <tr key={tarefa.id}>
                  <td>{tarefa.titulo}</td>
                  <td>{tarefa.prazo}</td>
                  <td>
                    <span
                      className={`${styles.prioridade} ${
                        styles[`prioridade${tarefa.prioridade}`]
                      }`}
                    >
                      {tarefa.prioridade}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <Modal
        aberto={modalAberto}
        titulo="Nova tarefa"
        aoFechar={() => setModalAberto(false)}
      >
        <form
          className={styles.formularioNovaTarefa}
          onSubmit={(evento) => evento.preventDefault()}
        >
          <div className={styles.linhaFormulario}>
            <label className={styles.grupoCampo}>
              <span>Prioridade</span>
              <select defaultValue="media">
                <option value="alta">Alta</option>
                <option value="media">Média</option>
                <option value="baixa">Baixa</option>
              </select>
            </label>

            <label className={styles.grupoCampo}>
              <span>Prazo</span>
              <input type="date" />
            </label>
          </div>

          <label className={styles.grupoCampo}>
            <span>Título</span>
            <input type="text" placeholder="Informe o título da tarefa" />
          </label>

          <label className={styles.grupoCampo}>
            <span>Descrição</span>
            <textarea placeholder="Descreva a tarefa" rows={6} />
          </label>

          <div className={styles.acoesFormulario}>
            <button type="submit" className={styles.botaoSalvar}>
              Salvar
            </button>
          </div>
        </form>
      </Modal>
    </section>
  )
}
