# Segunda entrega — plano de implementação

> **Para quem executa:** use `superpowers:subagent-driven-development` para
> implementar tarefa por tarefa. Os passos usam `- [ ]` para acompanhamento.

**Goal:** levar o backend de uma coleção achatada para três coleções com
subdocumentos, relacionamentos, autenticação e agregação, com o aplicativo
usável de ponta a ponta e o gate de 70% verde em todo o percurso.

**Architecture:** as camadas de `docs/padroes.md` continuam valendo
(`Controller → Service → Repository → MongoDB`, `Mapper` entre DTO e Model). O
contrato HTTP sobe primeiro com dados fixos para destravar o frontend, e o
recheio vira Mongo por baixo sem o contrato mudar.

**Tech Stack:** Spring Boot 3.5.16, Java 21, Spring Data MongoDB, Spring
Security, Springdoc, JUnit 5, Mockito, MockMvc, Testcontainers, JaCoCo.

**Spec:** [`docs/segunda-entrega.md`](segunda-entrega.md)

## Global Constraints

- `./mvnw clean test` passa **sem Docker**. Só `*IT` pode exigir container.
- Gate JaCoCo: `BUNDLE LINE COVEREDRATIO ≥ 0.70`. `configuration/**` e
  `AepApplication` ficam fora do denominador.
- Commits em Conventional Commits em português, com escopo obrigatório, primeira
  linha ≤ 72 caracteres. **Sem rodapé de atribuição.**
- **Não commitar sem pedido explícito** (`AGENTS.md`). Cada tarefa termina com o
  commit preparado; quem revisa decide.
- Comentários em código **não levam marca de ferramenta**. `TODO(nome):` é
  permitido.
- Datas em BSON Date, sempre UTC. `Instant.now(clock)` com o `Clock` injetado —
  nunca `Instant.now()`.
- `senhaHash` nunca atravessa a fronteira HTTP.
- Nomes de campo e slugs de `cor` conforme a tabela "Formatos na fronteira" da
  spec. `cor` ∈ {`roxo`, `magenta`, `verde`, `ambar`}.

## Review Focus

Cinco classes de entrada que a spec implica, que nenhuma tarefa cobriria por
inércia, e que doem em quem usa. Cada uma ganhou teste na tarefa que é dona do
código:

1. **`conteudo` com 20 001 caracteres** → 400 com erro de campo. Truncar calado
   perde texto que a pessoa escreveu. (Tarefa 4)
2. **`anotacaoId` que existe, mas em outra categoria** → 404. Sem isso, editar
   por id acerta a anotação errada. (Tarefa 4)
3. **`prazo` exatamente hoje** → **não** é atrasada. A fronteira do `<` é onde
   contador de atraso erra. (Tarefa 10)
4. **E-mail com maiúsculas** no cadastro e no login → mesma conta. O índice único
   é sensível a caixa; sem normalizar, `A@x.com` e `a@x.com` coexistem. (Tarefa 7)
5. **Última anotação removida** → categoria fica com `anotacoes: []` e
   `totalAnotacoes: 0`, não `null`. Lista nula quebra o `.map` da tela. (Tarefa 4)

`cor` fora do enum já devolve 400 pelo `HttpMessageNotReadableException` do
`GlobalExceptionHandler` existente — a Tarefa 3 só pina o comportamento.

## Grupos de revisão

Revisão por grupo, não por tarefa. O revisor de cada grupo é um agente fresco e
só roda ao final do grupo:

| Grupo | Tarefas | Modelo sugerido p/ execução | Revisor |
|---|---|---|---|
| **A — contrato** | 1, 2 | haiku (DTOs e controllers mecânicos) | sonnet |
| **B — persistência** | 3, 4, 5, 6 | sonnet | opus |
| **C — segurança** | 7, 8, 9 | opus (peça mais arriscada) | opus |
| **D — agregação** | 10, 11 | sonnet | opus |
| **E — entrega** | 12 | haiku | — (revisão final de branch) |

Cada grupo termina em **PR de `samuel-backend` para `dev`**, com a revisão do
grupo no corpo do PR. Ao fim de tudo, uma revisão de branch inteira, e só então
`dev` → `main` → tag.

Um grupo não começa antes do PR do anterior estar aberto: o PR é o ponto onde o
Felipe vê o que mudou no backend dele.

## Estrutura de arquivos

Novos pacotes seguem os existentes. Um arquivo por responsabilidade.

```
model/        Usuario, Categoria, Anotacao, Cor        (+ Tarefa modificado)
repository/   UsuarioRepository, CategoriaRepository, PainelRepository
dto/          Usuario{Create,}…, Categoria{Create,Update,Response,SummaryResponse},
              Anotacao{Create,Update,Response}, SessaoCreateRequest, PainelResponse,
              ProgressoResponse                        (+ Tarefa* modificados)
mapper/       CategoriaMapper, UsuarioMapper           (+ TarefaMapper modificado)
service/      CategoriaService, UsuarioService, PainelService, UsuarioAtual
                                                       (+ TarefaService modificado)
controller/   CategoriaController, UsuarioController, SessaoController,
              PainelController                         (+ TarefaController modificado)
exception/    RecursoNaoEncontradoException (base), CategoriaNotFoundException,
              AnotacaoNotFoundException, EmailJaCadastradoException
configuration/ SecurityConfiguration                   (fora do JaCoCo)
banco/        init.js reescrito
```

`CategoriaService` cuida de categoria **e** das anotações dentro dela: a anotação
não existe fora da categoria, e separar criaria duas classes que só conversam
entre si.

---

## Antes de começar: a branch `dev`

Versionamento é 0,1 da nota. `dev` é a **branch intermediária de integração**:
ninguém commita nela direto, e `main` só é atualizada no fim.

O fluxo é este, e só este:

```
samuel-backend ──PR──┐
                     ├──> dev ──> main ──> tag
felipe-<branch> ─PR──┘
```

- **As branches por pessoa continuam** — uma por membro, como `AGENTS.md` já
  manda. O trabalho deste plano fica em `samuel-backend`.
- **Terminou um grupo, abre PR para `dev`.** Cinco grupos de revisão, cinco PRs.
  O PR é onde a revisão do grupo aparece em texto, o que também serve de
  evidência de método.
- **`main` recebe `dev` no fim**, e a tag sai na `main` depois disso.

`samuel-backend` é **publicada e compartilhada** — commits novos por cima, nunca
force-push, rebase ou amend.

- [ ] **Criar `dev` a partir de `main`, uma vez, e voltar para a branch de trabalho**

```bash
git checkout main && git pull
git checkout -b dev && git push -u origin dev
git checkout samuel-backend
```

- [ ] **Commitar os dois documentos que já existem e ainda não estão no histórico**

`docs/segunda-entrega.md` e `docs/plano-segunda-entrega.md` estão no disco e
fora do git. Entram antes da Tarefa 1 — o desenho precisa estar versionado antes
do código que ele descreve.

```bash
git add docs/segunda-entrega.md docs/plano-segunda-entrega.md
git commit -m "docs: desenha e planeja a segunda entrega"
```

---

## Grupo A — contrato

### Task 1: Enum `Cor`, exceção base e DTOs do contrato

**Files:**
- Create: `model/Cor.java`, `exception/RecursoNaoEncontradoException.java`
- Create: `dto/CategoriaSummaryResponse.java`, `dto/CategoriaResponse.java`,
  `dto/CategoriaCreateRequest.java`, `dto/CategoriaUpdateRequest.java`,
  `dto/AnotacaoResponse.java`, `dto/AnotacaoCreateRequest.java`,
  `dto/AnotacaoUpdateRequest.java`, `dto/UsuarioResponse.java`,
  `dto/UsuarioCreateRequest.java`, `dto/SessaoCreateRequest.java`,
  `dto/PainelResponse.java`, `dto/ProgressoResponse.java`
- Modify: `exception/TarefaNotFoundException.java`,
  `exception/GlobalExceptionHandler.java`
- Test: `src/test/java/br/com/cesumar/aep/exception/GlobalExceptionHandlerTest.java`
  (existente, se houver — senão o teste do handler entra na Tarefa 2)

**Interfaces:**
- Consumes: nada.
- Produces: todos os records de DTO acima; `Cor` como enum de 4 valores com
  slugs minúsculos; `RecursoNaoEncontradoException extends RuntimeException` com
  construtor `(String mensagem)`.

Os campos de cada record vêm da seção "Formatos na fronteira" da spec. Validação
Bean Validation nos requests: `@NotBlank` em `nome`, `titulo` e `conteudo`;
`@Size(max = 20_000)` em `conteudo`; `@Email @NotBlank` em `email`;
`@NotNull` em `cor`.

- [ ] **Step 1: Criar `Cor` e `RecursoNaoEncontradoException`**

`Cor` é enum com `ROXO, MAGENTA, VERDE, AMBAR`, serializado em minúsculas. Siga
exatamente o padrão de slug que `model/Prioridade.java` já usa — o mecanismo de
desserialização e o 400 para valor desconhecido vêm de lá.

- [ ] **Step 2: Fazer `TarefaNotFoundException` estender a base**

`TarefaNotFoundException extends RecursoNaoEncontradoException`. Em
`GlobalExceptionHandler`, trocar `@ExceptionHandler(TarefaNotFoundException.class)`
por `@ExceptionHandler(RecursoNaoEncontradoException.class)` e renomear o método
para `recursoNaoEncontrado`. Um handler para as quatro exceções de 404, em vez de
quatro handlers iguais.

- [ ] **Step 3: Criar os 12 records de DTO**

Todos `record`, como os DTOs existentes. Nenhuma lógica.

- [ ] **Step 4: Verificar que compila e que nada quebrou**

Run: `cd backend && ./mvnw clean test`
Expected: PASS, mesma contagem de testes de antes.

- [ ] **Step 5: Preparar o commit**

```bash
git add backend/src/main/java/br/com/cesumar/aep
git commit -m "feat(backend): adiciona DTOs e enum Cor do contrato da 2a entrega"
```

---

### Task 2: Controllers com dados fixos + OpenAPI

**Files:**
- Create: `controller/CategoriaController.java`, `controller/UsuarioController.java`,
  `controller/SessaoController.java`, `controller/PainelController.java`
- Test: `src/test/java/br/com/cesumar/aep/controller/CategoriaControllerTest.java`,
  `.../PainelControllerTest.java`

**Interfaces:**
- Consumes: os DTOs da Tarefa 1.
- Produces: as rotas da tabela "Contrato da API" da spec, com os status que ela
  fixa. As Tarefas 3–10 trocam o corpo dos métodos sem mexer nas assinaturas.

Os dados fixos vivem **dentro do controller**, em constante privada, e saem na
tarefa que liga o Service. Não crie classe de "fake service": ela viraria código
morto a ser caçado depois.

- [ ] **Step 1: Escrever os testes MockMvc do contrato**

```java
@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {
    // GET /api/categorias devolve 200 e lista com id, nome, cor e totalAnotacoes
    void listarDevolveCategoriasComTotalDeAnotacoes()
    // GET /api/categorias/{id} devolve 200 e a categoria com anotacoes aninhadas
    void buscarPorIdDevolveAnotacoesAninhadas()
    // POST /api/categorias com nome vazio devolve 400 e fieldErrors.nome
    void criarSemNomeDevolve400()
    // POST /api/categorias com cor fora do enum devolve 400   <- Review Focus
    void criarComCorInvalidaDevolve400()
    // DELETE /api/categorias/{id} devolve 204
    void excluirDevolve204()
}
```

```java
@WebMvcTest(PainelController.class)
class PainelControllerTest {
    // GET /api/painel devolve 200 com pendentes, concluidas, atrasadas e progresso
    void painelDevolveTotaisEProgresso()
}
```

- [ ] **Step 2: Rodar e confirmar que falham**

Run: `cd backend && ./mvnw test -Dtest='CategoriaControllerTest,PainelControllerTest'`
Expected: FAIL — controllers não existem.

- [ ] **Step 3: Implementar os quatro controllers com dados fixos**

Mesmo formato de `TarefaController`: `@RestController`, `@RequestMapping`,
`ResponseEntity.created(...)` com `Location` no POST, `@ResponseStatus(NO_CONTENT)`
no DELETE. `SessaoController` devolve o usuário fixo no `POST` e no `GET`, e 204
no `DELETE`.

- [ ] **Step 4: Rodar e confirmar que passam**

Run: `cd backend && ./mvnw clean test`
Expected: PASS.

- [ ] **Step 5: Conferir o OpenAPI à mão**

Run: `cd backend && ./mvnw spring-boot:run` e abrir `http://localhost:8080/docs`
Expected: as 13 rotas da spec aparecem com os schemas dos DTOs. **É isto que o
Felipe consome.** Avise que o contrato subiu.

- [ ] **Step 6: Preparar o commit**

```bash
git add backend/src
git commit -m "feat(backend): publica o contrato da 2a entrega com dados fixos"
```

---

## Grupo B — persistência

### Task 3: `Categoria` com `anotacoes[]` e CRUD de categoria

**Files:**
- Create: `model/Categoria.java`, `model/Anotacao.java`,
  `repository/CategoriaRepository.java`, `mapper/CategoriaMapper.java`,
  `service/CategoriaService.java`, `exception/CategoriaNotFoundException.java`
- Modify: `controller/CategoriaController.java` (tira os dados fixos)
- Test: `src/test/java/br/com/cesumar/aep/service/CategoriaServiceTest.java`

**Interfaces:**
- Consumes: DTOs da Tarefa 1, rotas da Tarefa 2.
- Produces:
  - `Categoria` — `@Document(collection = "categorias")`, campos `id`,
    `usuarioId`, `nome`, `descricao`, `cor`, `criadaEm`, `atualizadaEm`,
    `anotacoes` (`List<Anotacao>`, **inicializada vazia, nunca nula**).
  - `Anotacao` — classe simples (não `@Document`), campos `id`, `titulo`,
    `conteudo`, `criadaEm`, `atualizadaEm`.
  - `CategoriaRepository extends MongoRepository<Categoria, String>` com
    `List<Categoria> findByUsuarioId(String usuarioId)`.
  - `CategoriaService` — `listar(String usuarioId)`,
    `buscarPorId(String id, String usuarioId)`,
    `criar(CategoriaCreateRequest, String usuarioId)`,
    `atualizar(String id, CategoriaUpdateRequest, String usuarioId)`,
    `excluir(String id, String usuarioId)`.

O `usuarioId` entra como parâmetro **desde já**, mesmo sem Security. A Tarefa 9
só troca quem o fornece. Até lá, o controller passa a constante do usuário
semeado.

- [ ] **Step 1: Escrever os testes do Service**

```java
class CategoriaServiceTest {
    // criar deriva criadaEm/atualizadaEm do Clock e nasce com anotacoes vazia
    void criarDerivaDatasEIniciaListaVazia()
    // listar devolve totalAnotacoes = anotacoes.size(), não campo guardado
    void listarCalculaTotalDeAnotacoes()
    // buscarPorId de categoria de outro usuário lança CategoriaNotFoundException
    void buscarCategoriaDeOutroUsuarioFalhaComoNaoEncontrada()
    // atualizar mexe em atualizadaEm e preserva criadaEm e anotacoes
    void atualizarPreservaCriadaEmEAnotacoes()
}
```

Use `Clock.fixed` e `@Mock CategoriaRepository`, como `TarefaServiceTest` já faz.

- [ ] **Step 2: Rodar e confirmar que falham**

Run: `cd backend && ./mvnw test -Dtest=CategoriaServiceTest`
Expected: FAIL.

- [ ] **Step 3: Implementar model, repository, mapper e service**

`CategoriaMapper.toSummaryResponse` calcula `totalAnotacoes` de
`anotacoes.size()`. Nada de contador guardado — é a regra de campo derivado de
`docs/modelagem-banco.md`.

- [ ] **Step 4: Ligar o `CategoriaController` ao Service**

Remover a constante de dados fixos. Os testes MockMvc da Tarefa 2 passam a usar
`@MockitoBean CategoriaService`.

- [ ] **Step 5: Rodar tudo**

Run: `cd backend && ./mvnw clean test`
Expected: PASS.

- [ ] **Step 6: Preparar o commit**

```bash
git add backend/src
git commit -m "feat(backend): persiste categorias com anotacoes aninhadas"
```

---

### Task 4: Anotações dentro da categoria

**Files:**
- Modify: `service/CategoriaService.java`, `mapper/CategoriaMapper.java`,
  `controller/CategoriaController.java`
- Create: `exception/AnotacaoNotFoundException.java`
- Test: `src/test/java/br/com/cesumar/aep/service/CategoriaServiceAnotacaoTest.java`

**Interfaces:**
- Consumes: tudo da Tarefa 3.
- Produces: em `CategoriaService` —
  `criarAnotacao(String categoriaId, AnotacaoCreateRequest, String usuarioId)`,
  `atualizarAnotacao(String categoriaId, String anotacaoId, AnotacaoUpdateRequest, String usuarioId)`,
  `excluirAnotacao(String categoriaId, String anotacaoId, String usuarioId)`.

O id da anotação nasce no Service: `new ObjectId().toHexString()`. O Mongo não
gera id para elemento de array.

- [ ] **Step 1: Escrever os testes, incluindo os três do Review Focus**

```java
class CategoriaServiceAnotacaoTest {
    void criarAnotacaoGeraIdHexadecimalDe24Caracteres()
    void criarAnotacaoTocaAtualizadaEmDaCategoria()
    // Review Focus 2: id existe, mas noutra categoria
    void atualizarAnotacaoDeOutraCategoriaFalhaComoNaoEncontrada()
    // Review Focus 5: remover a última deixa lista vazia, não nula
    void excluirUltimaAnotacaoDeixaListaVaziaNaoNula()
}
```

Review Focus 1 (`conteudo` com 20 001 caracteres → 400) é teste de **controller**,
porque quem barra é o `@Size` do Bean Validation. Vai em
`CategoriaControllerTest.criarAnotacaoComConteudoAcimaDoLimiteDevolve400`.

- [ ] **Step 2: Rodar e confirmar que falham**

Run: `cd backend && ./mvnw test -Dtest='CategoriaServiceAnotacaoTest,CategoriaControllerTest'`
Expected: FAIL.

- [ ] **Step 3: Implementar os três métodos**

Buscar a anotação por id **dentro da categoria carregada**, e lançar
`AnotacaoNotFoundException` se não estiver lá. Nunca busque anotação por id sem
o `categoriaId` — é exatamente o caminho que acerta a anotação errada.

- [ ] **Step 4: Ligar as três rotas no controller**

- [ ] **Step 5: Rodar tudo**

Run: `cd backend && ./mvnw clean test`
Expected: PASS.

- [ ] **Step 6: Preparar o commit**

```bash
git add backend/src
git commit -m "feat(backend): adiciona CRUD de anotacoes dentro da categoria"
```

---

### Task 5: `usuarioId` e `categoriaId` em `Tarefa`, e desvinculação no delete

**Files:**
- Modify: `model/Tarefa.java`, `dto/TarefaCreateRequest.java`,
  `dto/TarefaUpdateRequest.java`, `dto/TarefaResponse.java`,
  `dto/TarefaSummaryResponse.java`, `mapper/TarefaMapper.java`,
  `service/TarefaService.java`, `service/CategoriaService.java`,
  `repository/TarefaRepository.java`
- Test: `src/test/java/br/com/cesumar/aep/service/TarefaServiceTest.java` (existente),
  `.../service/CategoriaServiceTest.java` (existente)

**Interfaces:**
- Consumes: `CategoriaRepository` da Tarefa 3.
- Produces: `Tarefa` com `usuarioId` e `categoriaId`; em `TarefaRepository`,
  `List<Tarefa> findByUsuarioId(String usuarioId)`;
  `TarefaService.listar(String usuarioId)` substituindo `listar()`.

`usuarioId` **não** aparece em nenhum `TarefaResponse`: é dado de dono, não de
tarefa. `categoriaId` aparece.

- [ ] **Step 1: Escrever os testes**

```java
// em TarefaServiceTest
void criarComCategoriaDeOutroUsuarioFalhaComoNaoEncontrada()   // 404, não 400
void atualizarComCategoriaIdNuloDesvinculaACategoria()         // PUT é substituição
void listarDevolveSoAsTarefasDoUsuario()

// em CategoriaServiceTest
void excluirCategoriaDesvinculaTarefasAntesDeApagar()           // ordem importa
```

O último teste verifica **a ordem**: `InOrder` do Mockito, `updateMany` antes do
`delete`. Se falhar no meio, sobra tarefa sem disciplina, nunca referência
pendurada.

- [ ] **Step 2: Rodar e confirmar que falham**

Run: `cd backend && ./mvnw test -Dtest='TarefaServiceTest,CategoriaServiceTest'`
Expected: FAIL.

- [ ] **Step 3: Implementar**

A desvinculação usa `MongoTemplate.updateMulti` com `Query` por `categoriaId`,
`Update.unset("categoriaId")`. `CategoriaService` recebe `MongoTemplate` no
construtor.

- [ ] **Step 4: Rodar tudo**

Run: `cd backend && ./mvnw clean test`
Expected: PASS.

- [ ] **Step 5: Preparar o commit**

```bash
git add backend/src
git commit -m "feat(backend): relaciona tarefas a usuario e categoria"
```

---

### Task 6: `banco/init.js` reescrito

**Files:**
- Modify: `banco/init.js`
- Modify: `docs/modelagem-banco.md` (seções "Índices" e "Como subir o banco")

**Interfaces:**
- Consumes: os campos dos models das Tarefas 3 e 5.
- Produces: o `usuarioId` do usuário semeado, que as Tarefas 3–5 usam como
  constante até a Tarefa 9. **Fixe o `_id` do usuário no script** (`ObjectId`
  literal) para a constante do código não mudar a cada seed.

Escrito **uma vez**, já com `usuarioId` obrigatório em `tarefas`. Ver a decisão
na spec.

- [ ] **Step 1: Reescrever o script**

Três `createCollection` com `$jsonSchema` e `additionalProperties: false`, no
mesmo estilo do atual. `anotacoes` como `array` de `object` com `required`.
Os três índices da spec. Seed: 1 usuário, 4 categorias (uma delas com
`anotacoes: []`, para a tela vazia aparecer na demo), ~6 anotações, ~8 tarefas —
algumas com `categoriaId`, pelo menos uma sem, uma concluída e uma atrasada.

`senhaHash` é hash BCrypt pré-computado, literal no script. Gere com
`new BCryptPasswordEncoder().encode(...)` num teste descartável ou no `jshell`, e
**anote a senha em claro** — ela vai para o README na Tarefa 12.

- [ ] **Step 2: Subir o banco do zero e conferir**

```bash
mongosh aep --eval 'db.dropDatabase()' && mongosh aep banco/init.js
```
Expected: três coleções criadas, `db.categorias.findOne()` mostra `anotacoes`
aninhadas, `db.tarefas.countDocuments({categoriaId: {$exists: true}}) > 0`.

- [ ] **Step 3: Confirmar que o validador barra documento inválido**

Run: `mongosh aep --eval 'db.tarefas.insertOne({titulo: "x"})'`
Expected: erro de validação. Se inserir, o `$jsonSchema` está frouxo.

- [ ] **Step 4: Atualizar `docs/modelagem-banco.md`**

A seção "Índices" deixa de dizer que não há índice. "Como subir o banco" passa a
documentar os dois caminhos: `docker compose down -v && up -d`, e `mongosh` para
quem roda Mongo nativo.

- [ ] **Step 5: Preparar o commit**

```bash
git add banco/init.js docs/modelagem-banco.md
git commit -m "feat(banco): cria tres colecoes com validadores e indices"
```

---

## Grupo C — segurança

### Task 7: `Usuario` e cadastro com BCrypt

**Files:**
- Create: `model/Usuario.java`, `repository/UsuarioRepository.java`,
  `mapper/UsuarioMapper.java`, `service/UsuarioService.java`,
  `exception/EmailJaCadastradoException.java`
- Modify: `controller/UsuarioController.java`,
  `exception/GlobalExceptionHandler.java`, `backend/pom.xml`
- Test: `src/test/java/br/com/cesumar/aep/service/UsuarioServiceTest.java`

**Interfaces:**
- Produces: `Usuario` (`id`, `nome`, `email`, `senhaHash`, `criadoEm`);
  `UsuarioRepository` com `Optional<Usuario> findByEmail(String email)` e
  `boolean existsByEmail(String email)`;
  `UsuarioService.criar(UsuarioCreateRequest) → UsuarioResponse`.

Adicionar `spring-boot-starter-security` ao `pom.xml`.
`EmailJaCadastradoException` → **409**, com handler próprio (não estende
`RecursoNaoEncontradoException`). `DuplicateKeyException` também → 409, como rede
de segurança: duas requisições simultâneas passam pelas duas checagens.

- [ ] **Step 1: Escrever os testes**

```java
class UsuarioServiceTest {
    void criarGuardaSenhaEmHashENuncaEmTextoPuro()      // senhaHash != senha enviada
    void criarNaoExpoeSenhaHashNaResposta()             // UsuarioResponse só tem id, nome, email
    // Review Focus 4
    void criarNormalizaEmailParaMinusculas()
    void criarComEmailJaCadastradoLancaEmailJaCadastrado()
}
```

- [ ] **Step 2: Rodar e confirmar que falham**

Run: `cd backend && ./mvnw test -Dtest=UsuarioServiceTest`
Expected: FAIL.

- [ ] **Step 3: Implementar**

`PasswordEncoder` injetado no construtor (o bean nasce na Tarefa 8). Normalizar
o e-mail com `toLowerCase(Locale.ROOT)` **na entrada**, antes de checar e de
gravar — normalizar só na gravação deixa a checagem furada.

- [ ] **Step 4: Rodar tudo**

Run: `cd backend && ./mvnw clean test`
Expected: PASS.

- [ ] **Step 5: Preparar o commit**

```bash
git add backend/pom.xml backend/src
git commit -m "feat(backend): adiciona cadastro de usuario com senha em hash"
```

---

### Task 8: Spring Security e sessão

**Files:**
- Create: `configuration/SecurityConfiguration.java`
- Modify: `controller/SessaoController.java`, `service/UsuarioService.java`
- Test: `src/test/java/br/com/cesumar/aep/controller/SessaoControllerTest.java`,
  `.../controller/TarefaControllerSegurancaTest.java`

**Interfaces:**
- Produces: beans `SecurityFilterChain` e `PasswordEncoder`;
  `UsuarioService implements UserDetailsService`.

`configuration/**` está fora do JaCoCo — a configuração não dilui a cobertura, e
por isso mesmo o comportamento dela precisa dos testes MockMvc abaixo.

Conforme a spec: públicos só `POST /api/usuarios`, `POST /api/sessao`, `/docs/**`,
`/v3/api-docs/**` e estáticos. **CSRF fica ligado**, com
`CookieCsrfTokenRepository.withHttpOnlyFalse()`. **Sem isenção para login e
cadastro** — o `GET /api/sessao` que o app faz ao abrir planta o cookie. **Sem
chave para desligar a Security.**

- [ ] **Step 1: Escrever os testes**

```java
class SessaoControllerTest {
    void loginComCredenciaisValidasDevolve200ComUsuario()
    void loginComSenhaErradaDevolve401()
    void getSessaoSemAutenticacaoDevolve401()
    void getSessaoPlantaOCookieDoTokenCsrf()      // XSRF-TOKEN presente na resposta
}

class TarefaControllerSegurancaTest {
    void listarSemAutenticacaoDevolve401()
    void postSemTokenCsrfDevolve403()
}
```

- [ ] **Step 2: Rodar e confirmar que falham**

Run: `cd backend && ./mvnw test -Dtest='SessaoControllerTest,TarefaControllerSegurancaTest'`
Expected: FAIL.

- [ ] **Step 3: Implementar `SecurityConfiguration` e o login**

- [ ] **Step 4: Rodar tudo**

Run: `cd backend && ./mvnw clean test`
Expected: PASS. Os testes MockMvc anteriores vão precisar de `@WithMockUser` ou
do token CSRF — ajuste-os, não afrouxe a configuração.

- [ ] **Step 5: Verificar o gate de cobertura**

Run: `cd backend && ./mvnw clean verify`
Expected: PASS, cobertura ≥ 70%. **Primeiro ponto do plano onde o gate pode
quebrar**, porque a Security traz código novo. Se quebrar, o teste que falta é de
Service, não de configuração.

- [ ] **Step 6: Preparar o commit**

```bash
git add backend/src
git commit -m "feat(backend): protege a api com spring security e sessao"
```

---

### Task 9: Pertencimento a partir da sessão

**Files:**
- Create: `service/UsuarioAtual.java`
- Modify: todos os controllers (tiram a constante, pedem o usuário da sessão)
- Test: `src/test/java/br/com/cesumar/aep/controller/CategoriaControllerTest.java`
  (acrescenta um caso)

**Interfaces:**
- Produces: `UsuarioAtual.id() → String`, lendo do `SecurityContextHolder`.

Única mudança de comportamento: a origem do `usuarioId` sai da constante e vai
para a sessão. As assinaturas dos Services não mudam — foi para isso que elas
levaram `usuarioId` desde a Tarefa 3.

- [ ] **Step 1: Escrever o teste**

```java
// em CategoriaControllerTest
void buscarCategoriaDeOutroUsuarioDevolve404NaoE403()
```

404 e não 403: 403 confirmaria que o recurso existe.

- [ ] **Step 2: Rodar e confirmar que falha**

Run: `cd backend && ./mvnw test -Dtest=CategoriaControllerTest`
Expected: FAIL.

- [ ] **Step 3: Implementar `UsuarioAtual` e remover as constantes**

Grepe por constante de usuário semeado antes de dizer que terminou:
`grep -rn "usuarioDemo\|USUARIO_FIXO" backend/src/main`. Zero resultados.

- [ ] **Step 4: Rodar tudo**

Run: `cd backend && ./mvnw clean verify`
Expected: PASS.

- [ ] **Step 5: Preparar o commit**

```bash
git add backend/src
git commit -m "feat(backend): deriva o dono dos recursos da sessao autenticada"
```

---

## Grupo D — agregação

### Task 10: `GET /api/painel`

**Files:**
- Create: `repository/PainelRepository.java`, `service/PainelService.java`
- Modify: `controller/PainelController.java`
- Test: `src/test/java/br/com/cesumar/aep/repository/PainelRepositoryTest.java`,
  `.../service/PainelServiceTest.java`

**Interfaces:**
- Produces: `PainelRepository.montarPipeline(String usuarioId, LocalDate hoje) → Aggregation`
  e `PainelRepository.agregar(String usuarioId, LocalDate hoje) → PainelResponse`.

**Esta separação é obrigatória, não estilo.** `montarPipeline` é testável sem
Mongo e entra na cobertura do `./mvnw test`; `agregar` tem duas linhas e só é
exercitado pelo `*IT`. Juntos numa única classe, o pipeline inteiro ficaria no
denominador do JaCoCo sem estar no numerador — o endpoint que reforça o critério
de NoSQL derrubaria o de cobertura.

Definições fechadas na spec: atrasada é não concluída com `prazo` anterior a
hoje; tarefa sem `categoriaId` vai num balde `categoriaId: null`,
`nome: "Sem disciplina"`; categoria sem tarefa aparece com `total: 0`,
`percentual: 0`; `percentual` é inteiro arredondado no backend.

- [ ] **Step 1: Escrever os testes**

```java
class PainelRepositoryTest {
    // sem Mongo: afirma sobre os estágios do Aggregation montado
    void pipelineFiltraPeloUsuarioNoPrimeiroEstagio()
    void pipelineUsaLookupEntreCategoriasETarefas()
}

class PainelServiceTest {
    void painelSomaPendentesConcluidasEAtrasadas()
    // Review Focus 3: fronteira do "<"
    void tarefaComPrazoHojeNaoContaComoAtrasada()
    void tarefaSemCategoriaVaiParaOBaldeSemDisciplina()
    void categoriaSemTarefaApareceComPercentualZero()
    void percentualVemArredondadoParaInteiro()
}
```

- [ ] **Step 2: Rodar e confirmar que falham**

Run: `cd backend && ./mvnw test -Dtest='PainelRepositoryTest,PainelServiceTest'`
Expected: FAIL.

- [ ] **Step 3: Implementar**

`hoje` vem de `LocalDate.now(clock)` no Service, e desce como parâmetro — nunca
de `LocalDate.now()` dentro do repositório, ou o teste da fronteira do `<` passa
a depender do dia em que roda.

- [ ] **Step 4: Ligar o controller e rodar tudo**

Run: `cd backend && ./mvnw clean verify`
Expected: PASS.

- [ ] **Step 5: Preparar o commit**

```bash
git add backend/src
git commit -m "feat(backend): agrega o painel de progresso por disciplina"
```

---

### Task 11: Testes de integração com Testcontainers

**Files:**
- Create: `src/test/java/br/com/cesumar/aep/PainelAgregacaoIT.java`,
  `.../ValidadorMongoIT.java`

**Interfaces:**
- Consumes: `PainelRepository.agregar` da Tarefa 10.

Evidência adicional, fora dos 70%. `*IT` não roda em `./mvnw clean test`.

- [ ] **Step 1: Escrever os dois ITs**

`PainelAgregacaoIT` semeia duas categorias e cinco tarefas num Mongo de container
e afirma sobre o `PainelResponse` — é a prova de que o pipeline roda de verdade.
`ValidadorMongoIT` aplica o `$jsonSchema` do `init.js` e confirma que inserir
tarefa sem `usuarioId` falha.

- [ ] **Step 2: Rodar com Docker ligado**

Run: `cd backend && ./mvnw verify -Pintegration` (ou o perfil que o projeto usa)
Expected: PASS.

- [ ] **Step 3: Confirmar que o build sem Docker continua passando**

Run: `cd backend && ./mvnw clean test`
Expected: PASS, sem tentar subir container. **Este passo é a Definition of Done
do `AGENTS.md`**; se os ITs rodarem no `test`, o requisito quebrou.

- [ ] **Step 4: Preparar o commit**

```bash
git add backend/src/test
git commit -m "test(backend): cobre a agregacao e o validador contra mongo real"
```

---

## Grupo E — entrega

### Task 12: Documentação, README e tag

**Files:**
- Modify: `README.md`, `docs/segunda-entrega.md`, `docs/arquitetura.md`
- Modify: `backend/README.md` se existir

- [ ] **Step 1: Tirar do README a frase que encolhe o escopo**

A frase "A PoC avaliada neste marco é a **API REST**" sai. Ela declara por
escrito que o projeto é só backend, o que é exatamente a leitura que custou nota.

- [ ] **Step 2: Atualizar o README**

Três coleções e o relacionamento; as credenciais de demonstração do seed; os dois
caminhos para subir o banco; os limites conhecidos da spec (aninhamento sem teto,
última escrita vence, exclusão não atômica, sessão em memória, senha de demo
versionada).

- [ ] **Step 3: Rodar a verificação completa**

Run: `cd backend && ./mvnw clean verify`
Expected: PASS, cobertura ≥ 70%. Guarde o número — ele vai para o vídeo.

- [ ] **Step 4: Preparar o commit e a tag**

```bash
git add README.md docs
git commit -m "docs: documenta a segunda entrega e remove o escopo encolhido"
```

- [ ] **Step 5: Fechar o fluxo de branches**

O último PR de `samuel-backend` para `dev` entra, e só então `dev` vai para
`main`. A tag sai na `main` depois do merge, e **é passo manual de quem
entrega** — a seção 9 do enunciado pede que ela identifique a versão final:

```bash
git tag -a v2.0-entrega2 -m "Segunda entrega da AEP - 6o semestre"
```

Conferir antes de taguear: `main` tem o trabalho dos dois, e
`./mvnw clean verify` passa **na `main`**, não só na branch de trabalho.

---

## Fora deste plano

- Cards 6, 7, 12, 13 e 14 do Trello, que são do Felipe. O que este plano entrega
  para ele é o contrato da Tarefa 2, no dia 1.
- A sanitização do `conteudo` com DOMPurify, que é do card 12. O backend guarda o
  HTML limitado em tamanho; quem renderiza sanitiza.
- Os cards que faltam no quadro — vídeo, documentação e tag. O quadro precisa
  deles para o critério de metodologia, e eles não existem.
- A gravação do vídeo, com o roteiro na spec.
