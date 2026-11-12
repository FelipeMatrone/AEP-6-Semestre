# Frontend — AEP 6º Semestre

Interface web do projeto da Atividade de Ensino Prática do 6º semestre.
React 19 + TypeScript, build com Vite, roteamento com React Router e formulários
com react-hook-form + zod.

Fala com o backend Spring pela API REST: o Vite repassa `/api` para
`http://localhost:8080` (`vite.config.ts`), então suba o backend antes.

## Como rodar

```bash
npm install
npm run dev
```

Outros comandos:

| Comando | O que faz |
|---|---|
| `npm run dev` | Servidor de desenvolvimento com HMR |
| `npm run build` | Checagem de tipos (`tsc -b`) e build de produção |
| `npm run preview` | Serve o build de produção localmente |
| `npm run lint` | ESLint |

## Estrutura

```
src/
├── components/     Componentes compartilhados (Layout, Modal, CampoTexto, icones)
├── pages/          Uma pasta por tela, com seu CSS Module ao lado
│   ├── Login/
│   ├── Cadastro/
│   ├── Inicio/     Painel principal
│   ├── Tarefas/
│   ├── Anotacoes/  Categorias e o editor de anotações
│   ├── Calendario/
│   └── Perfil/     Estatísticas, dados pessoais e troca de senha
├── services/       Chamadas à API (http.ts cuida de CSRF e erros) e preferências
├── config.ts       Constantes de identidade do sistema
└── index.css       Tokens de cor, tipografia e reset
```

## Convenções

- **Estilos:** CSS Modules (`*.module.css`) ao lado do componente. Cores,
  espaçamentos e raios saem dos tokens de `src/index.css` — evite hex solto.
- **Nomes:** código em português (componentes, funções, variáveis, classes CSS),
  acompanhando o restante do repositório.
- **Formulários:** react-hook-form com resolver zod e `mode: 'onTouched'`.
- **Acessibilidade:** rótulo associado a todo campo, `aria-label` em botão só de
  ícone, e `<dialog>` nativo para modais — não reimplemente foco preso ou Esc.

Convenções de commit e de branch estão no `AGENTS.md` da raiz do repositório.
