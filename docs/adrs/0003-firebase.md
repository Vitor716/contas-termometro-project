# ADR 0003 - Uso de Firebase

## Status

Substituída parcialmente pela ADR 0010.

## Decisao recomendada

Nao iniciar o backend preso ao Firebase.

Se Firebase for adotado, usar em fases:

1. Firebase Auth para login quando houver frontend.
2. Firebase Hosting para frontend estatico, se escolhido.
3. Firestore apenas se o produto precisar operar como app client-first com sincronizacao simples.

Para o estágio atual, backup criptografado é preferido. Firestore será reconsiderado quando existir necessidade real de sincronização automática entre dispositivos.

## Motivo

O core do produto e calculo financeiro e historico de transacoes. Esse dominio fica mais previsivel em banco relacional no inicio.
