# ADR 0006 - Frontend simples no monolito

## Status

Aceita para o MVP.

## Contexto

O projeto precisa de uma interface bonita, simples e funcional, mas o objetivo principal e evoluir as regras financeiras. O usuario nao quer gastar muita energia com frontend nem criar dependencias desnecessarias.

## Decisao

Criar o frontend inicial dentro do proprio projeto Spring Boot.

Usar HTML, CSS e JavaScript simples em `src/main/resources/static`. Thymeleaf pode ser considerado se renderizacao server-side ajudar, mas nao e obrigatorio no inicio.

Nao usar Angular, Vue, React ou Next.js no MVP.

## Consequencias

- Menos ferramentas para configurar.
- Um unico processo local para API e tela.
- Menos dependencia externa.
- Menor curva de manutencao.
- Se a interface crescer muito, uma SPA pode ser extraida no futuro sem comprometer o dominio.

