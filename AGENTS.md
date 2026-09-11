# AEP 6º Semestre — convenções do repositório

Lido por qualquer pessoa e qualquer ferramenta de IA que escreva neste
repositório. O `CLAUDE.md` cobre só o que é específico do Claude Code e aponta
para cá; uma regra escrita em dois arquivos envelhece em um deles.

## Objetivo

PoC de um gerenciador de tarefas para a AEP do 6º semestre. A entrega avaliada
no 1º bimestre é a **API**; o cliente React em `frontend/` está em
desenvolvimento e não faz parte da medição de cobertura.

O planejamento por tarefa numerada, com as decisões travadas e o caminho
crítico, vive em `docs/backend-tarefas.md`. É o documento a consultar antes de
começar qualquer coisa no backend.

## Stack

| Camada | Tecnologia |
|---|---|
| Backend | Java 21, Spring Boot 3.5.16, Maven (wrapper commitado) |
| Banco | MongoDB, via `docker-compose.yml` na raiz |
| Doc da API | Springdoc, Swagger UI em `/docs` |
| Testes | JUnit 5, Mockito, MockMvc, Testcontainers |
| Cobertura | JaCoCo, gate de 70% de linha no `verify` |
| Frontend | React + TypeScript + Vite |

Package base: **`br.com.cesumar.aep`**. O backend fica em `backend/`.

## Fluxo entre as camadas

```
HTTP → Controller → Service → Repository → MongoDB
              ↕ Mapper ↕
            DTO      Model
```

- **Controller** traduz HTTP ↔ caso de uso. Sem regra de negócio.
- **Service** é onde a regra vive. Não conhece HTTP.
- **Repository** só persiste. Nenhuma regra.
- **Mapper** traduz o que veio do cliente. Campo derivado nasce no Service.
- **Model** (`@Document`) nunca atravessa a fronteira HTTP.

A ordem de implementação de um domínio novo é
`Model → Repository → Request DTOs → Response DTOs → Mapper → Service →
Controller → Exceptions → Tests`.

As regras completas — nomes por camada, ordem dos membros, proibições,
estrutura e matriz de testes — estão em **`docs/padroes.md`**, lido *antes* de
escrever código.

<!-- criados em T30 e T32 de docs/backend-tarefas.md; ainda não existem -->
Documentos previstos: `docs/arquitetura.md` (o desenho das camadas) e
`docs/checklist-revisao.md` (lido *depois*, antes de abrir PR).

## Definition of Done

Uma tarefa só está concluída quando:

1. `./mvnw clean compile` passa;
2. `./mvnw clean test` passa — e **sem exigir Docker**;
3. `./mvnw clean verify` passa, incluindo o gate de 70% do JaCoCo;
4. existe teste novo para comportamento novo, na camada certa e em uma só;
5. os contratos da API continuam válidos;
6. a documentação afetada foi conferida;
7. qualquer limitação de ambiente (Docker ausente, teste não executado) foi
   **relatada explicitamente**, nunca silenciada.

`./mvnw clean test` não pode depender de Docker: `TarefaServiceTest` e
`TarefaControllerTest` sozinhos já precisam passar de 70%. O teste `*IT` é
evidência adicional, não a perna que sustenta o número.

## Commits

Padrão: **Conventional Commits**, mensagens em **português**, escopo obrigatório.

```
tipo(escopo): descrição no imperativo, minúscula, sem ponto final
```

- Máximo de 72 caracteres na primeira linha.
- Corpo opcional, separado por linha em branco, explicando **por que** — o que
  mudou já está no diff.
- Mudança incompatível: `BREAKING CHANGE:` no rodapé.

**Tipos:** `feat` (nova funcionalidade), `fix` (correção), `docs` (documentação),
`style` (formatação, sem efeito em comportamento), `refactor` (reestruturação sem
mudar comportamento), `test` (testes), `chore` (build, dependências, configuração).

**Escopos em uso:** `login`, `cadastro`, `frontend`, `backend`, `docs`, `config`.

Exemplos:

```
feat(backend): adiciona CRUD de tarefas na API
fix(login): corrige botão de visualizar senha enviando o formulário
chore(backend): adiciona springdoc e o wrapper do maven
docs(aep): registra pendências de teste no TODO.md
```

## Branches

Uma branch por integrante, nome próprio em minúsculas: `felipe`, `samuel`.
Recorte por assunto usa hífen (`samuel-backend`) — barra não funciona enquanto
existir uma branch com o nome do prefixo.

## Trabalho assistido por IA

- **Não commitar sem pedido explícito.** Implementar e parar; o commit é decisão
  de quem revisa.
- Decisões de arquitetura, dependências novas e mudanças de escopo são
  perguntadas antes, não assumidas.
- **Comentários no código não levam marca de ferramenta.** Nada de prefixo
  `ponytail:`, `claude:` ou equivalente. O comentário explica a decisão técnica
  e pronto — quem lê o repositório não precisa saber qual ferramenta escreveu:

  ```ts
  // ✗  // ponytail: document.execCommand é deprecated mas ainda funciona
  // ✓  // document.execCommand é deprecated mas ainda funciona
  ```

  Vale para qualquer marcador do tipo, inclusive em código novo. `TODO(nome):`
  continua permitido — identifica pessoa responsável, não ferramenta.
