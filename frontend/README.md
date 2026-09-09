# Frontend — AEP 6º Semestre

Interface web do projeto da Atividade de Ensino Prática do 6º semestre.
React 19 + TypeScript, build com Vite, roteamento com React Router e formulários
com react-hook-form + zod.

**Estado atual:** roda inteiramente sem backend. Login e cadastro são mocks e as
anotações vivem em memória — veja `TODO.md`, que separa o que some sozinho quando
a API existir do que precisa ser corrigido antes.

## Como rodar

```bash
npm install
cp .env.example .env   # ajuste VITE_API_URL se o backend não estiver em :8080
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
│   └── Anotacoes/  Categorias e o editor de anotações
├── services/       Acesso a dados e preferências (hoje mockados)
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

Convenções de commit e de branch estão no `CLAUDE.md` da raiz do repositório.
