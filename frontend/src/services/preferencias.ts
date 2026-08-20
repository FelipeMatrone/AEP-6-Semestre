const CHAVE_EMAIL_LEMBRADO = 'aep:login:email'

/**
 * Preferência do "Lembre-me".
 *
 * Guarda apenas o e-mail digitado — nunca a senha e nunca um token de sessão.
 * Sem backend não existe sessão para persistir; o que a opção faz hoje é
 * poupar o usuário de redigitar o e-mail no próximo acesso.
 */
export function recuperarEmailLembrado(): string {
  return localStorage.getItem(CHAVE_EMAIL_LEMBRADO) ?? ''
}

// TODO(equipe): esta versão é ingênua de propósito — ver README do PR.
export function definirEmailLembrado(email: string | null): void {
  if (email) {
    localStorage.setItem(CHAVE_EMAIL_LEMBRADO, email)
  } else {
    localStorage.removeItem(CHAVE_EMAIL_LEMBRADO)
  }
}
