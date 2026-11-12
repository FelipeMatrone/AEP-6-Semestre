type ApiErrorResponse = {
  message?: string
  fieldErrors?: Record<string, string>
}

export class ErroApi extends Error {
  readonly status: number
  readonly errosDeCampo: Record<string, string>

  constructor(message: string, status: number, errosDeCampo: Record<string, string> = {}) {
    super(message)
    this.name = 'ErroApi'
    this.status = status
    this.errosDeCampo = errosDeCampo
  }
}

function tokenCsrf() {
  return document.cookie
    .split('; ')
    .find((cookie) => cookie.startsWith('XSRF-TOKEN='))
    ?.split('=')[1]
}

export async function prepararCsrf(forcar = false) {
  if (!forcar && tokenCsrf()) return

  // A rota protegida cria o cookie de CSRF mesmo respondendo 401 para quem
  // ainda não entrou. O corpo e o status não importam nesta preparação.
  await fetch('/api/sessao', { credentials: 'include' })
}

export async function requisitar<T>(url: string, opcoes?: RequestInit): Promise<T> {
  const metodo = opcoes?.method?.toUpperCase()
  if (metodo && metodo !== 'GET') {
    await prepararCsrf()
  }

  const cabecalhos = new Headers(opcoes?.headers)
  const token = tokenCsrf()

  cabecalhos.set('Content-Type', 'application/json')

  if (opcoes?.method && opcoes.method !== 'GET' && token) {
    cabecalhos.set('X-XSRF-TOKEN', decodeURIComponent(token))
  }

  const resposta = await fetch(url, {
    ...opcoes,
    credentials: 'include',
    headers: cabecalhos,
  })

  // Sessão expirada no meio do uso: qualquer rota protegida volta 401 e a tela
  // ficaria presa num erro genérico. /api/sessao fica de fora porque o login
  // trata o próprio 401 (senha errada) e a guarda do Layout trata o dela.
  if (resposta.status === 401 && url !== '/api/sessao') {
    window.location.assign('/login')
  }

  if (!resposta.ok) {
    const erro = (await resposta.json().catch(() => null)) as ApiErrorResponse | null
    throw new ErroApi(
      erro?.message ?? 'Não foi possível concluir a operação.',
      resposta.status,
      erro?.fieldErrors,
    )
  }

  if (resposta.status === 204) {
    return undefined as T
  }

  return resposta.json() as Promise<T>
}
