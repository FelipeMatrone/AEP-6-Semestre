import { ErroApi, prepararCsrf, requisitar } from './http'

export type Usuario = {
  nome: string
  email: string
}

/**
 * Erro de credenciais inválidas.
 *
 * Existe como classe própria para a tela distinguir "e-mail ou senha errados"
 * (culpa do usuário, mensagem específica) de qualquer outra falha — rede fora,
 * backend caído — que merece uma mensagem genérica.
 */
export class CredenciaisInvalidasError extends Error {
  constructor() {
    super('E-mail ou senha incorretos.')
    this.name = 'CredenciaisInvalidasError'
  }
}

export async function autenticar(
  email: string,
  senha: string,
): Promise<Usuario> {
  try {
    const usuario = await requisitar<Usuario>('/api/sessao', {
      method: 'POST',
      body: JSON.stringify({ email: email.trim(), senha }),
    })

    // O login troca a sessão no Spring Security. Busca um token novo para que
    // as próximas operações de escrita usem o cookie da sessão autenticada.
    await prepararCsrf(true)
    return usuario
  } catch (erro) {
    if (erro instanceof ErroApi && erro.status === 401) {
      throw new CredenciaisInvalidasError()
    }

    throw erro
  }
}
