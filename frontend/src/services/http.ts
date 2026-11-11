type ApiErrorResponse = {
  message?: string
}

function tokenCsrf() {
  return document.cookie
    .split('; ')
    .find((cookie) => cookie.startsWith('XSRF-TOKEN='))
    ?.split('=')[1]
}

export async function requisitar<T>(url: string, opcoes?: RequestInit): Promise<T> {
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

  if (!resposta.ok) {
    const erro = (await resposta.json().catch(() => null)) as ApiErrorResponse | null
    throw new Error(erro?.message ?? 'Não foi possível concluir a operação.')
  }

  if (resposta.status === 204) {
    return undefined as T
  }

  return resposta.json() as Promise<T>
}
