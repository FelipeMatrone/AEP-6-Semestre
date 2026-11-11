import { useEffect, useState } from 'react'
import { listarCategorias, type CategoriaResumo } from '../../services/categorias'
import { listarTarefas, type TarefaResumo } from '../../services/tarefas'
import styles from './Calendario.module.css'

export default function Calendario() {
  const [tarefas, setTarefas] = useState<TarefaResumo[]>([])
  const [categorias, setCategorias] = useState<CategoriaResumo[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [tentativa, setTentativa] = useState(0)

  useEffect(() => {
    let telaAtiva = true

    async function carregarCalendario() {
      setCarregando(true)
      setErro(null)

      try {
        const [tarefasDaApi, categoriasDaApi] = await Promise.all([
          listarTarefas(),
          listarCategorias(),
        ])

        if (telaAtiva) {
          setTarefas(tarefasDaApi)
          setCategorias(categoriasDaApi)
        }
      } catch {
        if (telaAtiva) {
          setErro('Não foi possível carregar o calendário. Tente novamente.')
        }
      } finally {
        if (telaAtiva) {
          setCarregando(false)
        }
      }
    }

    carregarCalendario()

    return () => {
      telaAtiva = false
    }
  }, [tentativa])

  return (
    <section className={styles.pagina}>
      <header className={styles.topo}>
        <div>
          <p className={styles.secao}>Planejamento acadêmico</p>
          <h1>Calendário</h1>
          <p>Visualize seus prazos e organize suas atividades por dia.</p>
        </div>
      </header>

      {carregando && <p className={styles.estado}>Carregando calendário...</p>}

      {erro && (
        <section className={styles.vazio}>
          <h2>Não foi possível carregar o calendário</h2>
          <p>{erro}</p>
          <button type="button" onClick={() => setTentativa((atual) => atual + 1)}>
            Tentar novamente
          </button>
        </section>
      )}

      {!carregando && !erro && tarefas.length === 0 && (
        <section className={styles.vazio}>
          <div className={styles.iconeVazio} aria-hidden="true">▦</div>
          <h2>Nenhuma tarefa cadastrada</h2>
          <p>Crie uma tarefa para começar a acompanhar seus prazos no calendário.</p>
        </section>
      )}

      {!carregando && !erro && tarefas.length > 0 && (
        <section className={styles.cartaoInicial} aria-label="Dados carregados para o calendário">
          <h2>Calendário mensal</h2>
          <p>{tarefas.length} tarefas e {categorias.length} disciplinas carregadas.</p>
        </section>
      )}
    </section>
  )
}
