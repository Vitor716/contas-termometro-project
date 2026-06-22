# ADR 0006 - Frontend simples com Alpine.js no monolito

## Status

Aceita como evolução do frontend atual.

## Contexto

O projeto precisa de uma interface bonita, simples e funcional, mas o objetivo principal é evoluir as regras financeiras. JavaScript puro atende ao início, porém tende a espalhar manipulação de DOM, listeners e sincronização de estado quando aparecem filtros, formulários, modais e projeções.

## Decisao

Criar o frontend dentro do próprio projeto Spring Boot.

Usar:

- HTML e CSS em `src/main/resources/static`;
- Alpine.js 3 para estado local, eventos, formulários, modais, filtros e renderização de listas;
- módulos JavaScript pequenos para acesso à API, formatação e regras exclusivas da interface;
- `fetch` para comunicação com os endpoints do Spring Boot.

O Alpine.js deve ser versionado junto com a aplicação ou instalado de forma reproduzível. O funcionamento principal não deve depender de CDN ou internet.

Não usar Angular, Vue, React ou Next.js enquanto o estado continuar predominantemente local a cada tela.

## Organização recomendada

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
      importacao.js
      parcelas.js
      consultor.js
  vendor/
    alpine.min.js
```

Cada componente Alpine deve controlar um fluxo de negócio visível. Evitar um único objeto global contendo toda a aplicação.

## Limites

- Alpine.js não contém regras financeiras.
- O frontend não recalcula decisões retornadas pelo backend.
- Valores financeiros exibidos devem vir dos DTOs da API.
- Estado compartilhado deve ser pequeno, por exemplo mês selecionado e notificações.
- Se surgir roteamento complexo, edição em grade ou muitos estados globais, reavaliar a decisão.

## Consequencias

- Menos ferramentas para configurar.
- Um único processo local para API e tela.
- Manipulação de interface mais simples que DOM manual.
- Ainda existe necessidade de disciplina para não colocar regra de negócio no HTML.
- Se a interface crescer muito, uma SPA pode ser extraída sem comprometer o domínio.

## Referências

- [Alpine.js - instalação](https://alpinejs.dev/essentials/installation)
- [Alpine.js - conceitos iniciais](https://alpinejs.dev/start-here)
