# Roadmap

Este roadmap prioriza domínio confiável, execução local simples e entregas pequenas. Funcionalidades já prototipadas podem precisar de revisão para atender aos critérios definitivos.

## Legenda

- ✅ implementado e validado;
- 🟡 parcialmente implementado ou com pendências;
- 🧪 protótipo que ainda não atende ao fluxo definitivo;
- ⬜ planejado.

## Estado atual

- ✅ Spring Boot 4, Kotlin, Java 21, Gradle Wrapper e monólito modular.
- ✅ SQLite local, JPA e cinco migrations Flyway.
- ✅ Endpoint de saúde.
- ✅ CRUD de lançamentos e listagem mensal.
- 🟡 Validações e tratamento de erros de lançamentos.
- 🟡 Resumo mensal disponível, mas ainda divergente de regras documentadas.
- 🧪 Importação CSV Nubank com parser, persistência e histórico de lotes.
- ✅ Frontend local responsivo com Alpine.js versionado.
- ✅ Dashboard, formulário, edição, exclusão, filtros e importação pelo frontend.
- 🟡 Testes: existem testes de contexto, repositório e modulith, mas a suíte não compila por referência antiga a `LancamentosMapperImpl`.
- ⬜ Metas configuráveis, consultor, parcelas, visão anual, importação inteligente, e-mail, IA e backup guiado.

O protótipo de importação não encerra o MVP futuro de importação inteligente. Persistência automática sem pré-visualização deve ser tratada como comportamento transitório.

O resumo mensal também não deve ser considerado concluído enquanto:

- investimento e ajuste de saldo não participarem das fórmulas conforme a documentação;
- divisão por zero estiver tratada;
- gasto esperado até hoje usar o dia do mês;
- performance contra a meta possuir semântica única;
- testes puros cobrirem os cálculos.

## Como interpretar itens parciais e protótipos

Um MVP 🟡 já entrega valor, mas ainda possui lacunas que impedem considerá-lo confiável ou encerrado. Cada seção parcial contém:

- `Implementado`: comportamento disponível hoje;
- `Pendências`: diferença objetiva para o estado-alvo;
- `Ordem sugerida`: sequência segura de desenvolvimento;
- `Concluído quando`: condição para trocar 🟡 por ✅.

Um MVP 🧪 prova uma ideia, mas seu contrato ou fluxo ainda será substituído. Código de protótipo pode ser reaproveitado, porém não deve limitar o desenho definitivo.

## Ordem recomendada

1. Consolidar fundação e setup.
2. Consolidar lançamentos e resumo mensal.
3. Organizar frontend com Alpine.js.
4. Implementar metas e configurações financeiras.
5. Implementar motor determinístico de decisão.
6. Implementar parcelas manuais e projeções.
7. Implementar visão anual.
8. Evoluir importação CSV inteligente.
9. Automatizar captura por e-mail.
10. Adicionar IA opcional local ou por free tier remoto.
11. Implementar backup e portabilidade guiados.

## MVP 0 - Fundação local e setup reproduzível — 🟡

Objetivo: clonar, executar e visualizar o sistema no Windows sem configurar infraestrutura.

Entregáveis:

- JDK 21 e Gradle Wrapper; ✅
- SQLite criado automaticamente; ✅
- mover o banco da raiz para `data/`;
- caminho configurável por `CONTAS_DB_PATH`;
- Flyway aplicando migrations; ✅
- perfil local padrão; ✅
- endpoint de saúde; ✅
- documentação `docs/setup-windows.md`; ✅
- banco, backups, tokens e CSVs fora do Git. ✅

### Implementado

- aplicação sobe localmente sem Docker;
- SQLite é criado automaticamente na raiz;
- migrations V1 a V5 são aplicadas;
- pool está limitado para uso com SQLite;
- perfil `local` é padrão;
- arquivos de banco são ignorados pelo Git.

### Pendências

1. Alterar a URL para usar `data/contas-termometro.db`.
2. Criar `data/` antes da inicialização do datasource.
3. Permitir override por `CONTAS_DB_PATH`.
4. Garantir que caminho com espaços funcione no Windows.
5. Testar primeiro uso sem banco e atualização de banco existente.
6. Corrigir documentação se a variável ou caminho final mudar.

### Ordem sugerida

1. parametrizar `application-local.yml`;
2. criar inicializador do diretório;
3. testar banco novo e banco migrado;
4. mover manualmente o banco existente com backup.

### Concluído quando

- clone novo sobe sem intervenção;
- banco existente pode ser movido sem perda;
- caminho padrão e customizado possuem teste;
- ADR relacionada: [ADR 0005](adrs/0005-sqlite-como-persistencia-inicial.md).

Critérios de aceite:

- um computador novo executa `.\gradlew.bat bootRun`;
- a interface abre sem Docker;
- o schema é criado automaticamente;
- o caminho do banco pode ser configurado;
- `git status` não mostra dados pessoais.

## MVP 1 - Lançamentos manuais confiáveis — ✅

Objetivo: substituir a entrada manual da planilha.

Entregáveis:

- criar, buscar, listar, editar e excluir lançamentos; ✅
- filtro mensal e vínculo opcional a lote; ✅
- validações de valor, data e mês; 🟡
- erros legíveis; 🟡
- recorrencia mensal manual para `ENTRADA` e `SAIDA_FIXA`;
- testes de service e controller;
- corrigir ou remover teste legado de mapper;
- decidir se exclusão será física ou por `status`;
- campos futuros de parcelamento previstos sem exigir implementação imediata.

### Implementado

- endpoints POST, GET por ID, GET por mês, PUT e DELETE;
- persistência monetária em centavos;
- validações Bean Validation no request;
- vínculo opcional com lote;
- exceção específica para lançamento inexistente.

### Pendências

1. Decidir e implementar cancelamento lógico ou exclusão física.
2. Fazer todas as listagens ignorarem cancelados por padrão.
3. Corrigir edição para permitir alterar `tipo` e tratar `idLote` deliberadamente.
4. Validar coerência entre `data` e `mesReferencia`.
5. Permitir valor negativo somente para `AJUSTE_SALDO`, alinhando DTO e banco.
6. Tratar `MethodArgumentNotValidException`, integridade de banco e erros inesperados em `ProblemDetail`.
7. Corrigir textos com encoding inválido.
8. Criar testes de service e controller.
9. Remover ou reescrever `LancamentosMapperImplTest`, que referencia classe inexistente.
10. Implementar recorrencias mensais manuais para entradas e saidas fixas, conforme ADR 0014.

### Ordem sugerida

1. aceitar a decisão da ADR 0011;
2. corrigir contrato e validações;
3. padronizar erros;
4. corrigir a suíte;
5. adicionar testes de comportamento.

### Concluído quando

- `.\gradlew.bat test` passa;
- cancelamento e consultas possuem semântica única;
- respostas inválidas retornam campos e mensagens previsíveis;
- ADR relacionada: [ADR 0011](adrs/0011-ciclo-de-vida-e-exclusao-de-lancamentos.md).

Critérios de aceite:

- tipos do domínio são cadastrados;
- listagem mensal é consistente;
- edição não altera silenciosamente campos não enviados;
- dados inválidos não são persistidos.

## MVP 2 - Resumo mensal validado — ✅

Objetivo: reproduzir a planilha com regras formalizadas.

Entregáveis:

- endpoint e DTO de resumo mensal; ✅
- fluxo de caixa básico; 🟡
- total investido; ✅
- performance contra meta; 🟡
- orçamento diário; 🟡
- ajustes de saldo;
- leitura da meta persistida;
- testes de limites e arredondamento;
- amostra fictícia conferida manualmente.

### Implementado

- endpoint mensal;
- DTO de resposta;
- calculadoras separadas para fluxo, investimento e gasto diário;
- totais básicos consumidos pelo dashboard.

### Divergências atuais

1. `saidaTotal` soma fixas e diário, mas não investimento.
2. `saldoMes` ignora investimento e ajustes.
3. porcentagem usa escala `0..100`.
4. performance é subtração de pontos percentuais.
5. meta está fixa em `20`, sem consultar `metas_mensais`.
6. entrada zero pode gerar divisão por zero.
7. gasto esperado retorna valor diário, não acumulado até o dia.
8. `diaAtual` é recebido, mas não é usado pela calculadora.
9. não há tratamento definido para mês futuro e histórico.

### Ordem sugerida

1. aceitar a ADR 0012;
2. escrever testes com os exemplos documentados;
3. corrigir calculadoras puras;
4. criar leitura da meta vigente;
5. ajustar DTO e frontend;
6. conferir com amostra manual fictícia.

### Concluído quando

- todos os cenários da ADR 0012 possuem teste;
- mês sem entrada retorna resposta válida;
- nenhuma fórmula é recalculada no frontend;
- resultado bate com a referência aprovada;
- ADR relacionada: [ADR 0012](adrs/0012-semantica-oficial-dos-resumos-financeiros.md).

Critérios de aceite:

- cálculo bate com a referência aprovada;
- fórmulas possuem testes puros;
- nenhuma regra depende do frontend.

## MVP 3 - Frontend local com Alpine.js — 🟡

Objetivo: operar lançamentos e resumo com código de interface simples de manter.

Entregáveis:

- Alpine.js versionado localmente; ✅
- componentes separados por fluxo;
- cliente de API e formatadores reutilizáveis;
- estados de carregamento, vazio e erro; ✅
- responsividade; ✅
- dashboard e CRUD mensal; ✅
- formulário de criação e edição; ✅
- busca e filtro por tipo; ✅
- fluxo de importação básica; ✅
- atalhos dos indicadores para listas filtradas com edição. ✅

### Implementado

- Alpine.js local e sem CDN;
- dashboard mensal;
- CRUD visual, busca e filtros;
- atalhos de cards para listagem;
- visão anual;
- upload e histórico da importação básica;
- estados de carregamento, vazio, erro e confirmação;
- layout responsivo.

### Pendências

1. Dividir `app.js` por contexto: API, dashboard, lançamentos, anual e importação.
2. Transformar os principais fluxos em componentes Alpine independentes.
3. Remover estado global que não seja realmente compartilhado.
4. Não manter fallback de cálculo financeiro no navegador após endpoints definitivos.
5. Criar testes de interface para navegação, filtro, formulário e erro da API.
6. Revisar acessibilidade com foco, labels, contraste e diálogos.
7. Definir política de cache dos assets locais.

### Ordem sugerida

1. extrair cliente HTTP e formatadores;
2. extrair componentes sem alterar comportamento;
3. adicionar testes de fluxo;
4. remover agregações transitórias após APIs anuais.

### Concluído quando

- cada fluxo possui estado isolado;
- `app.js` deixa de ser arquivo central monolítico;
- falhas da API são testadas;
- frontend não contém fórmula financeira;
- ADR relacionada: [ADR 0006](adrs/0006-frontend-simples-no-monolito.md).

Critérios de aceite:

- funciona servido pelo Spring Boot;
- funciona sem CDN;
- regras financeiras não são duplicadas no navegador;
- componentes não dependem de um estado global único.

## MVP 4 - Metas e limites financeiros — ⬜

Objetivo: fornecer parâmetros reais para o consultor.

Entregáveis:

- meta mensal e anual de investimento;
- reserva mínima intocável;
- orçamento diário mínimo;
- comprometimento máximo da renda;
- margem de segurança;
- estratégia conservadora, equilibrada ou flexível.

Critérios de aceite:

- configurações possuem valores padrão explícitos;
- mudanças afetam novas simulações;
- histórico financeiro não é recalculado silenciosamente.

## MVP 5 - Motor de tomada de decisão — ⬜

Objetivo: responder se uma compra cabe e qual alternativa é mais saudável.

Entregáveis:

- cenários à vista, parcelados, adiados e de valor reduzido;
- projeção mensal;
- valor máximo seguro;
- quantidade máxima de parcelas;
- impacto na meta, reserva, orçamento diário e comprometimento;
- prazo de recuperação;
- decisão e explicação determinísticas;
- documentação `docs/analise-financeira.md`.

Critérios de aceite:

- decisão é reproduzível sem IA;
- regras acionadas aparecem na resposta;
- parcela que quebra um mês futuro é rejeitada;
- usuário consegue conferir os cálculos.

## MVP 6 - Parcelas e compromissos futuros — ⬜

Objetivo: visualizar o que já está comprometido nos próximos meses.

Entregáveis:

- grupo de parcelamento;
- cadastro manual de compra parcelada;
- recorrencias vigentes consideradas nas projeções futuras;
- parcela atual e total;
- projeções futuras separadas de lançamentos realizados;
- aba de parcelas;
- saldo restante e mês final;
- comprometimento mensal futuro.

Critérios de aceite:

- projeção não infla o realizado;
- edição de uma parcela preserva histórico;
- cancelamento e antecipação têm semântica definida;
- consultor considera parcelas existentes.
- consultor considera entradas e saidas fixas recorrentes ainda vigentes.

## MVP 7 - Resumo anual — 🟡

Objetivo: visualizar evolução de entradas, investimento e compromissos.

Entregáveis:

- panorama anual no frontend agregando os endpoints mensais; ✅
- totais anuais; ✅
- percentual e média investidos; ✅
- destaques de maior entrada, investimento e saída; ✅
- lista dos 12 meses, incluindo meses zerados; ✅
- navegação do mês anual para lançamentos filtráveis; ✅
- endpoint anual dedicado no backend;
- melhor e pior mês segundo regra financeira formal;
- evolução de parcelas e comprometimento.

Estado atual: a interface consolida os 12 meses usando o resumo mensal e possui fallback de agregação quando o resumo falha. O MVP só será concluído após existir endpoint anual no backend e regras mensais corrigidas.

### Implementado

- seletor de ano;
- cards consolidados;
- gráfico dos 12 meses;
- destaques;
- meses vazios;
- navegação para lançamentos do mês.

### Limitações do estado atual

- frontend realiza múltiplas requisições;
- fallback duplica fórmulas financeiras;
- melhor e pior mês não possuem regra oficial;
- resultado herda divergências do resumo mensal;
- parcelas e comprometimento futuro ainda não existem.

### Ordem sugerida

1. concluir MVP 2;
2. aceitar ADR 0013;
3. implementar agregador anual no backend;
4. testar igualdade entre soma mensal e anual;
5. substituir agregação do frontend por uma requisição.

### Concluído quando

- endpoint anual retorna 12 meses;
- frontend apenas apresenta o DTO;
- ranking anual é determinístico;
- percentuais anuais usam totais do ano;
- ADR relacionada: [ADR 0013](adrs/0013-resumo-anual-calculado-no-backend.md).

## MVP 8 - Importação CSV inteligente — 🧪

Objetivo: importar com revisão, detectar parcelas, duplicidades e recorrências.

Entregáveis:

- parser Nubank e upload básico; ✅
- persistência e exclusão por lote; ✅
- pré-visualização sem persistência;
- confirmação transacional e idempotente;
- hash de arquivo, mensagem e linha;
- impressão digital do lançamento;
- detecção de parcela informada pelo Nubank;
- sugestão de recorrência e `SAIDA_FIXA`;
- regras de classificação aprendidas por confirmação;
- tela de revisão;
- documentação `docs/importacao-inteligente.md`.

Critérios de aceite:

- mesmo arquivo não é importado duas vezes;
- duplicidade exata é bloqueada;
- similaridade gera alerta;
- sugestão mostra confiança e evidências;
- nenhuma reclassificação histórica ocorre sem confirmação;
- parcelas detectadas aparecem na aba de parcelas.

### O que o protótipo comprova

- o CSV Nubank pode ser recebido e interpretado;
- linhas válidas podem virar lançamentos;
- falhas podem ser registradas;
- lançamentos podem ser agrupados e removidos por lote;
- existe interface para upload e histórico.

### Por que ainda é protótipo

- persiste imediatamente, sem pré-visualização;
- não usa hash real do arquivo;
- identificador do lote é temporal e aleatório;
- não é idempotente;
- reenvio do mesmo CSV duplica lançamentos;
- não detecta parcelas, recorrências ou similaridade;
- exclusão do lote apaga registros fisicamente;
- JSON de falhas é montado manualmente;
- erros de lote usam exceção genérica;
- contrato dos endpoints diverge da documentação definitiva.

### Estratégia de evolução

Não expandir o endpoint atual com várias responsabilidades. Criar o fluxo novo de pré-visualização em paralelo:

1. armazenar metadados e hash;
2. persistir linhas em estado não confirmado;
3. executar detectores;
4. permitir correção;
5. confirmar transacionalmente;
6. migrar o frontend;
7. descontinuar o upload com persistência imediata.

### Concluído quando

- importação é idempotente;
- revisão precede gravação;
- sugestões são auditáveis;
- lote confirmado pode ser cancelado sem apagar histórico;
- ADR relacionada: [ADR 0007](adrs/0007-importacao-inteligente-com-revisao.md).

## MVP 9 - Captura de CSV por e-mail — ⬜

Objetivo: encontrar anexos automaticamente sem infraestrutura própria em cloud.

Entregáveis:

- conector Gmail com OAuth de aplicativo desktop;
- filtros por remetente, assunto e anexo;
- polling local;
- registro de `messageId`, `attachmentId` e hash;
- envio do arquivo para a pré-visualização existente;
- revogação de acesso;
- opção de execução pelo Agendador de Tarefas do Windows.

Critérios de aceite:

- senha do e-mail nunca é armazenada;
- escopo inicial é somente leitura;
- anexo repetido não é processado;
- mensagem desconhecida não é importada;
- confirmação continua obrigatória.

## MVP 10 - IA opcional local ou remota — ⬜

Objetivo: melhorar explicações sem tornar a IA fonte da decisão ou criar custo obrigatório.

Entregáveis:

- porta `ExplicadorDecisao`;
- implementação por template;
- adaptador Ollama;
- adaptador opcional Gemini free tier;
- adaptador opcional GitHub Models para desenvolvimento;
- DTO reduzido e anonimizado;
- resposta validada por JSON Schema;
- fallback automático;
- configuração para ativar e escolher modelo.

Critérios de aceite:

- sistema funciona sem qualquer provedor;
- IA não altera decisão nem valores;
- resposta inválida usa template;
- dados brutos não são enviados ao modelo.

## MVP 10.1 - Copiloto de Configuração (Preenchimento Inteligente) — ⬜

Objetivo: transformar o usuário em um "revisor de decisões", usando a IA para ler o histórico financeiro, calcular sugestões personalizadas de configuração e preencher o formulário do Termômetro automaticamente para revisão.

Entregáveis:
- agregador de histórico financeiro no backend (cálculo de médias anonimizadas dos últimos 3 a 6 meses);
- envio de DTO de contexto para o adaptador de IA (apenas números e categorias agregadas, sem descrições brutas);
- instrução via *System Prompt* obrigando a IA a justificar o cálculo;
- formatação estrita da resposta da IA usando JSON Schema (`reservaSugerida`, `percentualFixoSugerido`, `explicacao`);
- botão na interface: "✨ Sugerir regras baseadas no meu histórico";
- o frontend recebe o mock da IA e preenche os `<input>` da tela, permitindo que o usuário edite antes de clicar em "Salvar".

Critérios de aceite:
- a IA atua apenas como "sugestão em tela", ela NUNCA salva diretamente no banco de dados (`metas_mensais` ou `configuracao_termometro`);
- se o usuário não tiver histórico suficiente (ex: conta nova), a IA ou o Backend devem usar um fallback com os cálculos estáticos padrão (ex: 6x o custo de vida informado manualmente);
- nenhuma descrição real de lançamento (ex: "Mercado Zezinho") trafega para o modelo remoto (Gemini/GitHub Models), apenas agregações (ex: "gastoDiarioMedio: 1500.00").

## MVP 11 - Backup, versionamento e portabilidade — ⬜

Objetivo: abrir o sistema em outro computador com segurança.

Entregáveis:

- exportar backup consistente;
- importar com validação de versão;
- criptografar antes de enviar para qualquer serviço;
- destino opcional em repositório GitHub privado separado;
- retenção de snapshots;
- backup automático antes de restaurar;
- mostrar caminho e data do banco;
- documentação de migração entre computadores;
- documentação `docs/backup-e-sincronizacao.md`;
- reavaliar Firestore somente para sincronização entre dispositivos.

Critérios de aceite:

- banco é restaurado em instalação nova;
- restauração incompatível é bloqueada;
- somente arquivos criptografados podem passar pelo Git;
- chave de criptografia nunca é versionada;
- procedimento prioriza Windows e não exige cloud.
