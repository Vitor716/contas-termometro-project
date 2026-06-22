# ADR 0008 - Motor determinístico com IA opcional local ou remota

## Status

Aceita para evolução futura.

## Contexto

O consultor deve recomendar à vista, parcelado, quantidade segura de parcelas, valor máximo e recuperação da meta. O projeto também deseja experimentar IA sem custo obrigatório.

Existem duas categorias:

- execução local, sem cobrança por requisição e sem envio para terceiros;
- API remota com faixa gratuita, sujeita a limites, mudança de preço e políticas de tratamento de dados.

## Decisão

Separar o consultor em duas camadas.

### Camada obrigatória: motor determinístico

Calcula cenários com regras versionadas e retorna:

- decisão `OK`, `ATENCAO` ou `NAO_RECOMENDADO`;
- pagamento recomendado;
- quantidade máxima saudável de parcelas;
- valor máximo seguro;
- impacto mensal;
- impacto na meta de investimento;
- impacto na reserva intocável;
- comprometimento de renda;
- orçamento diário restante;
- prazo estimado de recuperação;
- regras acionadas e valores usados.

Esta camada é a fonte de verdade e deve funcionar sem IA.

### Camada opcional: assistente de IA

Pode usar:

- modelo local via Ollama;
- Gemini Developer API no free tier;
- GitHub Models para desenvolvimento e experimentação;
- outro provedor que implemente o mesmo contrato.

Usos permitidos:

- transformar o resultado estruturado em explicação mais natural;
- comparar cenários já calculados;
- resumir riscos;
- sugerir perguntas adicionais;
- auxiliar classificação de descrições ambíguas.

A IA não pode:

- modificar cálculos;
- aprovar uma compra reprovada pelo motor;
- acessar o banco diretamente;
- executar alterações sem confirmação;
- inventar valores ausentes;
- substituir validações ou testes.

O backend envia à IA somente um DTO reduzido, sem descrições sensíveis quando elas não forem necessárias. A resposta deve seguir JSON Schema e passar por validação antes do uso.

## Estratégia de provedores

```text
ExplicadorDecisao
  |- ExplicadorPorTemplate
  |- ExplicadorOllama
  |- ExplicadorGemini
  `- ExplicadorGitHubModels
```

O provedor é configuração, não regra de domínio.

## Opções sem custo obrigatório

### Ollama

Executa localmente no Windows. Não cobra por requisição, mas consome armazenamento, memória, CPU/GPU e energia. É a opção de maior privacidade.

### Gemini Developer API

Possui modelos com free tier e tokens sem cobrança dentro dos limites vigentes. No free tier, a documentação informa que o conteúdo pode ser usado para melhorar produtos. Portanto, usar somente dados anonimizados e nunca enviar CSV bruto, nomes de estabelecimentos, observações pessoais ou saldos identificáveis.

### GitHub Models

Oferece uso gratuito de API para experimentação, com limites por minuto, dia, tokens e concorrência. A oferta gratuita é preview e pode mudar. É adequada para desenvolvimento, testes de prompts e baixo volume, não como dependência garantida de produção.

## Decisão recomendada

- O motor e a explicação por templates são obrigatórios.
- Ollama é a opção padrão quando privacidade for prioridade.
- Gemini free tier pode ser opção simples para computadores modestos, com envio anonimizado e consentimento.
- GitHub Models fica preferencialmente para desenvolvimento.
- Nenhum free tier deve ser tratado como disponibilidade permanente.
- A aplicação deve permitir desligar toda integração de IA.

Credenciais de APIs remotas ficam em variável de ambiente ou cofre local, nunca no Git.

## Fallback

Se o provedor estiver indisponível, limitado, lento ou retornar estrutura inválida, mostrar a explicação determinística gerada por templates.

## Consequências

- Decisões permanecem reproduzíveis.
- É possível experimentar IA sem acoplar o domínio ao modelo ou provedor.
- IA local exige mais recursos do computador.
- IA remota gratuita reduz o requisito de hardware, mas reduz privacidade e não garante gratuidade futura.
- Prompts, schemas e modelos usados precisam ser versionados e testados.

## Referências

- [Ollama no Windows](https://docs.ollama.com/windows)
- [Ollama API](https://docs.ollama.com/api/introduction)
- [Ollama structured outputs](https://docs.ollama.com/capabilities/structured-outputs)
- [Gemini Developer API - preços e free tier](https://ai.google.dev/gemini-api/docs/pricing)
- [GitHub Models - prototipação e limites gratuitos](https://docs.github.com/en/github-models/use-github-models/prototyping-with-ai-models)
