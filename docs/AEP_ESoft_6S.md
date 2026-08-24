# AEP 2026.2 — 6S — Engenharia de Software

> Atividade de Estudo Programada – 2026.2 – 6S
>
> Versão em Markdown do documento original `AEP_ESoft_6S.pdf`.

---

## 1. Contextualização

A Atividade de Estudo Programada (AEP) é um trabalho interdisciplinar obrigatório do curso de Engenharia de Software, desenvolvido em equipes de até três integrantes, com o objetivo de integrar os conhecimentos adquiridos nas disciplinas do semestre por meio do desenvolvimento incremental de uma solução computacional.

Cada equipe deverá desenvolver uma **Prova de Conceito (PoC)** alinhada a um dos **17 Objetivos de Desenvolvimento Sustentável (ODS)** da Organização das Nações Unidas (ONU), demonstrando como a tecnologia pode contribuir para a solução de um problema real da sociedade.

## 2. Disciplinas Envolvidas

- Banco de Dados NoSQL
- Paradigmas de Linguagem
- Processo de Software
- Projeto, Implementação e Testes

## 3. Objetivo

Desenvolver uma Prova de Conceito (PoC) utilizando banco de dados NoSQL e programação orientada a objetos, aplicando práticas de engenharia de software, versionamento, documentação e testes automatizados.

## 4. Organização da AEP

A atividade será composta por **duas entregas obrigatórias**. A avaliação considerará a evolução incremental da solução entre as entregas, a aderência aos requisitos técnicos e as evidências disponíveis no repositório GitHub, na documentação e nos vídeos de demonstração.

---

## 5. Primeira Entrega — 1,0 ponto

### Objetivo

Apresentar a proposta da solução e disponibilizar a primeira versão funcional da PoC.

### Entregáveis

- Repositório GitHub criado e contendo a primeira versão funcional da PoC.
- README inicial com problema, ODS, tecnologias e instruções básicas de execução.
- Testes automatizados executáveis, com cobertura mínima obrigatória de **70%** sobre o código da PoC entregue.
- Vídeo de demonstração com duração de **2 a 3 minutos**.

### Critérios de avaliação

| Critério | Evidência esperada para correção | Pontos |
|---|---|---|
| Problema e alinhamento ao ODS | Problema claramente definido, público/contexto identificável e relação objetiva com o ODS escolhido. | 0,1 |
| Primeira versão funcional da PoC | A solução executa o fluxo principal proposto e permite demonstrar funcionalidade real, ainda que parcial. | 0,1 |
| Banco de dados NoSQL | Uso efetivo de NoSQL e atendimento aos requisitos previstos para o semestre. | 0,1 |
| Programação orientada a objetos e organização do código | Uso coerente de classes/objetos, responsabilidades compreensíveis e estrutura de projeto organizada. | 0,1 |
| GitHub e versionamento | Repositório acessível, código versionado, histórico de commits e estrutura mínima organizada. | 0,1 |
| Testes automatizados | Testes relevantes, executáveis e coerentes com as funcionalidades implementadas. | 0,1 |
| Cobertura de testes ≥ 70% | Relatório ou comando reproduzível evidencia cobertura mínima de 70%. **Abaixo de 70%, este item recebe 0 ponto.** | 0,1 |
| Vídeo de demonstração | Apresenta problema, ODS, arquitetura inicial e execução da PoC dentro do tempo previsto. | 0,3 |

---

## 6. Segunda Entrega — 1,0 ponto

### Objetivo

Concluir a Prova de Conceito, evidenciando a evolução da solução, a qualidade técnica, os testes automatizados e a documentação necessária para execução e avaliação do projeto.

### Entregáveis

- Código-fonte atualizado no GitHub.
- Documentação técnica completa do projeto.
- Testes automatizados executáveis, com cobertura mínima obrigatória de **70%**.
- Vídeo de demonstração com duração de **3 a 5 minutos**.

### Critérios de avaliação

| Critério | Evidência esperada para correção | Pontos |
|---|---|---|
| PoC funcional e evolução | Fluxos principais executam corretamente e há evolução verificável em relação à primeira entrega. | 0,1 |
| Banco de dados NoSQL | Modelagem e uso do banco atendem integralmente aos requisitos do semestre e às necessidades da PoC. | 0,1 |
| Programação orientada a objetos e qualidade do código | Estrutura coerente, responsabilidades bem definidas, legibilidade e ausência de duplicações ou acoplamentos desnecessários relevantes. | 0,1 |
| Metodologia de trabalho utilizada | Quadro de tarefas. | 0,1 |
| Testes automatizados | Conjunto de testes pertinente, executável e cobrindo comportamentos relevantes da solução. | 0,1 |
| Cobertura de testes ≥ 70% | Relatório ou comando reproduzível evidencia cobertura mínima de 70%. **Abaixo de 70%, este item recebe 0 ponto.** | 0,1 |
| Documentação técnica | README e documentação permitem compreender, instalar, executar e testar a PoC, incluindo tecnologias e estrutura do banco. | 0,1 |
| GitHub e versionamento | Repositório organizado, histórico compatível com a evolução do trabalho e versão final claramente identificável. | 0,1 |
| Vídeo de demonstração | Apresenta evolução, arquitetura final, execução da PoC, tecnologias, testes e principais resultados, dentro do tempo previsto. | 0,2 |

---

## 7. Requisitos Técnicos Obrigatórios

A PoC deverá atender a **todos** os requisitos abaixo.

| Área | Requisito obrigatório |
|---|---|
| Banco de dados | Utilização efetiva de banco de dados NoSQL. |
| Programação | Desenvolvimento utilizando linguagem com suporte a programação orientada a objetos e aplicação efetiva do paradigma. |
| Versionamento | Código-fonte versionado em repositório GitHub acessível para avaliação. |
| Testes | Implementação de testes automatizados executáveis. |
| Cobertura | Cobertura mínima obrigatória de 70% em cada entrega, calculada sobre o código da PoC apresentado naquele marco. A equipe deverá disponibilizar evidência reproduzível da medição. |
| Documentação | Documentação técnica suficiente para compreender, instalar, executar e testar a solução. |
| Funcionalidade | PoC executável e compatível com os requisitos previstos para o semestre. |

---

## 8. Evolução por Semestre

### 1º Semestre — corresponde à **1ª entrega (1º bimestre)**

A PoC deverá utilizar:

- Uma única coleção NoSQL.
- Objetos homogêneos, com estrutura simples.
- Operações básicas de CRUD.

Exemplo de documento:

```json
{
  "nome": "João",
  "idade": 20,
  "curso": "Engenharia de Software"
}
```

### 2º Semestre — corresponde à **2ª entrega (2º bimestre)**

A PoC deverá evoluir para:

- Múltiplas coleções.
- Relacionamento entre coleções.
- Pelo menos uma coleção contendo objetos complexos, como documentos aninhados ou listas de subdocumentos.

Exemplo de documento:

```json
{
  "nome": "João",
  "email": "joao@email.com",
  "enderecos": [
    {
      "rua": "Rua A",
      "cidade": "Maringá",
      "estado": "PR"
    }
  ]
}
```

> **Interpretação adotada pela equipe:** os rótulos "1º Semestre" e "2º Semestre" do documento original referem-se aos **dois bimestres da disciplina**, ou seja, às duas entregas da AEP — e não aos semestres do curso. Portanto:
>
> - **1ª entrega (1º bimestre):** uma única coleção, objetos homogêneos e simples, CRUD básico.
> - **2ª entrega (2º bimestre):** múltiplas coleções, relacionamento entre elas e pelo menos uma coleção com objetos complexos (documentos aninhados ou listas de subdocumentos).
>
> Essa leitura é coerente com o critério "evolução verificável em relação à primeira entrega" da seção 6. Vale confirmar com o professor antes de fechar a modelagem.

---

## 9. Organização do Repositório GitHub

O repositório deverá conter, no mínimo:

- `README.md`
- Código-fonte da PoC
- Documentação técnica
- Testes automatizados
- Instrução ou evidência reproduzível para geração do relatório de cobertura
- Identificação clara da versão correspondente a cada entrega, por tag, release ou commit informado pela equipe

---

## 10. Vídeos de Demonstração

### Primeira Entrega — 2 a 3 minutos

O vídeo deverá apresentar:

- O problema escolhido
- O ODS atendido
- O projeto inicial da solução
- A primeira versão funcional da PoC

### Segunda Entrega — 3 a 5 minutos

O vídeo deverá apresentar:

- A evolução da solução
- O projeto final
- A demonstração da PoC
- As tecnologias utilizadas
- Os testes automatizados e a cobertura obtida
- Os principais resultados alcançados

---

## 11. Modelo de Entrega

### IDENTIFICAÇÃO — 6S

| Campo | Valor |
|---|---|
| Curso | |
| Série | |

#### Acadêmicos

| RA | Nome |
|---|---|
| | |
| | |
| | |

| Item | Conteúdo |
|---|---|
| TÍTULO do PoC | |
| LINK DO VÍDEO (YouTube) | |
| LINK DO GITHUB | |
