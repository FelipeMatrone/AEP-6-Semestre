# Padrões de código e de testes — backend

Especificação que o código do backend deve seguir. Vale para qualquer pessoa e
qualquer ferramenta que escreva neste repositório.

Este arquivo é lido **antes** de escrever código. O `docs/checklist-revisao.md`
é lido **depois**, antes de abrir PR. São momentos diferentes, por isso são dois
arquivos.

> Onde ele mora e por quê: markdown comum em `docs/`, não em `.agents/skills/`
> nem em `.claude/skills/`. Aqueles diretórios são formatos proprietários de
> ferramenta — o primeiro é layout do Codex, o segundo do Claude Code — e nenhum
> dos dois é visível para quem abre o repositório no GitHub. O professor precisa
> conseguir validar a convenção lendo o repositório, e nós precisamos que ela
> valha para as duas ferramentas em uso. Um markdown em `docs/` atende os três.

---

## 1. Estrutura de classes

### Pacotes

Package base: `br.com.cesumar.aep`. Abaixo dele, um pacote por responsabilidade:

```
model         documentos persistidos (@Document) e enums do domínio
repository    interfaces Spring Data, sem implementação própria
dto           contratos HTTP de entrada e saída
mapper        conversão explícita entre DTO e Model
service       casos de uso e regras de negócio
controller    tradução HTTP ↔ caso de uso
exception     exceções de domínio e tratamento centralizado
configuration infraestrutura e dados de desenvolvimento
```

Não criar pacote por funcionalidade (`tarefa/`, `usuario/`) — o domínio é
pequeno demais, e o critério de nota é "responsabilidades compreensíveis", que
o corte por camada torna visível na própria árvore de diretórios.

### Nomes

Derivados do domínio, sem exceção. Para o domínio `Tarefa`:

| Camada | Arquivo | Forma |
|---|---|---|
| Model | `model/Tarefa` | classe |
| Enum | `model/Prioridade` | enum |
| Repository | `repository/TarefaRepository` | interface, corpo vazio |
| DTO de criação | `dto/TarefaCreateRequest` | record |
| DTO de atualização | `dto/TarefaUpdateRequest` | record |
| DTO de resposta | `dto/TarefaResponse` | record |
| DTO de listagem | `dto/TarefaSummaryResponse` | record |
| Mapper | `mapper/TarefaMapper` | classe `@Component` |
| Service | `service/TarefaService` | classe `@Service` |
| Controller | `controller/TarefaController` | classe `@RestController` |
| Exceção | `exception/TarefaNotFoundException` | classe |

Campos e métodos em português, seguindo o domínio (`titulo`, `prazo`,
`buscarPorId`). Sufixos técnicos em inglês (`Request`, `Response`, `Mapper`,
`Service`) porque são vocabulário do framework, não do domínio.

### Ordem dos membros dentro da classe

Fixa, para que dois arquivos diferentes se leiam igual:

1. constantes (`private static final`);
2. campos `final` injetados;
3. construtor;
4. métodos públicos na ordem do CRUD: `listar`, `buscarPorId`, `criar`,
   `atualizar`, `excluir`;
5. métodos privados de apoio, no fim.

### Responsabilidade de cada camada

| Camada | Faz | Não faz |
|---|---|---|
| Controller | valida com `@Valid`, define status e header `Location`, delega | regra de negócio, `try/catch` de fluxo normal, acesso ao Repository |
| DTO | representa **um** caso de uso; Jakarta Validation nos Requests | ser reaproveitado entre criação, atualização e resposta |
| Mapper | traduz campos **vindos do cliente**, preserva o id no update | acessar Repository, calcular campo derivado |
| Service | casos de uso, regras, campos derivados, exceções de domínio | conhecer HTTP |
| Repository | persistência e consulta | qualquer regra |
| Model | representa o documento | atravessar a fronteira HTTP |

Regra que resolve a fronteira Mapper/Service, que é onde dois autores divergem:
**o Mapper só traduz o que veio do cliente; tudo que é derivado nasce no
Service.** `concluida = false` na criação, `criadaEm`, `atualizadaEm` — Service.

### Proibições

- Lombok.
- MapStruct ou qualquer framework de mapping.
- Interface `Service`/`ServiceImpl` ou `Mapper`/`MapperImpl` sem um problema
  concreto que a justifique.
- Injeção em campo (`@Autowired` em atributo). Injeção é por construtor, e o
  construtor não leva `@Autowired`.
- `@Document` recebido ou devolvido por Controller.
- Comentário que repete o código. Comentário explica decisão, não sintaxe.
- Marca de ferramenta em comentário (`claude:`, `ponytail:`). `TODO(nome):` é
  permitido — identifica pessoa, não ferramenta.

---

## 2. Estrutura de testes

### Três tipos, e só três

| Tipo | Arquivo | Anotações | Executor | Responsabilidade exclusiva |
|---|---|---|---|---|
| Unitário | `service/TarefaServiceTest` | `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks` | surefire — `./mvnw test` | regra de negócio |
| Contrato HTTP | `controller/TarefaControllerTest` | `@WebMvcTest(TarefaController.class)`, `@MockitoBean` | surefire — `./mvnw test` | status, JSON, validação |
| Integração | `integration/TarefaApiIT` | `@Testcontainers`, `@SpringBootTest`, `@AutoConfigureMockMvc`, `@Container static`, `@DynamicPropertySource` | failsafe — `./mvnw verify` | ida e volta contra Mongo real |

O pacote do teste espelha o pacote da classe testada. O teste de integração vive
em `integration/` porque testa o conjunto, não uma classe.

### O sufixo é contrato de execução

`*Test` roda no **surefire** (`test`). `*IT` roda no **failsafe** (`verify`).
Errar o sufixo faz o teste de integração rodar duas vezes ou não rodar nenhuma —
e "não rodar" passa despercebido, porque o build fica verde do mesmo jeito.

### Nomenclatura

O nome do método é o comportamento esperado, em português, sem prefixo `test` e
sem `_`:

```java
void deveListarTarefasResumidas()
void deveLancarExcecaoAoBuscarTarefaInexistente()
void deveRetornarNotFoundAoExcluirTarefaInexistente()
```

Um comportamento por teste.

### Organização interna do arquivo

- `private static final String BASE_PATH = "/api/tarefas";` no topo dos testes
  HTTP. A rota aparece uma vez só.
- Arrange / Act / Assert separados por **linha em branco**, nunca por comentário
  `// arrange`.
- Fábricas privadas no fim do arquivo (`private Tarefa tarefa(...)`,
  `private TarefaResponse response(...)`). É o que mantém cada teste com cinco
  linhas legíveis em vez de trinta de montagem.

### Onde cada regra é testada

Cada comportamento é verificado em **exatamente uma** camada:

- regra de negócio → só `TarefaServiceTest`;
- status HTTP, forma do JSON, validação → só `TarefaControllerTest`;
- persistência de verdade → só `TarefaApiIT`.

Duplicar a mesma regra em duas camadas é o que faz a suíte doer a cada refactor,
e é uma das coisas que a revisão procura.

### Matriz mínima por domínio

**Service**

- listar mapeia para o resumo;
- buscar existente devolve a representação completa;
- buscar inexistente lança `TarefaNotFoundException`;
- criar força `concluida = false` e carimba `criadaEm` e `atualizadaEm`;
- atualizar preserva o id persistido e mexe em `atualizadaEm`;
- excluir inexistente lança e **não** chama `delete`.

**Controller**

- `200` na listagem;
- `200` na busca individual, com a representação completa;
- `201` com header `Location` e corpo completo;
- `400` com o mapa `fieldErrors` para corpo inválido;
- `404` para recurso ausente;
- `204` no delete, sem corpo.

**Integração** — um fluxo só: criar → buscar → listar → atualizar → excluir.

### A projeção se prova pela ausência

`TarefaSummaryResponse` só está testado de verdade quando o teste afirma que os
campos fora do resumo **não** vieram:

```java
.andExpect(jsonPath("$[0].titulo").value("Entregar a modelagem"))
.andExpect(jsonPath("$[0].prioridade").value("media"))
.andExpect(jsonPath("$[0].observacao").value("Observação de teste"))
```

Sem o `doesNotExist`, um `TarefaResponse` devolvido por engano na listagem passa
verde.

### Reprodutibilidade

- Sem estado mutável compartilhado entre testes.
- Sem `@Order` e sem dependência de ordem de execução.
- Sem id fixo gerado pelo banco.
- Sem espera arbitrária (`Thread.sleep`).
- `repository.deleteAll()` no `@BeforeEach` do teste de integração.
- O teste de integração não depende do MongoDB instalado na máquina nem do
  `docker-compose.yml` de desenvolvimento — ele sobe o próprio contêiner, na
  mesma versão de imagem do Compose.

> O contêiner do Testcontainers sobe um Mongo **limpo**: ele não executa
> `banco/init.js`, logo não tem o `$jsonSchema` nem os índices. O teste de
> integração prova o CRUD, **não** prova que a aplicação satisfaz o validador do
> banco de desenvolvimento. Essa verificação é manual e está em
> `backend-tarefas.md`.

### Cobertura

Gate de 70% de linha no `./mvnw verify`, via JaCoCo, excluindo a classe
`Application` e o pacote `configuration`.

Restrição que vem junto: **`TarefaServiceTest` e `TarefaControllerTest`
sozinhos já precisam passar de 70%.** O teste de integração exige Docker; se ele
for a perna que sustenta o número, qualquer máquina sem Docker perde o item de
cobertura — que é eliminatório. O `*IT` é evidência adicional, não a base do
cálculo.

---

## 3. Como verificar

```bash
./mvnw clean test      # unitários e de contrato; não exige Docker
./mvnw clean verify    # tudo, incluindo integração e o gate de cobertura
```

Relatório em `target/site/jacoco/index.html`.

Antes de considerar uma tarefa concluída: o projeto compila, os testes passam,
existe teste novo para comportamento novo, os contratos da API continuam
válidos, a documentação afetada foi conferida, e qualquer limitação de ambiente
(Docker ausente, teste não executado) foi relatada explicitamente em vez de
silenciada.
