# Produto

## Problema

Hoje a decisao financeira depende de uma planilha manual. A planilha funciona, mas exige disciplina operacional e nao transforma os numeros em respostas diretas para decisoes do dia a dia.

O produto deve responder:

- Quanto entrou no mes?
- Quanto saiu em gastos fixos?
- Quanto foi gasto no diario/debito?
- Quanto saiu no total?
- Qual foi a performance do mes?
- Quanto foi economizado ou investido?
- Qual porcentagem do salario foi investida?
- Posso comprar algo agora sem comprometer minha economia?
- Se eu parcelar, isso fica saudavel nos proximos meses?

## Principios de produto

- Privacidade primeiro: dados financeiros reais ficam locais.
- Sem dependencia cloud para funcionar.
- Explicavel: toda recomendacao deve mostrar quais numeros foram usados.
- Manual primeiro: comecar com lancamentos simples antes de automatizar importacoes.
- Planilha como referencia: os primeiros calculos devem bater com a planilha atual.
- Decisao antes de grafico: telas bonitas so fazem sentido depois que as respostas estiverem corretas.

## Usuarios

O usuario inicial e uma pessoa que controla seu proprio dinheiro e quer tomar decisoes racionais de compra, economia e investimento.

Nao e um produto multiempresa, nao e contabilidade formal e nao e recomendacao de investimento.

## Escopo inicial

Entram no MVP:

- Entradas de dinheiro.
- Entradas recorrentes, como salario ou renda fixa mensal.
- Saidas fixas.
- Saidas fixas recorrentes, como aluguel, internet e assinaturas.
- Gastos diarios/debito.
- Lancamentos de investimento/economia.
- Saldo de fechamento ou ajuste.
- Resumo mensal.
- Resumo anual basico.
- Simulador de compra a vista ou parcelada.

Ficam fora no inicio:

- Integracao bancaria.
- Importacao inteligente e captura automatica de anexos.
- Cartao de credito automatizado.
- Login cloud.
- Sincronizacao entre dispositivos.
- Recomendacao de produto financeiro.
- Precificacao de carteira de investimentos.

## Linguagem do dominio

- Entrada: dinheiro que entrou no mes, como salario ou renda extra.
- Saida fixa: conta previsivel ou compromisso recorrente, como luz, aluguel, internet, cartao.
- Diario: gasto variavel do dia a dia, normalmente debito/pix/dinheiro.
- Investimento: dinheiro separado para economia, reserva ou investimento.
- Saida total: soma de saidas fixas, diario e investimentos quando o objetivo for olhar fluxo de caixa.
- Performance do mes: resultado entre entradas, saidas e economia planejada.
- Saldo: dinheiro restante ou saldo informado para reconciliar com a realidade.
- Parcela: compromisso de uma compra dividido em meses, vinculado a um grupo de parcelamento.
- Projecao: valor esperado em mês futuro; não equivale a lançamento realizado.
- Recorrencia: padrão observado em meses distintos que pode sugerir saída fixa.
- Lancamento recorrente: entrada ou saida fixa marcada pelo usuario para repetir automaticamente nos meses futuros ate ser alterada, encerrada ou cancelada.
- Entrada fixa: entrada recorrente previsivel, como salario, bolsa ou aluguel recebido.
- Sugestao: classificação proposta pelo sistema, com confiança e evidências, ainda não confirmada.

## Recorrencias manuais

O usuario deve poder marcar algumas entradas e saidas fixas como recorrentes.

Comportamento esperado:

- a recorrencia vale para o mes atual e meses seguintes;
- se nao houver data de fim, continua indefinidamente;
- alterar uma recorrencia nao deve modificar meses anteriores sem confirmacao explicita;
- editar uma ocorrencia deve permitir escolher entre `somente este mes` e `este e proximos meses`;
- excluir uma ocorrencia deve permitir remover apenas o mes ou encerrar a recorrencia a partir daquele mes;
- lancamentos gerados por recorrencia devem aparecer na lista mensal como lancamentos normais, mas com indicacao de origem recorrente.

No MVP, a recorrencia deve ser manual e explicita. Deteccao automatica por importacao pode sugerir recorrencias no futuro, mas nao deve ativar uma serie sem confirmacao do usuario.

## Pergunta central do consultor

"Essa compra cabe na minha vida financeira sem atrapalhar minha economia?"

A resposta deve considerar:

- entrada do mes;
- saidas ja comprometidas;
- gasto diario acumulado;
- investimento ja feito;
- meta de economia;
- impacto da compra no mes atual;
- impacto das parcelas nos meses futuros.

Também deve responder:

- É melhor pagar à vista ou parcelar?
- Qual é o número máximo saudável de parcelas?
- Qual é o valor máximo seguro?
- A compra toca minha reserva?
- Quanto tempo levo para recuperar a meta?
- Qual regra levou à recomendação?
