# ADR 0002 - Persistencia local e free tier

## Status

Proposta.

## Contexto

O projeto precisa rodar localmente e poder ser aberto em outro computador sem custo fixo.

## Opcoes

### PostgreSQL local com Docker

Boa compatibilidade com uma API real e facil evolucao para providers free tier como Supabase, Neon ou Railway quando necessario.

### SQLite local

Mais simples para rodar em qualquer maquina, mas menos parecido com um backend web multiusuario.

### Firebase Firestore

Free tier atraente, mas muda o desenho de persistencia e pode acoplar cedo demais o dominio ao provider.

## Recomendacao

Para o MVP da API, usar PostgreSQL local com Docker e manter as configuracoes por variaveis de ambiente.

Firebase deve entrar primeiro para Auth, quando houver frontend ou sincronizacao entre maquinas. Firestore so deve ser escolhido se a experiencia principal virar client-first.

