# Frontend

## Decisao

O MVP deve ter um frontend simples dentro do proprio projeto Spring Boot.

Abordagem recomendada:

- HTML, CSS e JavaScript simples em `src/main/resources/static`;
- ou templates server-side com Thymeleaf se houver necessidade de renderizacao dinamica no servidor;
- sem Angular, Vue, React ou Next.js no inicio.

## Por que essa abordagem

O objetivo do projeto e ajudar a tomar decisoes financeiras racionais. O risco principal nao e falta de framework frontend; o risco principal e calcular errado ou transformar o projeto em algo trabalhoso demais antes de validar o dominio.

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
    app.js
```

Primeiras telas:

- Dashboard mensal.
- Lancamento rapido.
- Lista de lancamentos do mes.
- Simulador de compra.
- Resumo anual.

## Quando considerar um framework

Reavaliar Angular/Vue/React somente se pelo menos um destes pontos aparecer:

- muitas telas com estado compartilhado;
- edicao complexa em grade/tabela;
- graficos interativos pesados;
- necessidade de app mobile/PWA mais elaborado;
- frontend separado para deploy em hosting estatico.

Enquanto isso nao existir, manter frontend simples e local-first.

## Decisao sobre design

O visual deve ser calmo e operacional, nao marketing.

Diretrizes:

- poucos cliques para lancar dados;
- cards apenas para blocos de informacao;
- tabelas claras para lancamentos;
- cores de status com significado financeiro;
- evitar animacoes e excesso visual;
- priorizar legibilidade dos numeros.

