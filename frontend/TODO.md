# TODO — frontend

## Como este arquivo é organizado

Os itens são separados por **quando** fazem sentido, não por gravidade:

- **A — Morre com o backend.** O problema desaparece sozinho quando a API existir.
  Corrigir agora é trabalho jogado fora. Só não deixe surpreender na apresentação.
- **B — Entra no mesmo PR do backend.** Hoje é inofensivo porque nada sai da
  máquina; no dia em que dados reais circularem, vira falha de verdade. Não dá
  para corrigir antes (falta decidir como a sessão funciona) e não pode ficar
  para depois. **São pré-requisito de merge do backend, não sugestões.**
- **C — Corrigir agora, mesmo em PoC.** Barato, visível na demonstração, ou risco
  que independe de haver backend.
- **D — Dívida aceita.** Fica como está de propósito. Cada item traz o **gatilho**:
  a condição que o move para C. Dívida sem gatilho é dívida esquecida.

---

## A — Morre com o backend

Nada a fazer aqui além de não se assustar. Listado para ninguém "corrigir" à toa.

- [ ] Senha mock `'123456'` compilada no bundle (`src/services/auth.ts:31`).
      Some junto com o `USUARIO_MOCK` quando `autenticar()` virar `fetch`.
- [ ] Contagem de anotações inventada: `categoriasIniciais[0].anotacoes = 12`, mas
      só existem 3 anotações com `categoriaId: 1` (`src/pages/Anotacoes/dados.ts:11`).
      Com a API o número passa a ser derivado.
- [ ] `atualizadaEm` nunca muda ao editar uma anotação
      (`src/pages/Anotacoes/Categoria.tsx:44`). O backend devolve o timestamp real.
- [ ] `aoSalvar()` do cadastro é `async` sem `await` — `isSubmitting` nunca aparece
      e `cadastrado` nunca volta a `false` (`src/pages/Cadastro/Cadastro.tsx:48`).
      A função inteira será reescrita quando o endpoint existir.
- [ ] Dados mock declarados dentro do componente (`src/pages/Inicio/Inicio.tsx:14-76`),
      enquanto `Anotacoes` usa `./dados.ts`. Dois padrões que viram um só com a API.
- [ ] Nome, iniciais e badge de notificação fixos no shell
      (`src/components/Layout.tsx:83-86`: "Aluno AEP", "AA", "3").
      Passam a vir do usuário autenticado.
- [ ] Categoria criada em `Anotacoes.tsx` não é encontrada em `Categoria.tsx` — uma
      guarda o state em `useState` (linha 26), a outra lê `categoriasIniciais` do
      módulo (linha 8). Criar → clicar → "Categoria não encontrada"; e o state some
      ao navegar. Resolve sozinho quando ambas lerem da API.
      ⚠ **Risco de demonstração:** se a apresentação inclui criar categoria, este
      item sobe para C. Solução provisória: contexto no `Layout`, que já envolve as
      duas rotas.

---

## B — Entra no mesmo PR do backend

Pré-requisitos de merge. Conferir antes de abrir o PR da integração.

- [ ] **Sanitizar o conteúdo das anotações antes de renderizar.**
      `src/pages/Anotacoes/Categoria.tsx:238` faz
      `dangerouslySetInnerHTML={{ __html: anotacaoSelecionada.conteudo }}`.
      Enquanto a fonte é local isso é aceitável; no instante em que `conteudo` vier
      da API, é injeção de HTML de servidor direto no DOM — XSS armazenado clássico
      se anotações forem compartilhadas. Usar DOMPurify na renderização.
      Não confie no conteúdo só porque saiu daqui limpo.
- [ ] **Proteger as rotas privadas.** `/inicio` e `/anotacoes` abrem digitando a URL
      (`src/App.tsx:14-18`). Hoje não vaza nada — tudo é mock. A guarda depende da
      decisão de sessão abaixo, por isso vive aqui e não em C.
      O `Layout` já envolve exatamente as rotas privadas: a checagem cabe nele,
      retornando `<Navigate to="/login" replace />` ou `<Outlet />`.
- [ ] Decidir como o backend devolve a sessão para a SPA depois do OAuth:
      cookie `HttpOnly` (exige `SameSite` e CORS configurados) ou JWT no redirect.
      Essa decisão define o que a guarda acima verifica.
- [ ] Substituir o mock de `src/services/auth.ts` por `fetch` para o endpoint real
      do Spring. Nada além do corpo da função precisa mudar.
- [ ] Habilitar `spring-boot-starter-oauth2-client` no backend: o endpoint
      `/oauth2/authorization/google` que o botão do Google já aponta passa a existir.
      Requer cadastrar as URLs de origem e de redirect no Google Cloud Console
      (inclusive `http://localhost` para desenvolvimento).
- [ ] Validar senha no servidor. A regra de cliente (item em C) é conveniência de
      UX, nunca a defesa — a validação que conta é a do backend.

---

## C — Corrigir agora, mesmo em PoC

### Risco real, independente de backend

- [ ] **Fechar o drag & drop no editor** (`src/pages/Anotacoes/Categoria.tsx`).
      O `onPaste` (linha 86) sanitiza colagem convertendo para texto puro, mas
      **não existe `onDrop`**: arrastar conteúdo de outra aba entra como HTML pelo
      caminho padrão do navegador, é lido de volta por `innerHTML` no `onInput` e
      volta pela renderização sem passar por sanitização.
      Uma linha: `onDrop={(evento) => evento.preventDefault()}`.
- [ ] **`try/catch` no `localStorage`** (`src/services/preferencias.ts:14`).
      `recuperarEmailLembrado()` roda no corpo do `Login` (linha 40), fora de error
      boundary. Onde o storage está bloqueado (aba anônima em alguns navegadores,
      política corporativa, iframe com cookies de terceiros bloqueados), `getItem`
      **lança** e a tela de login inteira fica em branco. Retornar `''` no catch.
      Uma preferência cosmética não pode derrubar a autenticação.
- [ ] **Senha mínima no cadastro** (`src/pages/Cadastro/Cadastro.tsx:21`).
      Hoje `z.string().min(1)` aceita senha de 1 caractere. Mínimo de 8.
      Validação em fronteira de confiança não é lugar de economizar.
- [ ] **Modal acessível** (`src/components/Modal.tsx:33`). Tem
      `role="dialog" aria-modal="true"` — que diz ao leitor de tela que o resto da
      página está inerte — mas não fecha com `Esc`, não prende o foco, não devolve o
      foco ao fechar e não trava o scroll do fundo.
      O `<dialog>` nativo entrega tudo isso sem dependência, e o resultado é
      **menos código** do que existe hoje:
      `useEffect(() => { aberto ? ref.current?.showModal() : ref.current?.close() }, [aberto])`
      (`showModal()`, não `show()` — é o que ativa o top layer, o `::backdrop` e o Esc.)

### Bugs visíveis

- [ ] **O modal "Nova categoria" abre vazio** (`src/pages/Anotacoes/Anotacoes.tsx:146`).
      Em `/anotacoes`, a janela abre com o cabeçalho e o "×", mas o corpo vem sem o
      campo e sem os botões — só o `padding: 24px` de `.conteudo`.
      **Já descartado:** o formulário existe no JSX (linhas 147-176) e as três
      classes (`.formularioCategoria`, `.acoesModal`, `.botaoCancelar`) existem em
      `Anotacoes.module.css:206-241`. Não é o caso do `.cabecalho` abaixo.
      **Pista:** na captura **não há `::backdrop`** — o fundo está nítido, sem o
      escurecimento nem o blur de `Modal.module.css:20-23`. Um `<dialog>` sem
      backdrop não foi aberto por `showModal()`. Isso casa com o corpo vazio:
      `Modal.tsx:68` só renderiza os filhos com `{aberto && children}`, então a
      janela está visível enquanto `aberto` ainda é `false` — as duas coisas têm
      a mesma causa. Investigar o efeito de `Modal.tsx:25-36`, cuja guarda
      `!janela.open` pula o `showModal()` se o diálogo já estiver aberto por
      outro caminho, deixando estado e DOM dessincronizados.
      ⚠ **Bloqueia a demonstração:** criar categoria é fluxo de tela, não depende
      do backend — e agrava o item de "Categoria criada não é encontrada" da
      seção A, que pressupõe que dá para criar uma.

- [ ] **`.cabecalho` não existe em `Login.module.css`.** Usada em
      `src/pages/Login/Login.tsx:100` e `src/pages/Cadastro/Cadastro.tsx:74`;
      `styles.cabecalho` é `undefined`, o React omite o atributo e o header fica sem
      estilo nas duas telas, silenciosamente. Criar a classe ou remover o uso.
      (Foi a única divergência do tipo no projeto — as demais classes conferem.)
- [ ] **Limpar o erro de login ao reenviar** (`src/pages/Login/Login.tsx:60`).
      `status` só muda no `catch`. Se o login falhar, o usuário corrigir o e-mail e
      o reenvio parar na validação do Zod, o alerta antigo de "E-mail ou senha
      incorretos" continua na tela contradizendo o erro de campo.
      `setStatus({ tipo: 'ocioso' })` no início de `aoEnviar`.
- [ ] **O modal "Tarefas pendentes" mente** (`src/pages/Inicio/Inicio.tsx:95`).
      O filtro exclui as pendentes de hoje (`!tarefa.paraHoje`), então uma tarefa
      pendente de hoje não aparece em lugar nenhum do modal. Corrigir o filtro ou o
      rótulo. De quebra, `proximasTarefas` (linha 91) é o mesmo filtro escrito duas
      vezes — a segunda é `.slice(0, 4)` da primeira.
- [ ] **`<title>Login</title>` fixo** (`index.html:12`). A aba diz "Login" durante a
      demonstração inteira, inclusive em `/anotacoes`.

### Arrumação barata (agora custa minutos; depois custa um refactor)

- [ ] Mover `src/pages/Login/icones.tsx` para `src/components/`.
      Hoje `src/components/Layout.tsx:3` importa de dentro de uma página —
      dependência invertida, e quanto mais gente importar dali, pior fica.
- [ ] Centralizar `'Sistema AEP'`, hoje repetido em `Login.tsx:16`,
      `Cadastro.tsx:11` e `Layout.tsx:34`. O próprio código diz
      "Identidade provisória: trocar quando o nome do sistema for definido" —
      é justamente por ser provisório que precisa estar em um lugar só.
- [ ] Remover os dois `!important` de `src/components/Layout.module.css:99-100`.
      Existem para vencer `.navegacao a` (especificidade 0-2-1 contra 0-1-0);
      `.navegacao .itemAtivo` resolve sem gambiarra.
- [ ] Adicionar Prettier. Resolve a indentação quebrada de
      `src/pages/Inicio/Inicio.tsx` (linhas 82-93, 169-183, 224-259 — blocos colados
      na coluna 0) e tira formatação da pauta de review para sempre.
      O `eslint` atual passa limpo e não pega nada disso.
- [ ] Reescrever o `README.md`. Ainda é o template do Vite, em inglês, explicando
      SWC e Oxc. É um entregável de faculdade — o professor abre o repositório.

### Testes — requisito da disciplina, não item opcional

- [ ] Configurar Vitest + @testing-library/react (`environment: 'jsdom'`,
      `setupFiles` com `@testing-library/jest-dom`) e `vitest --coverage` no
      `package.json`, que hoje não tem sequer um script `test`.
- [ ] Cobrir os critérios de aceite da tela de login: e-mail obrigatório, senha
      obrigatória, senha inicia oculta, alternância de visibilidade, mensagem de
      credenciais inválidas, "Lembre-me" disponível, "Cadastre-se" leva a `/cadastro`.
      Mocke `src/services/auth.ts` para não depender de temporizador real.
- [ ] Garantir cobertura ≥ 70% — exigência da AEP (`docs/AEP_ESoft_6S.md`).
      Quanto mais telas entram antes disso, mais caro fica começar.

---

## D — Dívida aceita (com gatilho)

Fica como está de propósito. O gatilho é o que move o item para C.

- [ ] `Cadastro.tsx:8` importa `Login.module.css` — uma página se estilizando com o
      CSS de outra, e o painel lateral está duplicado nas duas telas.
      **Gatilho:** entrar uma terceira tela de autenticação (ex. "esqueci minha
      senha"), ou o Login precisar mudar de layout. Aí extrair `Auth.module.css` e
      um componente `TelaAuth`.
- [ ] Cores de estado duplicadas hex a hex em três arquivos: `#c52852`/`#fce3eb`,
      `#26744f`/`#dff5e9`, `#9a6700`/`#fff2ce` em `Inicio.module.css`,
      `Anotacoes.module.css` e `Categoria.module.css`.
      **Gatilho:** uma quarta tela usar as mesmas cores, ou alguém pedir ajuste de
      contraste. O `index.css` já tem sistema de tokens — é só estender.
- [ ] `.prioridadeMédia` (`Inicio.module.css:236`), acessada via
      `styles['prioridade' + tarefa.prioridade]`. Conferidos os code points: TSX e
      CSS estão ambos em NFC, então **funciona hoje**. O risco é depender de os dois
      arquivos continuarem na mesma normalização Unicode — um editor que salve em
      NFD quebra sem nenhum erro, só uma classe `undefined`.
      **Gatilho:** qualquer mexida nesse arquivo. Trocar por slug ASCII
      (`alta`/`media`/`baixa`) elimina a categoria de risco inteira.
- [ ] `Modal.module.css:57`: `max-height: calc(min(80dvh, 680px) - 79px)`.
      O `79px` é a altura do cabeçalho calculada à mão, mas o cabeçalho muda de
      padding no `@media (max-width: 480px)` (linha 67) — o cálculo fica errado no
      mobile. A correção (`flex-direction: column` na `.janela` + `flex: 1` no
      `.conteudo`) é menor que o código atual.
      **Gatilho:** a demonstração ser em tela pequena, ou a troca para `<dialog>`
      em C, que já mexe nesse arquivo.
- [ ] Botão "Tarefas" no menu não faz nada e não tem `className`
      (`src/components/Layout.tsx:48`). É placeholder honesto de feature futura.
      **Gatilho:** a apresentação. Antes dela, pelo menos `disabled`, para não
      parecer quebrado em vez de "ainda não implementado".
- [ ] CSS morto: `.voltar` em `Anotacoes.module.css` (quem usa `.voltar` é
      `Categoria`, do próprio CSS) e `.vazioLateral` em `Categoria.module.css`.
      **Gatilho:** próxima faxina — junto com o item abaixo.
- [ ] `src/assets/` (hero.png, react.svg, vite.svg) e `public/icons.svg` são restos
      da demo do Vite, órfãos desde a substituição do `App.tsx`.
      **Gatilho:** o time confirmar que não serão reaproveitados.

---

## Concluídos

- [x] Redirecionar para a tela inicial após autenticar. Feito em
      `src/pages/Login/Login.tsx:64` (`navigate('/inicio')`).
