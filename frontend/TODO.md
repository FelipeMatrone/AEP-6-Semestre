# TODO — frontend

Pendências deixadas conscientemente na implementação da tela de login.

## Testes

- [ ] Configurar Vitest + @testing-library/react (`environment: 'jsdom'`, `setupFiles`
      com `@testing-library/jest-dom`) e `vitest --coverage` no `package.json`.
- [ ] Cobrir os critérios de aceite da tela de login:
      e-mail obrigatório, senha obrigatória, senha inicia oculta,
      alternância de visibilidade, mensagem de credenciais inválidas,
      "Lembre-me" disponível, "Cadastre-se" leva a `/cadastro`.
- [ ] Garantir cobertura ≥ 70% — exigência da AEP (`docs/AEP_ESoft_6S.md`).
      Nos testes, mocke `src/services/auth.ts` para não depender de temporizador real.

## Backend / autenticação

- [ ] Substituir o mock de `src/services/auth.ts` por `fetch` para o endpoint
      real do Spring. Nada além do corpo da função precisa mudar.
- [ ] Habilitar `spring-boot-starter-oauth2-client` no backend: o endpoint
      `/oauth2/authorization/google` que o botão do Google já aponta passa a
      existir. Requer cadastrar as URLs de origem e de redirect no
      Google Cloud Console (inclusive `http://localhost` para desenvolvimento).
- [ ] Decidir como o backend devolve a sessão para a SPA depois do OAuth:
      cookie `HttpOnly` (exige `SameSite` e CORS configurados) ou JWT no
      redirect. Essa decisão define o que a tela faz **depois** do login.
- [ ] Redirecionar para a tela inicial após autenticar — hoje a tela apenas
      exibe uma mensagem de sucesso, porque ainda não há para onde ir.

## Faxina

- [ ] `src/assets/` (hero.png, react.svg, vite.svg) e `public/icons.svg` são
      restos da demo do Vite e ficaram órfãos ao substituir o `App.tsx`.
      Remover quando o time confirmar que não serão reaproveitados.
