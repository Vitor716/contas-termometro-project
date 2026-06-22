# ADR 0009 - Captura local de CSV por e-mail

## Status

Proposta para melhoria futura.

## Contexto

Extratos CSV podem chegar por e-mail. Baixar e enviar manualmente o anexo é repetitivo, mas dar acesso amplo à caixa postal aumenta o risco de privacidade.

## Decisão proposta

Criar um conector opcional de e-mail, executado localmente, inicialmente para Gmail.

Fluxo:

1. usuário autoriza leitura via OAuth 2.0;
2. um job local consulta periodicamente mensagens usando filtros restritos;
3. somente anexos `.csv` de remetentes e assuntos permitidos são baixados;
4. o hash do anexo e o identificador da mensagem são registrados;
5. o arquivo entra na mesma pré-visualização da importação manual;
6. nenhuma linha é confirmada automaticamente;
7. após processamento, a mensagem pode receber uma marca local ou label, se o escopo autorizado permitir.

## Escolha técnica inicial

Preferir Gmail API em vez de senha IMAP:

- OAuth para aplicativo desktop;
- escopo mínimo `gmail.readonly` no primeiro corte;
- busca usando `from:`, `subject:`, `has:attachment` e nome/extensão do arquivo;
- polling local em intervalo configurável;
- tokens armazenados fora do Git.

IMAP com OAuth pode ser uma alternativa para abstrair provedores, mas não elimina autenticação e tende a exigir tratamento de sessão. Webhooks do Gmail exigem infraestrutura adicional e não combinam com o primeiro modo local-first.

## Segurança

- Nunca armazenar senha do e-mail.
- `credentials.json`, tokens e anexos reais ficam fora do Git.
- Permitir lista de remetentes confiáveis.
- Limitar tamanho e extensão.
- Validar conteúdo como CSV antes de processar.
- Não abrir macros ou formatos executáveis.
- Exibir a origem do anexo na revisão.
- Permitir revogar a autorização e apagar tokens.

## Execução no Windows

Primeira opção: job do próprio Spring enquanto a aplicação estiver aberta.

Evolução opcional: script registrado no Agendador de Tarefas do Windows para iniciar a sincronização em horários definidos.

## Consequências

- Reduz trabalho manual sem criar backend em cloud.
- Depende de internet e autorização do provedor de e-mail.
- A integração precisa ser opcional e desacoplada do parser Nubank.
- Outros provedores podem implementar o mesmo contrato futuramente.

## Referências

- [Gmail API - listar e filtrar mensagens](https://developers.google.com/workspace/gmail/api/guides/list-messages)
- [Gmail API - obter anexos](https://developers.google.com/workspace/gmail/api/reference/rest/v1/users.messages.attachments/get)
- [OAuth 2.0 para aplicativos desktop](https://developers.google.com/identity/protocols/oauth2/native-app)
- [Gmail com IMAP e OAuth 2.0](https://developers.google.com/workspace/gmail/imap/imap-smtp)
