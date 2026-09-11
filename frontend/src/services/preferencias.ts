const CHAVE_EMAIL_LEMBRADO = 'aep:login:email'

/**
 * Preferência do "Lembre-me".
 *
 * Guarda apenas o e-mail digitado — nunca a senha e nunca um token de sessão.
 * Sem backend não existe sessão para persistir; o que a opção faz hoje é
 * poupar o usuário de redigitar o e-mail no próximo acesso.
 *
 * Onde o navegador bloqueia o armazenamento (aba anônima, política corporativa,
 * iframe com cookies de terceiros barrados) o próprio acesso a localStorage
 * lança. Como a leitura acontece durante a renderização da tela de login, uma
 * exceção aqui derrubaria a tela inteira — por isso as duas funções falham em
 * silêncio: perder a conveniência é aceitável, perder o login não é.
 */
export function recuperarEmailLembrado(): string {
  try {
    return localStorage.getItem(CHAVE_EMAIL_LEMBRADO) ?? ''
  } catch {
    return ''
  }
}

// TODO(equipe): esta versão é ingênua de propósito — ver README do PR.
export function definirEmailLembrado(email: string | null): void {
  try {
    if (email) {
      localStorage.setItem(CHAVE_EMAIL_LEMBRADO, email)
    } else {
      localStorage.removeItem(CHAVE_EMAIL_LEMBRADO)
    }
  } catch {
    // Sem persistência da preferência; o login em si não é afetado.
  }
}
