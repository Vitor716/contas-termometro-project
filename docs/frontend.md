# Frontend

## Decisao

O MVP deve ter um frontend simples dentro do proprio projeto Spring Boot.

Abordagem recomendada:

- HTML e CSS em `src/main/resources/static`;
- Alpine.js 3 para reatividade e estado de interface;
- JavaScript modular para API e utilitarios;
- sem Angular, Vue, React ou Next.js no inicio.

## Por que essa abordagem

O objetivo do projeto e ajudar a tomar decisoes financeiras racionais. O risco principal nao e falta de framework frontend; o risco principal e calcular errado ou transformar o projeto em algo trabalhoso demais antes de validar o dominio.

Alpine.js foi escolhido porque mantém o frontend próximo de HTML e JavaScript, mas reduz manipulação manual de DOM para listas, filtros, formulários, abas e modais.

Estado atual: Alpine.js está versionado localmente e controla navegação, menu responsivo e atalhos de filtro. O acesso à API e a renderização das tabelas ainda podem ser extraídos progressivamente do `app.js` para componentes separados.

Um frontend simples:

- reduz dependencias;
- reduz build e configuracao;
- roda junto com a API;
- facilita abrir localmente;
- evita separar deploys;
- permite focar nas regras financeiras.

## Experiencia desejada

Mesmo simples, a interface deve ser bonita, clara e funcional.

Prioridades:

- lancar entrada, saida fixa, diario, investimento e ajuste de saldo rapidamente;
- marcar entrada ou saida fixa como recorrente quando fizer sentido;
- visualizar resumo mensal sem ruido;
- mostrar percentuais e performance de forma direta;
- simular compra a vista ou parcelada com explicacao;
- nunca expor dados reais fora da maquina.

## Estrutura sugerida

```text
src/main/resources/static/
  index.html
  css/
    app.css
  js/
    api.js
    formatadores.js
    componentes/
      dashboard.js
      lancamentos.js
      parcelas.js
      importacao.js
      consultor.js
  vendor/
    alpine.min.js
```

Primeiras telas:

- Dashboard mensal.
- Lancamento rapido.
- Lista de lancamentos do mes.
- Simulador de compra.
- Parcelas e projecoes.
- Resumo anual.
- Importacao CSV inteligente, em MVP posterior.

### Recorrencias

O formulario de lancamento deve permitir marcar recorrencia apenas para:

- `ENTRADA`;
- `SAIDA_FIXA`.

Campos esperados:

- recorrente: sim/nao;
- inicio da recorrencia;
- dia preferencial;
- fim opcional;
- frequencia mensal no MVP.

Ao editar ou excluir um lancamento originado de recorrencia, a interface deve pedir o escopo da acao:

- somente este mes;
- este e proximos meses;
- toda a serie, quando permitido.

A listagem mensal deve indicar quando um lancamento veio de recorrencia. Essa indicacao nao deve competir com tipo, categoria e valor; pode ser um pequeno selo ou texto auxiliar.

O dashboard nao deve recalcular recorrencias no navegador. A tela deve consumir os lancamentos/resumos ja resolvidos pelo backend.

### Visão anual

A visão anual existente:

- permite selecionar o ano;
- consolida entradas, saídas operacionais, investimentos e saldo;
- mostra comparação visual dos 12 meses;
- destaca meses com maior entrada, investimento e saída;
- inclui meses sem movimentação;
- abre os lançamentos de qualquer mês ao clicar no card.

Enquanto não existir `GET /api/anos/{ano}/resumo`, o frontend agrega os endpoints mensais. Essa é uma solução transitória; a regra anual definitiva deve ficar no backend.

No dashboard, indicadores operacionais devem funcionar como atalhos:

- `Entradas` abre lançamentos do tipo `ENTRADA`;
- `Saídas fixas` abre lançamentos do tipo `SAIDA_FIXA`;
- `Gastos diários` abre lançamentos do tipo `GASTO_DIARIO`;
- `Saldo do mês` abre todas as movimentações;
- a listagem filtrada mantém ações de edição e exclusão.

## Regras de implementação

- Dependências devem funcionar offline e ficar versionadas ou possuir build reproduzível.
- Não usar CDN como requisito de execução.
- Regras financeiras continuam no backend.
- Um componente Alpine deve representar um fluxo da tela.
- Estado global deve ficar restrito a informações realmente compartilhadas, como mês selecionado.
- A camada de API deve centralizar `fetch`, tratamento de erro e serialização.

## Quando considerar um framework

Reavaliar Angular/Vue/React somente se pelo menos um destes pontos aparecer:

- muitas telas com estado compartilhado;
- edicao complexa em grade/tabela;
- graficos interativos pesados;
- necessidade de app mobile/PWA mais elaborado;
- frontend separado para deploy em hosting estatico.

Enquanto isso nao existir, manter Alpine.js e frontend local-first.

## Decisao sobre design

O visual deve ser calmo e operacional, nao marketing.

Diretrizes:

- poucos cliques para lancar dados;
- cards apenas para blocos de informacao;
- tabelas claras para lancamentos;
- cores de status com significado financeiro;
- evitar animacoes e excesso visual;
- priorizar legibilidade dos numeros.
