# Roadmap

Este roadmap existe para permitir evoluir o projeto sem depender de muita IA. Cada MVP deve terminar com algo executavel, testado e pequeno o bastante para revisar sozinho.

## Ordem recomendada

1. Fundacao tecnica.
2. Lancamentos manuais.
3. Resumo mensal.
4. Importacao CSV.
5. Consultor de compra.
6. Visao anual.
7. Frontend simples.
8. Metas e investimentos.

## MVP 0 - Fundacao local

Objetivo: deixar o projeto pronto para desenvolvimento local seguro.

Entregaveis:

- Configurar SQLite local. ✅
- Garantir que `data/` fique fora do Git. ✅
- Criar migrations iniciais.
- Criar estrutura de pacotes em portugues. ✅
- Criar perfil `local`. ✅
- Criar endpoint `GET /api/sistema/saude`. ✅
- Criar fixtures anonimas para testes. ✅

Tarefas:

- Adicionar dependencias de persistencia.
- Criar `data/.gitkeep` somente se necessario, sem banco real.
- Criar migration `V1__criar_lancamentos.sql`.
- Criar migration `V2__criar_metas_mensais.sql`.
- Criar teste de contexto.
- Criar teste simples de repository.

Criterios de aceite:

- `.\gradlew.bat test` passa.
- A aplicacao sobe sem cloud.
- O banco real nao aparece no `git status`.

## MVP 1 - Lancamentos manuais

Objetivo: substituir o input manual da planilha.

Modulos envolvidos:

- `lancamentos`
- `configuracao`

Endpoints:

```text
POST /api/lancamentos
GET /api/lancamentos?mes=2026-06
GET /api/lancamentos/{id}
PUT /api/lancamentos/{id}
DELETE /api/lancamentos/{id}
```

Tipos de lancamento:

- `ENTRADA`
- `SAIDA_FIXA`
- `GASTO_DIARIO`
- `INVESTIMENTO`
- `AJUSTE_SALDO`

Campos minimos:

- `id`
- `tipo`
- `descricao`
- `valor`
- `data`
- `mesReferencia`
- `categoria`
- `observacao`

Tarefas:

- Criar entidade `Lancamento`.
- Criar enum `TipoLancamento`.
- Criar repository.
- Criar service com validacoes.
- Criar controller.
- Criar DTOs de request/response.
- Validar valor positivo.
- Validar `mesReferencia` no formato `yyyy-MM`.
- Criar testes unitarios do service.
- Criar testes de controller.

Criterios de aceite:

- E possivel cadastrar entrada, saida fixa, gasto diario e investimento.
- E possivel listar todos os lancamentos de um mes.
- Dados invalidos retornam erro claro.

## MVP 2 - Resumo mensal

Objetivo: reproduzir os calculos principais da planilha.

Modulos envolvidos:

- `lancamentos`
- `orcamento`

Endpoint:

```text
GET /api/meses/{yyyy-MM}/resumo
```

Resposta deve conter:

- soma das entradas;
- soma das saidas fixas;
- total de gasto diario;
- total investido/economizado;
- saida total;
- saldo do mes;
- porcentagem investida sobre entradas;
- meta de investimento do mes;
- performance contra meta;
- gasto diario esperado ate o dia atual;
- gasto diario restante.

Tarefas:

- Criar calculadora mensal pura, sem Spring.
- Criar DTO `ResumoMensal`.
- Criar service que busca lancamentos e chama calculadora.
- Criar controller de meses.
- Criar testes com cenario parecido com a planilha real, mas usando valores ficticios.
- Documentar formulas em `docs/regras-de-calculo.md`.

Criterios de aceite:

- O resumo bate com uma amostra manual calculada.
- A calculadora tem testes cobrindo entrada, saida fixa, gasto diario e investimento.
- Nenhum dado real e usado nos testes.

## MVP 3 - Importacao CSV

Objetivo: reduzir input manual importando extratos, com revisao antes de salvar.

Comecar por Nubank, mas desenhar para permitir outros bancos depois.

Modulos envolvidos:

- `importacao`
- `lancamentos`

Endpoints:

```text
POST /api/importacoes/nubank/csv/pre-visualizacao
POST /api/importacoes/nubank/csv/confirmar
GET /api/importacoes/{id}
```

Fluxo:

1. Usuario envia CSV.
2. Sistema interpreta linhas.
3. Sistema retorna pre-visualizacao com sugestao de tipo/categoria.
4. Usuario confirma o que deve virar lancamento.
5. Sistema grava lancamentos.

Tarefas:

- Criar modulo `importacao`.
- Criar parser para CSV Nubank.
- Criar modelo `LinhaImportada`.
- Criar detector simples de duplicidade.
- Criar mapeamento de colunas documentado.
- Criar testes com CSV ficticio.
- Bloquear persistencia automatica sem confirmacao.

Criterios de aceite:

- Um CSV ficticio do Nubank e interpretado.
- A pre-visualizacao nao grava no banco.
- A confirmacao grava somente linhas selecionadas.
- Duplicidades obvias sao sinalizadas.

Observacao:

Nao versionar CSV real. Criar apenas exemplos anonimos em `src/test/resources`.

## MVP 4 - Consultor de compra

Objetivo: responder se uma compra cabe no mes atual ou em parcelas.

Modulos envolvidos:

- `consultor`
- `orcamento`
- `lancamentos`

Endpoints:

```text
POST /api/consultor/compras/simular
GET /api/consultor/simulacoes?mes=2026-06
```

Entrada:

- descricao;
- valor;
- forma de pagamento: `A_VISTA` ou `PARCELADO`;
- quantidade de parcelas;
- mes inicial.

Resposta:

- decisao: `OK`, `ATENCAO`, `NAO_RECOMENDADO`;
- impacto no mes atual;
- impacto nos proximos meses;
- impacto na meta de investimento;
- explicacao com os numeros usados.

Tarefas:

- Criar calculadora de compra a vista.
- Criar calculadora de parcelamento.
- Criar DTO de simulacao.
- Criar historico opcional de simulacoes.
- Criar testes para compra que cabe e compra que nao cabe.

Criterios de aceite:

- O sistema explica por que uma compra cabe ou nao cabe.
- O resultado nao depende de IA.
- O usuario consegue reproduzir a conta manualmente.

## MVP 5 - Resumo anual

Objetivo: enxergar evolucao de entradas e investimentos no ano.

Endpoint:

```text
GET /api/anos/{yyyy}/resumo
```

Resposta deve conter:

- total de entradas no ano;
- total investido no ano;
- porcentagem investida anual;
- media mensal investida;
- melhor mes;
- pior mes;
- lista de resumos mensais.

Tarefas:

- Reutilizar calculadora mensal.
- Criar agregador anual.
- Criar testes com 12 meses ficticios.

Criterios de aceite:

- Percentual anual bate com soma dos meses.
- Meses sem lancamento aparecem zerados ou sao tratados de forma documentada.

## MVP 6 - Frontend local simples

Objetivo: usar o sistema sem Postman/curl.

Abordagem:

- HTML/CSS/JS simples em `src/main/resources/static`.
- Sem Angular, Vue ou React.

Telas:

- Dashboard mensal.
- Formulario de lancamento.
- Lista de lancamentos.
- Importacao CSV.
- Simulador de compra.
- Resumo anual.

Tarefas:

- Criar layout base.
- Criar chamadas `fetch` para a API.
- Criar estados de carregamento e erro.
- Criar formatacao de moeda e percentual.
- Criar CSS simples e responsivo.

Criterios de aceite:

- E possivel cadastrar lancamento pela tela.
- E possivel ver resumo mensal.
- E possivel simular compra.
- A tela roda servida pelo Spring Boot.

## MVP 7 - Metas de investimento

Objetivo: evoluir a parte de investimento sem transformar o app em corretora.

Endpoints:

```text
POST /api/metas
GET /api/metas?ano=2026
PUT /api/metas/{id}
```

Entregaveis:

- meta mensal por percentual;
- meta anual;
- categorias de investimento;
- comparacao meta vs realizado;
- projecao simples de recuperacao depois de compra grande.

Criterios de aceite:

- O sistema mostra se a meta mensal foi batida.
- O sistema mostra quanto falta para a meta anual.
- Compras simuladas mostram impacto na recuperacao futura.

## MVP 8 - Backup e portabilidade

Objetivo: abrir em outro PC sem usar cloud obrigatoria.

Entregaveis:

- exportar backup local;
- importar backup local;
- documentar restauracao;
- avaliar criptografia de backup.

Criterios de aceite:

- Um banco local pode ser copiado/restaurado sem expor dados no Git.
- O processo esta documentado.
