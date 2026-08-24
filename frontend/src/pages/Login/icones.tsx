/**
 * Ícones em SVG inline. Decorativos (aria-hidden) porque quem os contém — o
 * botão, o link — já carrega o rótulo acessível.
 */

export function IconeCapelo() {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
      <path
        fill="currentColor"
        d="M12 3 1 8.5 12 14l9-4.5V16h2V8.5L12 3Zm0 12.7-6.5-3.25v3.05C5.5 17.99 8.41 20 12 20s6.5-2.01 6.5-4.5v-3.05L12 15.7Z"
      />
    </svg>
  )
}

export function IconeOlho() {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
      <path
        fill="currentColor"
        d="M12 5c-5 0-9.27 3.11-11 7 1.73 3.89 6 7 11 7s9.27-3.11 11-7c-1.73-3.89-6-7-11-7Zm0 12a5 5 0 1 1 0-10 5 5 0 0 1 0 10Zm0-8a3 3 0 1 0 0 6 3 3 0 0 0 0-6Z"
      />
    </svg>
  )
}

export function IconeOlhoCortado() {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
      <path
        fill="currentColor"
        d="M12 7a5 5 0 0 1 5 5c0 .64-.13 1.25-.36 1.81l2.92 2.92A11.8 11.8 0 0 0 23 12c-1.73-3.89-6-7-11-7-1.27 0-2.49.2-3.64.57l2.16 2.16C11.08 7.26 11.53 7 12 7ZM2.71 3.16 1.29 4.58l2.48 2.48A11.8 11.8 0 0 0 1 12c1.73 3.89 6 7 11 7 1.52 0 2.97-.3 4.31-.82l3.11 3.11 1.41-1.41L2.71 3.16ZM12 17a5 5 0 0 1-5-5c0-.69.15-1.34.4-1.93l1.55 1.55A2.99 2.99 0 0 0 12 15c.14 0 .27-.01.4-.04l1.55 1.55c-.59.31-1.25.49-1.95.49Z"
      />
    </svg>
  )
}

export function IconeGoogle() {
  return (
    <svg viewBox="0 0 48 48" width="20" height="20" aria-hidden="true">
      <path
        fill="#4285F4"
        d="M45.12 24.5c0-1.56-.14-3.06-.4-4.5H24v8.51h11.84c-.51 2.75-2.06 5.08-4.39 6.64v5.52h7.11c4.16-3.83 6.56-9.47 6.56-16.17Z"
      />
      <path
        fill="#34A853"
        d="M24 46c5.94 0 10.92-1.97 14.56-5.33l-7.11-5.52c-1.97 1.32-4.49 2.1-7.45 2.1-5.73 0-10.58-3.87-12.31-9.07H4.34v5.7A21.99 21.99 0 0 0 24 46Z"
      />
      <path
        fill="#FBBC05"
        d="M11.69 28.18A13.2 13.2 0 0 1 11 24c0-1.45.25-2.86.69-4.18v-5.7H4.34A21.99 21.99 0 0 0 2 24c0 3.55.85 6.91 2.34 9.88l7.35-5.7Z"
      />
      <path
        fill="#EA4335"
        d="M24 10.75c3.23 0 6.13 1.11 8.41 3.29l6.31-6.31C34.91 4.18 29.93 2 24 2 15.4 2 7.96 6.93 4.34 14.12l7.35 5.7c1.73-5.2 6.58-9.07 12.31-9.07Z"
      />
    </svg>
  )
}
