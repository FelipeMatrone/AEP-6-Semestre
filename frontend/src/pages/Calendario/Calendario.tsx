import { useEffect, useState } from 'react'
import { listarCategorias, type CategoriaResumo } from '../../services/categorias'
import { listarTarefas, type TarefaResumo } from '../../services/tarefas'
import styles from './Calendario.module.css'

const nomesMeses = [
  'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
  'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
]

const diasDaSemana = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom']

function primeiroDiaDoMes(data: Date) {
  return new Date(data.getFullYear(), data.getMonth(), 1)
}

function diasDoCalendario(mes: Date) {
  const primeiroDia = primeiroDiaDoMes(mes)
  const deslocamento = (primeiroDia.getDay() + 6) % 7

  return Array.from({ length: 42 }, (_, indice) => {
    const data = new Date(primeiroDia)
    data.setDate(primeiroDia.getDate() - deslocamento + indice)
    return data
  })
}

export default function Calendario() {
  const [tarefas, setTarefas] = useState<TarefaResumo[]>([])
  const [categorias, setCategorias] = useState<CategoriaResumo[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [tentativa, setTentativa] = useState(0)
  const [mesExibido, setMesExibido] = useState(() => primeiroDiaDoMes(new Date()))

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

  const dias = diasDoCalendario(mesExibido)

  function alterarMes(deslocamento: number) {
    setMesExibido((mesAtual) =>
      new Date(mesAtual.getFullYear(), mesAtual.getMonth() + deslocamento, 1),
    )
  }

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
        <section className={styles.calendario} aria-label="Calendário mensal">
          <header className={styles.cabecalhoCalendario}>
            <div>
              <h2>{nomesMeses[mesExibido.getMonth()]} de {mesExibido.getFullYear()}</h2>
              <p>{tarefas.length} tarefas e {categorias.length} disciplinas carregadas.</p>
            </div>
            <div className={styles.navegacaoMes}>
              <button type="button" onClick={() => alterarMes(-1)} aria-label="Mês anterior">‹</button>
              <button type="button" onClick={() => alterarMes(1)} aria-label="Próximo mês">›</button>
            </div>
          </header>

          <div className={styles.gradeCalendario}>
            {diasDaSemana.map((dia) => (
              <span key={dia} className={styles.nomeDia}>{dia}</span>
            ))}
            {dias.map((dia) => (
              <div
                key={dia.toISOString()}
                className={`${styles.dia} ${
                  dia.getMonth() === mesExibido.getMonth() ? '' : styles.diaForaDoMes
                }`}
                aria-label={new Intl.DateTimeFormat('pt-BR', { dateStyle: 'full' }).format(dia)}
              >
                <span>{dia.getDate()}</span>
              </div>
            ))}
          </div>
        </section>
      )}
    </section>
  )
}
