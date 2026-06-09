# ADR 0003 - Uso de Firebase

## Status

Proposta.

## Decisao recomendada

Nao iniciar o backend preso ao Firebase.

Usar Firebase em fases:

1. Firebase Auth para login quando houver frontend.
2. Firebase Hosting para frontend estatico, se escolhido.
3. Firestore apenas se o produto precisar operar como app client-first com sincronizacao simples.

## Motivo

O core do produto e calculo financeiro e historico de transacoes. Esse dominio fica mais previsivel em banco relacional no inicio.

