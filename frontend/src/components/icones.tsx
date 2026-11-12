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
