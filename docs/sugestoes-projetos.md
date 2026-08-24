# Sugestões de Projeto — AEP 2026.2 (6S)

Levantamento de ideias de PoC alinhadas aos Objetivos de Desenvolvimento Sustentável (ODS) da ONU, para discussão do grupo.

Cada sugestão descreve **o problema real**, **o que a PoC faz** e **como isso resolve o problema**. A modelagem das coleções, a escolha de linguagem e as decisões de arquitetura ficam a critério do grupo.

**Índice**

1. [Rede de resgate de excedente alimentar — ODS 2](#1-rede-de-resgate-de-excedente-alimentar--ods-2-fome-zero)
2. [Mapa colaborativo de problemas urbanos — ODS 11](#2-mapa-colaborativo-de-problemas-urbanos--ods-11-cidades-e-comunidades-sustentáveis)
3. [Pontos de coleta e logística reversa — ODS 12](#3-pontos-de-coleta-e-logística-reversa--ods-12-consumo-e-produção-responsáveis)
4. [Acompanhamento de adesão a tratamentos de saúde — ODS 3](#4-acompanhamento-de-adesão-a-tratamentos-de-saúde--ods-3-saúde-e-bem-estar)
5. [Mentoria e reforço escolar voluntário — ODS 4](#5-mentoria-e-reforço-escolar-voluntário--ods-4-educação-de-qualidade)
6. [Inventário de pegada de carbono para pequenas empresas — ODS 13](#6-inventário-de-pegada-de-carbono-para-pequenas-empresas--ods-13-ação-contra-a-mudança-global-do-clima)
7. [Rede social escolar — avaliação de viabilidade](#7-rede-social-escolar--avaliação-de-viabilidade)

---

## 1. Rede de resgate de excedente alimentar — ODS 2 (Fome Zero)

**Problema**

Restaurantes, padarias e mercados descartam diariamente alimento ainda próprio para consumo, enquanto ONGs e cozinhas comunitárias a poucos quilômetros têm demanda não atendida. A ponte entre os dois lados hoje é feita por telefone e grupos de WhatsApp — sem registro, sem previsibilidade e sem ninguém conseguindo medir o desperdício evitado.

**O que a PoC faz**

O estabelecimento publica o que sobrou e até quando é seguro consumir. As ONGs cadastradas visualizam o que está disponível e reservam. O sistema acompanha o lote até a retirada e consolida quanto foi resgatado por período.

**Como resolve**

Transforma um combinado informal em um fluxo rastreável, reduz a perda por validade vencida durante a negociação e gera o indicador que hoje não existe: quilos de alimento desviados do lixo.

---

## 2. Mapa colaborativo de problemas urbanos — ODS 11 (Cidades e Comunidades Sustentáveis)

**Problema**

Buraco na via, alagamento recorrente, poste apagado, descarte irregular de entulho. O cidadão reclama em canais dispersos — ouvidoria, redes sociais, telefone — e a prefeitura recebe tudo desagregado, sem enxergar que o mesmo ponto foi reportado quinze vezes no último ano.

**O que a PoC faz**

O morador registra a ocorrência com foto, categoria e localização. Outros moradores confirmam a mesma ocorrência. A gestão pública vê o mapa com os pontos mais reincidentes e o tempo médio até a resolução.

**Como resolve**

Converte reclamação isolada em evidência agregada, dá prioridade objetiva a quem decide onde alocar equipe e devolve transparência ao cidadão sobre o que virou obra e o que ficou parado.

---

## 3. Pontos de coleta e logística reversa — ODS 12 (Consumo e Produção Responsáveis)

**Problema**

Quase ninguém sabe onde descartar corretamente eletrônico velho, pilha, óleo de cozinha usado ou medicamento vencido. O material acaba no lixo comum ou no ralo. Do outro lado, os pontos de coleta que existem são pouco divulgados e não têm controle do volume que recebem.

**O que a PoC faz**

Catálogo de pontos de coleta filtrável por tipo de resíduo e proximidade, com horário de funcionamento e registro do que foi entregue. O ponto acompanha o volume acumulado por categoria.

**Como resolve**

Ataca os dois gargalos ao mesmo tempo: a falta de informação de quem quer descartar certo e a falta de dado de quem recebe. O volume registrado vira argumento para o ponto negociar coleta com as recicladoras.

---

## 4. Acompanhamento de adesão a tratamentos de saúde — ODS 3 (Saúde e Bem-Estar)

**Problema**

O abandono de tratamento contínuo — hipertensão, diabetes, tuberculose — é altíssimo na atenção básica. A UBS costuma descobrir que o paciente parou de tomar a medicação só quando ele reaparece com o quadro agravado, meses depois, num atendimento que custa muito mais caro.

**O que a PoC faz**

Registra o plano terapêutico do paciente com cronograma de doses e retornos. O paciente, ou o agente comunitário de saúde, confirma a adesão. O sistema sinaliza para a equipe quem está em risco de abandono antes que ele aconteça.

**Como resolve**

Troca a lógica reativa pela busca ativa. A equipe deixa de esperar o paciente sumir e passa a receber uma lista priorizada de quem procurar nesta semana.

> **Cuidado:** usar exclusivamente dados fictícios na PoC. Nenhuma informação real de paciente.

---

## 5. Mentoria e reforço escolar voluntário — ODS 4 (Educação de Qualidade)

**Problema**

Estudantes da rede pública com defasagem concentrada em disciplinas específicas, e universitários dispostos a ensinar sem saber onde ajudar. Quando o encontro acontece, é por acaso ou por conhecido em comum — não existe mecanismo estruturado, e a maior parte do voluntariado disponível simplesmente não é aproveitada.

**O que a PoC faz**

Cruza a necessidade do aluno (disciplina, série, horário disponível) com a competência e a agenda do voluntário, sugere as combinações viáveis, agenda as sessões e registra o progresso ao longo do tempo.

**Como resolve**

Reduz o atrito de encontrar a pessoa certa, que é o que faz a maior parte da boa vontade se perder. E o histórico de sessões mostra à escola se a mentoria está de fato movendo a nota.

---

## 6. Inventário de pegada de carbono para pequenas empresas — ODS 13 (Ação contra a Mudança Global do Clima)

**Problema**

Empresas pequenas começam a receber exigência de dado ambiental de clientes maiores e de editais, mas as ferramentas de inventário no mercado são caras, complexas ou exigem consultoria. Resultado: a empresa não mede, e sem medir não tem como reduzir nem comprovar nada.

**O que a PoC faz**

A empresa lança os consumos do mês — energia, combustível da frota, viagens, resíduos. O sistema aplica os fatores de emissão e devolve o inventário organizado, com a evolução mês a mês e a identificação da maior fonte.

**Como resolve**

Entrega o essencial do inventário sem consultoria, num formato que a empresa consegue apresentar. E mostrar qual fonte concentra a emissão é o que transforma o número em decisão prática.

---

## 7. Rede social escolar — avaliação de viabilidade

### O veredito curto

**É viável e tem ótimo encaixe no ODS 4 — mas só com um recorte estreito.** "Rede social para escolas" no sentido genérico (perfil, feed, amigos, curtidas, mensagens) é a proposta de maior risco da lista, por dois motivos concretos ligados aos critérios de avaliação da AEP.

### Por que a versão genérica é arriscada

**O critério de problema fica fraco.** A AEP pede "problema claramente definido, público/contexto identificável e relação objetiva com o ODS escolhido". "Uma rede social para escolas" descreve uma *solução*, não um problema — e a pergunta natural do avaliador é: qual dor exatamente isso resolve que o WhatsApp e o Google Classroom já não resolvem? Sem uma resposta nítida, o item perde ponto.

**O escopo trabalha contra a cobertura de testes.** Rede social genérica é muita tela e pouca regra de negócio: cadastro, publicação, comentário, curtida, seguir, notificar. É CRUD largo e raso. Como a cobertura mínima de 70% é obrigatória e zera o item se não for atingida, um projeto com muitas funcionalidades superficiais obriga a escrever muito teste sem substância. Os projetos com lógica concentrada — cálculo, classificação, casamento entre partes — atingem 70% com bem menos esforço.

**Risco secundário:** rede social envolvendo menores levanta questões reais de moderação e privacidade. Numa PoC acadêmica isso não inviabiliza nada, mas é bom citar no vídeo como limitação conhecida, em vez de deixar o avaliador levantar a questão.

### Os recortes que funcionam

O problema de comunicação escolar é real e bem documentado — o que precisa mudar é o enquadramento. Três recortes viáveis, do mais forte para o mais fraco:

#### Recorte A — Rede de colaboração entre professores *(recomendado)*

**Problema:** professores da rede pública refazem, sozinhos, o mesmo plano de aula que um colega da escola vizinha já preparou e testou. Não existe memória compartilhada: o material fica no pen drive, no e-mail pessoal, e some quando o professor troca de escola. O tempo gasto reinventando plano é tempo tirado do atendimento ao aluno.

**O que a PoC faz:** professores publicam planos de aula e materiais marcados por disciplina, série e habilidade da BNCC. Outros professores buscam, aplicam em sala e devolvem uma avaliação de como funcionou na prática. O que dá certo ganha visibilidade; o autor acumula reputação.

**Como resolve:** transforma esforço individual em acervo coletivo e usa o retorno de quem aplicou como filtro de qualidade — o que separa isso de um repositório de arquivos qualquer.

**Por que é o mais forte:** o problema se explica em uma frase, o público é identificável, o vínculo com o ODS 4 é direto, e a lógica de busca, ranqueamento e reputação dá substância real para os testes automatizados.

#### Recorte B — Mural de comunicação escola–família

**Problema:** o comunicado da escola se perde entre o grupo de WhatsApp da turma, o bilhete na agenda e o e-mail que ninguém abre. A escola não sabe quem leu, a família descobre a reunião no dia seguinte, e o aluno que mais precisa de acompanhamento é justamente o de família menos alcançada.

**O que a PoC faz:** mural por turma com comunicados, calendário e confirmação de leitura pelo responsável. A escola enxerga quem não foi alcançado e pode acionar por outro canal.

**Como resolve:** substitui a suposição de que a mensagem chegou por um dado. A lista de não alcançados é o produto real — é ela que permite ação.

**Ponto de atenção:** é o recorte mais próximo do que Classroom e ClassDojo já fazem. Se o grupo escolher este, vale deixar claro no vídeo qual é o diferencial (foco em rede pública sem infraestrutura, funcionamento em celular simples, uso sem conta institucional).

#### Recorte C — Rede de projetos e produções estudantis

**Problema:** o trabalho bom que o aluno produz — feira de ciências, projeto integrador, redação premiada — morre no dia da apresentação. Não vira portfólio, não é visto por outras turmas e não sobrevive ao fim do ano letivo.

**O que a PoC faz:** os alunos publicam seus projetos com descrição, mídia e disciplina envolvida; professores validam; o acervo fica navegável entre turmas e anos.

**Como resolve:** dá permanência e público ao trabalho estudantil, o que muda o incentivo — produzir para um acervo visível é diferente de produzir para uma nota.

**Ponto de atenção:** é o recorte de vínculo mais indireto com o ODS 4 e o que mais depende de um bom argumento na apresentação.

### Recomendação

Se o grupo quiser seguir pela rede social escolar, o **Recorte A** é o caminho: mantém a natureza de rede social (publicação, feed, avaliação pela comunidade, reputação), tem um problema que não precisa de defesa e coloca a lógica de negócio no centro, e não na periferia.

**Observação de sobreposição:** os recortes A e C conversam com a sugestão 5 (mentoria e reforço escolar). Se o grupo gostar do tema educação, vale decidir entre "conectar pessoas" (sugestão 5) e "compartilhar conhecimento produzido" (recorte A) — as duas juntas viram um escopo grande demais para uma PoC de dois bimestres.

---

## Como escolher

Três perguntas para o grupo responder antes de decidir:

**1. O problema é reconhecível sem explicação?**
O critério de maior peso da primeira entrega é "problema claramente definido, público identificável e relação objetiva com o ODS". As sugestões 1, 2 e 3 são as que qualquer avaliador reconhece em uma frase.

**2. Dá para demonstrar em vídeo?**
O vídeo vale 0,3 na primeira entrega e 0,2 na segunda — é o item de maior peso individual. A sugestão 2 é a mais forte visualmente; a 6 termina em um número concreto, o que também funciona bem.

**3. Alguém do grupo tem acesso ao contexto real?**
Se algum integrante conhece uma ONG, uma UBS, uma escola ou uma pequena empresa disposta a conversar meia hora, esse projeto sobe para primeiro lugar. O problema deixa de ser suposição, e o vídeo ganha uma justificativa que nenhum outro grupo terá.
