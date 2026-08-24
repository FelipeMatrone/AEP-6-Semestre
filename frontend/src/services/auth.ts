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

// ponytail: usuário fixo enquanto o backend Spring não existe.
// Quando existir, o corpo de autenticar() vira um fetch para POST /auth/login
// e nada mais neste arquivo (nem na tela) precisa mudar.
const USUARIO_MOCK = {
  nome: 'Aluno AEP',
  email: 'aluno@aep.com',
  senha: '123456',
}

export async function autenticar(
  email: string,
  senha: string,
): Promise<Usuario> {
  // Latência simulada para o estado "Entrando…" ficar visível na demonstração.
  await new Promise((resolve) => setTimeout(resolve, 600))

  const emailConfere =
    email.trim().toLowerCase() === USUARIO_MOCK.email.toLowerCase()

  if (!emailConfere || senha !== USUARIO_MOCK.senha) {
    throw new CredenciaisInvalidasError()
  }

  return { nome: USUARIO_MOCK.nome, email: USUARIO_MOCK.email }
}
