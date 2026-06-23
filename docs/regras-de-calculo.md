# Regras de Cálculo

Este documento descreve, de forma detalhada e orientada ao domínio, as regras e fórmulas usadas pela aplicação para gerar os resumos, decisões e indicadores financeiros. O objetivo é explicar quais valores do domínio são usados como entradas, como cada métrica é calculada passo-a-passo e quais interpretações e validações aplicamos.

Observação: se existir uma planilha externa com outra fórmula, a planilha prevalece até que a regra seja formalmente revisada e aprovada.

Sumário rápido

- Escopo: cálculos do domínio (resumo mensal/ anual, diário, performance, decisões de compra à vista e parcelada).
- Não cobre endpoints ou formatos de API; foca nas fórmulas, entradas e exemplos.

Glossário (mapeamento para modelos do código)

- Lancamento (entidade): representa um registro financeiro com pelo menos os campos `tipo`, `valor`, `data`, `mesReferencia`, `categoria`, `observacao`.
- Tipos de lançamento (`Lancamento.tipo`): `ENTRADA`, `SAIDA_FIXA`, `GASTO_DIARIO`, `INVESTIMENTO`, `AJUSTE_SALDO`.
- totalEntradas, totalSaidasFixas, totalGastoDiario, totalInvestido: somas agrupadas por `mesReferencia` e por `tipo`.
- percentualMetaInvestimentoMensal: parâmetro de configuração do módulo `orcamento` (valor entre 0 e 1). Pode ser uma constante ou configurável por usuário/conta.

Princípios e Assunções

- Somamos lançamentos filtrando por `mesReferencia` (ex: `2026-06`) para cálculos mensais.
- Antes de calcular um mes, recorrencias vigentes de `ENTRADA` e `SAIDA_FIXA` devem estar materializadas ou consideradas como projecao daquele mes.
- `AJUSTE_SALDO` é aplicado apenas para ajustar o saldo do mês quando há diferença entre o saldo calculado e o saldo real (por exemplo, saldo em conta). Em cálculos de disponibilidade operacional preferimos usar `soma(AJUSTE_SALDO)` apenas no `saldoMes` e não nas métricas de gasto operacional.
- Divisões por zero: quando `totalEntradas == 0`, percentuais que dependam de `totalEntradas` devem retornar `0` ou `null` de forma documentada (ver seção de validação abaixo).
- Datas: `diasDoMes` é o número de dias do mês de `mesReferencia` (ex.: junho tem 30 dias). `diaDoMes` é o dia atual relativo ao mês (1..diasDoMes) — para comparações acumuladas usamos a data de execução do cálculo.

1) Resumo Mensal (métricas principais)

Entradas (variáveis usadas):
- totalEntradas = soma dos valores de lançamentos do tipo `ENTRADA` com `mesReferencia = M`.
- totalSaidasFixas = soma dos lançamentos do tipo `SAIDA_FIXA` no mês `M`.
- totalGastoDiario = soma dos lançamentos do tipo `GASTO_DIARIO` no mês `M`.
- totalInvestido = soma dos lançamentos do tipo `INVESTIMENTO` no mês `M`.
- ajustes = soma dos lançamentos do tipo `AJUSTE_SALDO` no mês `M`.

Fórmulas (passo a passo):

- saidaSemInvestimento = totalSaidasFixas + totalGastoDiario
- saidaTotal = saidaSemInvestimento + totalInvestido
- saldoMes = totalEntradas - saidaTotal + ajustes

Interpretação:
- `saidaSemInvestimento` representa o custo operacional do mês (fixos + variáveis), excluindo o que foi reservado/investido.
- `saidaTotal` inclui o que foi efetivamente destinado a investimento/economia.
- `saldoMes` mostra o resultado líquido do mês considerando ajustes manuais.

Exemplo numérico:
- totalEntradas = 10.000,00
- totalSaidasFixas = 3.000,00
- totalGastoDiario = 2.000,00
- totalInvestido = 1.500,00
- ajustes = -50,00

Calculo:
- saidaSemInvestimento = 3.000 + 2.000 = 5.000
- saidaTotal = 5.000 + 1.500 = 6.500
- saldoMes = 10.000 - 6.500 - 50 = 3.450,00

### 1.1) Recorrencias no resumo mensal

Objetivo: entradas fixas e saidas fixas recorrentes devem participar automaticamente dos resumos dos meses futuros.

Tipos aceitos no MVP:

- `ENTRADA`;
- `SAIDA_FIXA`.

Regra:

- para o mes `M`, localizar recorrencias ativas com `mes_inicio <= M` e (`mes_fim` vazio ou `mes_fim >= M`);
- gerar ou considerar uma ocorrencia por recorrencia vigente;
- nao gerar ocorrencia duplicada se ja existir lancamento com `recorrencia_id + mes_referencia`;
- ocorrencias recorrentes entram nas mesmas somas dos lancamentos manuais;
- excecoes de um mes prevalecem sobre a regra da serie naquele mes.

Exemplo:

- salario recorrente de 8.000 a partir de `2026-06`;
- internet recorrente de 120 a partir de `2026-06`;
- ao consultar `2026-08`, o resumo deve incluir 8.000 em `totalEntradas` e 120 em `totalSaidasFixas`, mesmo que o usuario ainda nao tenha cadastrado manualmente esse mes.

Edicao:

- `somente este mes`: altera apenas a ocorrencia e marca como excecao;
- `este e proximos meses`: altera a regra a partir de `M`, preservando meses anteriores;
- `toda a serie`: deve exigir confirmacao quando afetar historico.

Exclusao:

- remover somente o mes exclui ou cancela a ocorrencia;
- encerrar a recorrencia define fim da serie a partir de `M`;
- lancamentos passados nao devem ser apagados silenciosamente.

2) Economia Mensal (indicadores de prioridade)

Fórmulas:

- percentualInvestidoSobreEntradas = if totalEntradas > 0 then totalInvestido / totalEntradas else 0
- sobraDepoisDeGastos = totalEntradas - totalSaidasFixas - totalGastoDiario

Notas:
- `percentualInvestidoSobreEntradas` indica a parcela das entradas efetivamente transformada em investimento no mês.
- `sobraDepoisDeGastos` é a disponibilidade operacional antes de decidir entre investir, comprar ou manter em caixa. Pode ser negativa (déficit).

Exemplo (continuação):
- percentualInvestidoSobreEntradas = 1.500 / 10.000 = 0,15 (15%)
- sobraDepoisDeGastos = 10.000 - 3.000 - 2.000 = 5.000

3) Meta e Performance do Mês

Entradas adicionais:
- percentualMetaInvestimentoMensal (configurável). Ex.: 0.2 = 20%.

Fórmulas:

- metaInvestimento = totalEntradas * percentualMetaInvestimentoMensal
- diferencaInvestimento = totalInvestido - metaInvestimento
- percentualPerformance = if metaInvestimento > 0 then totalInvestido / metaInvestimento else 0

Interpretação prática:
- `percentualPerformance >= 1`: meta atingida ou ultrapassada.
- `0.8 <= percentualPerformance < 1`: aproximação da meta (alerta amarelo).
- `percentualPerformance < 0.8`: performance significativamente abaixo da meta (alerta vermelho).

Exemplo (percentualMetaInvestimentoMensal = 0.2):
- metaInvestimento = 10.000 * 0.2 = 2.000
- diferencaInvestimento = 1.500 - 2.000 = -500 (falta 500)
- percentualPerformance = 1.500 / 2.000 = 0,75 (75%) => abaixo da meta

4) Orçamento Diário (controle do gasto variável)

Objetivo: comparar o gasto acumulado com o que seria esperado proporcionalmente ao dia do mês.

Fórmulas e definições:

- diasDoMes = número de dias do mês `M` (ex.: 30)
- diaDoMes = dia atual (1..diasDoMes). Para relatórios históricos, usa-se o último dia do mês para avaliação acumulada final.
- orcamentoDiario = if diasDoMes > 0 then (totalEntradas - totalSaidasFixas - metaInvestimento) / diasDoMes else 0
- totalGastoDiario = soma(GASTO_DIARIO)
- diarioEsperadoAteHoje = orcamentoDiario * diaDoMes
- diarioRestante = diarioEsperadoAteHoje - totalGastoDiario

Interpretação:
- Se `diarioRestante >= 0` então o gasto até hoje está dentro do esperado.
- Se `diarioRestante < 0` houve excesso de gasto em relação ao plano diário.

Exemplo:
- metaInvestimento = 2.000 (como no exemplo)
- orcamentoDiario = (10.000 - 3.000 - 2.000) / 30 = 166,67
- supondo diaDoMes = 15, diarioEsperadoAteHoje = 2.500, totalGastoDiario = 1.800
- diarioRestante = 2.500 - 1.800 = 700 (ainda dentro do limite)

5) Resumo Anual

Fórmulas:

- entradasAnuais = soma(totalEntradas de todos os meses do ano)
- investimentoAnual = soma(totalInvestido de todos os meses do ano)
- percentualInvestidoAnual = if entradasAnuais > 0 then investimentoAnual / entradasAnuais else 0

Observação: ao gerar a lista de resumos mensais, meses sem lançamentos devem ser incluídos com valores zerados (ou documentar a escolha de omiti-los).

6) Decisão de Compra — À Vista

Entradas necessárias:
- valorCompra: preço da compra proposta
- resumoAtual do mês (totalEntradas, totalSaidasFixas, totalGastoDiario, totalInvestido, metaInvestimento, sobraDepoisDeGastos)

Regras e interpretações (duas abordagens explicitas):

a) Conservadora (não mexer no que já foi investido):

- sobraDepoisDaCompra = sobraDepoisDeGastos - valorCompra
- podeComprarAVista = (sobraDepoisDaCompra >= 0) e (totalInvestido >= metaInvestimento)

Interpretação: compra só é permitida se existir sobra operacional suficiente e a meta de investimento do mês já estiver alcançada.

b) Flexível (permitir usar parte do investimento para a compra):

- investimentoDepoisDaCompra = totalInvestido - max(0, valorCompra - sobraDepoisDeGastos)
- podeComprarAVista = (investimentoDepoisDaCompra >= metaInvestimento)

Interpretação: se a sobra operacional não cobrir toda a compra, permitimos reduzir o que estava previsto para investimento, desde que após a compra a quantia destinada a investimento não fique abaixo da meta.

Exemplo (sobraDepoisDeGastos = 5.000, totalInvestido=1.500, metaInvestimento=2.000):
- valorCompra = 600 => abordagem a) sobraDepoisDaCompra = 4.400 => pode comprar? não (investimento < meta)
- abordagem b) investimentoDepoisDaCompra = 1.500 - max(0, 600 - 5.000) = 1.500 => ainda abaixo da meta -> não recomendado

Observação: a resposta da API deve explicitar qual interpretação foi aplicada e apresentar os números usados (sobra antes, sobra depois, investimento antes/depois, meta).

7) Decisão de Parcelamento

Entradas necessárias:
- valorCompra; parcelas; mesInicial; projeção de entradas futuras (se disponível); totalSaidasFixas futuras conhecidas; percentualMetaInvestimentoMensal.

Regra inicial (simplificada):

- valorParcela = valorCompra / parcelas
- para cada mês afetado, atualiza-se provisoriamente `totalSaidasFixas + valorParcela` e recalcula-se `orcamentoDiario` para verificar impacto.
- podeParcelar = condição que indica se, em média, o orçamento diário futuro permanecerá acima de um mínimo aceitável (`orcamentoDiarioMinimo`, parâmetro de heurística) e se a meta de investimento pode continuar sendo atendida.

Saídas esperadas para o usuário:
- valor da parcela; meses afetados; impacto no orcamentoDiario e se a meta de investimento permanece atingível; explicação numérica mês-a-mês.

Exemplo simplificado:
- valorCompra = 1.200, parcelas = 6 => valorParcela = 200
- para os 6 meses seguintes, cada mês terá +200 em saídas fixas projetadas; recalculamos metaInvestimento e orcamentoDiario e verificamos se `orcamentoDiario` fica abaixo do mínimo tolerável.

8) Regras de Validação e Arredondamento

- Valores monetários: armazenar e mostrar com 2 casas decimais; operações internas usam decimal (BigDecimal) para evitar perda de precisão.
- Arredondamento padrão: HALF_UP (arredonda .5 para cima).
- Percentuais: apresentar com 2 casas decimais (ex.: 15,00%).
- Regras de validação:
  - `Lancamento.valor` deve ser > 0 para tipos que representam transações reais (ENTRADA, SAIDA_FIXA, GASTO_DIARIO, INVESTIMENTO). AJUSTE_SALDO pode ser negativo/positivo.
  - `mesReferencia` deve obedecer ao formato `yyyy-MM`.
  - `percentualMetaInvestimentoMensal` deve estar entre 0 e 1.

9) Tratamento de casos especiais

- totalEntradas = 0: percentuais relativos às entradas retornam 0 e exibem nota de que não houve entradas.
- Meses com lançamentos nulos: incluir mês com zeros no resumo anual (ou documentar quando optar por omitir).
- Valores extremos/negativos: aceitar lançamentos negativos apenas quando semântica permitir (ex.: estorno) e documentar impacto nas somas.

10) Mapas entre termos do negócio e atributos de código

- `totalEntradas` = repository.sumByTipo(mes, TipoLancamento.ENTRADA)
- `totalSaidasFixas` = repository.sumByTipo(mes, TipoLancamento.SAIDA_FIXA)
- `totalGastoDiario` = repository.sumByTipo(mes, TipoLancamento.GASTO_DIARIO)
- `totalInvestido` = repository.sumByTipo(mes, TipoLancamento.INVESTIMENTO)
- `ajustes` = repository.sumByTipo(mes, TipoLancamento.AJUSTE_SALDO)

11) Exposição para consumidores (o que incluir no DTO `ResumoMensal`)

Recomenda-se que o DTO contenha:
- totalEntradas, totalSaidasFixas, totalGastoDiario, totalInvestido, ajustes
- saidaSemInvestimento, saidaTotal, saldoMes
- percentualInvestidoSobreEntradas, sobraDepoisDeGastos
- metaInvestimento, diferencaInvestimento, percentualPerformance
- orcamentoDiario, diarioEsperadoAteHoje, diarioRestante
- datas: mesReferencia, diasDoMes, diaDoMes (valor usado para cálculo)

12) Notas finais e manutenção

- Sempre documentar mudanças nas fórmulas neste arquivo e incluir um exemplo numérico quando alterar a semântica.
- Testes: cada fórmula deve ter testes unitários que verifiquem casos normais, limites (zer0, negativos), e arredondamento.

Se quiser, eu posso:

- 1) adicionar exemplos de testes unitários que comprovem cada fórmula; ou
- 2) gerar um conjunto de exemplos CSV/fixtures que ilustrem cenários (mês normal, mês sem entrada, compra parcelada). 

Fim do documento.
