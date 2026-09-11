# Checklist de revisão

Lido **depois** de escrever, antes de abrir PR. `docs/padroes.md` é lido antes —
são momentos diferentes, por isso são dois arquivos.

Este checklist é, linha por linha, o que a correção procura num diff de backend.
Rodar uma vez por PR e uma vez antes da tag de entrega.

Como usar: passe pelos itens na ordem. Um item que falha não é opinião de
revisor, é divergência da convenção registrada — corrija ou mude a convenção,
nunca deixe os dois discordando.

---

## 1. Fronteiras entre camadas

- [ ] **Controller sem regra de negócio.** Nada de `if` sobre domínio, nada de
      cálculo, nada de `try/catch` de fluxo normal. Só `@Valid`, status, header e
      delegação.
- [ ] **Controller não toca Repository.** A injeção é do Service.
- [ ] **Service não conhece HTTP.** Nenhum `ResponseEntity`, `HttpStatus`,
      `HttpServletRequest` ou anotação de web dentro de `service/`.
- [ ] **Repository sem regra.** Interface de corpo vazio, ou só métodos de
      consulta derivados do nome. Nenhuma decisão de negócio.
- [ ] **Model não atravessa HTTP.** Nenhum método de Controller recebe ou devolve
      `Tarefa` — nem dentro de `List`, `Optional` ou `ResponseEntity`.
- [ ] **Campo derivado nasce no Service, não no Mapper.** O Mapper só traduz o
      que veio do cliente. `concluida = false`, `criadaEm`, `atualizadaEm`:
      Service.

```bash
# Model vazando pela fronteira HTTP
grep -rn "Tarefa\b" backend/src/main/java/br/com/cesumar/aep/controller/

# HTTP vazando para dentro do Service
grep -rnE "ResponseEntity|HttpStatus|HttpServlet" backend/src/main/java/br/com/cesumar/aep/service/
```

## 2. DTOs

- [ ] **Um DTO por caso de uso.** Nenhum `record` reaproveitado entre criação,
      atualização e resposta por conveniência.
- [ ] **Validação nos Requests.** Jakarta Validation nos campos obrigatórios,
      com mensagem em português.
- [ ] **O DTO não aceita o que é regra.** `concluida` não existe em
      `TarefaCreateRequest` — tarefa nasce pendente. Campo novo nesses dois
      passa por essa pergunta antes de entrar.
- [ ] **Boolean obrigatório é `Boolean`, não `boolean`.** Com o primitivo, a
      ausência do campo vira `false` calado em vez de `400`.
- [ ] **Mudou DTO de resposta?** `docs/api-http.md` mudou junto.

## 3. Persistência

- [ ] **O `id` sobrevive ao update.** O Mapper altera a instância vinda do banco,
      não constrói uma nova. Vale para `criadaEm` também.
- [ ] **Campo novo no Model entrou no `$jsonSchema`.** `additionalProperties:
      false` recusa campo não declarado — o insert falha em produção e o teste de
      integração não pega, porque o contêiner sobe sem `init.js`.
- [ ] **Campo novo no Model entrou em `docs/modelagem-banco.md`.**
- [ ] **`enum` novo tem par de conversores.** O Spring Data grava
      `Enum.name()`; o banco espera slug ASCII.
- [ ] **Nada de campo derivado gravado.** Contagem, "é para hoje", rótulo
      formatado: derive na leitura. Campo derivado guardado é campo que vai
      mentir.
- [ ] **Data de calendário continua à meia-noite UTC.** Nenhum `atStartOfDay`
      com fuso local, nenhum `new Date("AAAA-MM-DD")` no cliente.
- [ ] **Índice novo tem justificativa.** Índice que nenhuma consulta usa é
      escrita mais lenta de graça.

## 4. Erros

- [ ] **Exceção nova tem handler.** Toda exceção de domínio aparece no
      `GlobalExceptionHandler` e sai como `ApiError`.
- [ ] **Nenhum `try/catch` de fluxo normal.** "Não encontrado" é exceção lançada
      no Service, não `Optional` tratado no Controller.
- [ ] **`fieldErrors` vem vazio, nunca nulo.** O `crud.html` itera sem checar.
- [ ] **Status novo documentado** em `docs/api-http.md`.

## 5. Testes

- [ ] **Comportamento novo tem teste novo.**
- [ ] **Na camada certa, e em uma só.** Regra de negócio → só
      `TarefaServiceTest`. Status, JSON, validação → só `TarefaControllerTest`.
      Persistência real → só `TarefaApiIT`. A mesma regra em duas camadas é o que
      faz a suíte doer a cada refactor.
- [ ] **Sufixo correto.** `*Test` roda no surefire (`test`), `*IT` no failsafe
      (`verify`). Errar o sufixo faz o teste de integração rodar duas vezes ou
      **nenhuma** — e "nenhuma" passa despercebido, porque o build fica verde.
- [ ] **A projeção é provada pela ausência.** Teste de listagem tem
      `jsonPath(...).doesNotExist()` nos campos fora do resumo. Sem isso, um
      `TarefaResponse` devolvido por engano passa verde.
- [ ] **Nome em português, sem `test` e sem `_`.** Um comportamento por teste.
- [ ] **Sem dependência de ordem.** Nenhum `@Order`, nenhum estado mutável
      compartilhado, nenhum id fixo do banco, nenhum `Thread.sleep`.
- [ ] **`repository.deleteAll()` no `@BeforeEach` do `*IT`.**
- [ ] **Instante comparado usa o `Clock` injetado,** não `Instant.now()` — no
      Windows duas chamadas seguidas podem devolver o mesmo valor.
- [ ] **`./mvnw clean test` passa sem Docker,** e T17 + T18 sozinhos seguem acima
      de 70%. O `*IT` é evidência adicional, nunca a perna que sustenta o número.

## 6. Convenção de escrita

- [ ] **Nome derivado do domínio.** Sufixo técnico em inglês (`Request`,
      `Service`), campo e método em português (`titulo`, `buscarPorId`).
- [ ] **Ordem dos membros:** constantes, campos `final`, construtor, públicos na
      ordem do CRUD (`listar`, `buscarPorId`, `criar`, `atualizar`, `excluir`),
      privados no fim.
- [ ] **Injeção por construtor,** sem `@Autowired` em campo e sem `@Autowired` no
      construtor.
- [ ] **Sem Lombok, sem MapStruct.**
- [ ] **Sem interface de uma implementação.** Nada de `ServiceImpl` sem problema
      concreto que justifique.
- [ ] **Comentário explica decisão, não sintaxe.** Comentário que repete o código
      sai.
- [ ] **Nenhuma marca de ferramenta em comentário** (`claude:`, `ponytail:` ou
      equivalente). `TODO(nome):` é permitido — identifica pessoa, não
      ferramenta.

```bash
grep -rniE "(claude|ponytail|copilot|cursor|gpt|gemini):" backend/src frontend/src
```

## 7. Documentação e histórico

- [ ] **A documentação afetada foi conferida.** Mudou contrato →
      `docs/api-http.md`. Mudou camada → `docs/arquitetura.md`. Mudou banco →
      `docs/modelagem-banco.md`. Decisão nova → `docs/decisoes.md`. Tarefa
      concluída → caixa marcada em `docs/backend-tarefas.md`.
- [ ] **Nenhum documento afirma o que o código não faz.** Divergência entre doc e
      código conta contra, e é mais fácil de achar do que um bug.
- [ ] **Commits em Conventional Commits, em português, com escopo,** até 72
      caracteres na primeira linha. Corpo explica **por que**.
- [ ] **Número de cobertura citado é o medido hoje,** com o comando ao lado.
- [ ] **Limitação de ambiente relatada, não silenciada.** Teste não executado,
      Docker ausente, verificação manual pendente: escrito em voz alta. É item da
      Definition of Done, não cortesia.

## 8. Antes da tag de entrega

- [ ] `./mvnw clean test` verde, sem Docker.
- [ ] `./mvnw clean verify` verde, com o gate do JaCoCo.
- [ ] Número de cobertura no README igual ao do relatório recém-gerado.
- [ ] `docker compose down -v && docker compose up -d` sobe o banco, e o
      `crud.html` executa criar, listar, atualizar e excluir contra ele.
- [ ] `/docs` abre e o contrato mostrado é o que `docs/api-http.md` descreve.
- [ ] README responde: problema, ODS, público, stack, como subir, como executar,
      como testar, como medir cobertura e o número obtido.
- [ ] Branches de todos os integrantes mescladas — "histórico de commits" é
      critério de nota, e um histórico de um autor só conta contra.
- [ ] Tag criada na `main`, nomeando a entrega.
