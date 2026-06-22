# Índice de decisões arquiteturais

ADRs registram decisões e consequências. Tarefas, bugs e critérios de conclusão pertencem ao roadmap.

| ADR | Status | Tema | Roadmap relacionado |
|---|---|---|---|
| [0001](0001-monolito-modular-local-first.md) | Aceita | Monólito modular local-first | MVP 0 |
| [0002](0002-persistencia-local-e-free-tier.md) | Parcialmente substituída | Persistência local e free tier | MVP 0 e 11 |
| [0003](0003-firebase.md) | Parcialmente substituída | Uso futuro de Firebase | MVP 11 |
| [0004](0004-privacidade-e-github.md) | Aceita | Privacidade e GitHub | Todos |
| [0005](0005-sqlite-como-persistencia-inicial.md) | Aceita | SQLite inicial | MVP 0 |
| [0006](0006-frontend-simples-no-monolito.md) | Aceita | Alpine.js no monólito | MVP 3 |
| [0007](0007-importacao-inteligente-com-revisao.md) | Aceita para evolução | Importação com revisão | MVP 8 |
| [0008](0008-motor-deterministico-e-ia-local-opcional.md) | Aceita para evolução | Motor e IA opcional | MVP 5 e 10 |
| [0009](0009-captura-local-de-csv-por-email.md) | Proposta | Captura por e-mail | MVP 9 |
| [0010](0010-backup-criptografado-e-sincronizacao-opcional.md) | Proposta | Backup e sincronização | MVP 11 |
| [0011](0011-ciclo-de-vida-e-exclusao-de-lancamentos.md) | Proposta | Cancelamento lógico | MVP 1 |
| [0012](0012-semantica-oficial-dos-resumos-financeiros.md) | Proposta | Fórmulas e DTO mensal | MVP 2 |
| [0013](0013-resumo-anual-calculado-no-backend.md) | Proposta | Agregação anual | MVP 7 |

## Regra de atualização

Quando uma decisão for aceita:

1. atualizar o status da ADR;
2. implementar migrations, código e testes;
3. marcar os itens correspondentes no roadmap;
4. registrar uma ADR substituta se a decisão mudar.
