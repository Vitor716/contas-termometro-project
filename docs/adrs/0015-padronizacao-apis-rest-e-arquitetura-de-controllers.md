# ADR 0015: Padronização de APIs REST e Arquitetura de Controllers

**Status:** Aceito
**Data:** 25 de junho de 2026
**Autores:** [Seu Nome/Sua Equipe]

## 1. Contexto
Durante a evolução da aplicação `ContasTermometro`, notou-se uma variação na forma como os endpoints REST estavam sendo construídos. Foram identificados cenários com uso de verbos nas URLs, inconsistência nos retornos de HTTP Status (como `200 OK` para criação de recursos), nomenclaturas divergentes entre as camadas de Controller e Service, e vazamento de contexto (Controllers acessando diretamente Repositories ou Services de outros domínios).

Para garantir a escalabilidade, legibilidade (Clean Code) e adequação rigorosa ao modelo de maturidade REST, faz-se necessário definir um padrão universal para a construção das APIs do projeto.

## 2. Decisão
Fica estabelecido que todas as novas APIs REST criadas, bem como a refatoração das existentes, devem seguir rigorosamente as regras abaixo:

### 2.1. Modelagem de URIs (Rotas)
* **Recursos no Plural:** As URLs devem ser baseadas em substantivos no plural, representando os recursos manipulados.
    * *Correto:* `/api/importacoes`, `/api/lancamentos`
    * *Incorreto:* `/api/importacao`, `/api/lancamento`
* **Ausência de Verbos nas Rotas:** A ação a ser executada deve ser inferida pelo método HTTP e não pela URL.
    * *Correto:* `POST /api/importacoes/nubank`
    * *Incorreto:* `POST /api/importacoes/upload/nubank`
* **Rotas Aninhadas:** Para sub-recursos, deve-se manter a semântica hierárquica. Exemplo: `/api/configuracoes/termometros/snapshots/{mes}`.

### 2.2. Passagem de Parâmetros
* **`@PathVariable`:** Uso exclusivo para identificação obrigatória de um recurso específico (Ex: `/{id}`).
* **`@RequestParam`:** Uso para filtros refinados, parâmetros opcionais, paginação e ordenação (Ex: `?mes=2026-06`).

### 2.3. Padronização de Status HTTP e Retornos
A resposta (`ResponseEntity`) deve refletir exatamente o resultado da operação:
* **GET / PUT:** `200 OK` (retornando o objeto ou lista).
* **POST:** `201 Created` (retornando o objeto recém-criado).
* **DELETE:** `204 No Content` (utilizando o tipo de retorno genérico `Void` e não retornando corpo na resposta).

### 2.4. Glossário de Nomenclatura (Controller ↔ Service)
Os nomes dos métodos no Controller devem omitir o nome do recurso (já inferido pela classe/rota) e devem ser exatamente os mesmos na camada de Service:
* `listar()`: Para busca de múltiplos itens (`List<T>` ou `Page<T>`).
* `buscarPorId(id)`: Para busca de um recurso específico via identificador.
* `buscar()` ou `obter()`: Para busca de recursos únicos baseados em contexto (ex: um resumo mensal).
* `criar(request)`: Para rotas `POST`.
* `editar(id, request)`: Para rotas `PUT`.
* `deletar(id)`: Para rotas `DELETE`.

### 2.5. Isolamento de Domínio e Camadas
* O Controller só deve possuir dependências (via injeção) da camada de **Service** correspondente ao seu próprio domínio.
* **Proibido:** Injetar instâncias de `Repository` diretamente no Controller.
* Se um Controller precisar de dados de outro contexto (Bounded Context), a comunicação deve ser feita por meio da composição das chamadas via Service ou transferindo a responsabilidade da rota para o controlador correspondente àquele domínio.

## 3. Consequências

### Positivas
* **Previsibilidade:** Desenvolvedores e consumidores da API saberão exatamente o formato esperado da URL, retornos e nomes de métodos.
* **Manutenibilidade:** Menor acoplamento entre módulos, facilitando testes unitários e futuras migrações ou refatorações de domínios específicos.
* **Integração:** Ferramentas de documentação automática (como Swagger/OpenAPI) irão gerar especificações mais limpas e semânticas.

### Negativas / Pontos de Atenção
* **Refatoração Imediata:** Rotas existentes precisarão ser atualizadas, o que pode quebrar contratos temporariamente.
* **Alinhamento de Front-end:** As aplicações clientes (web/mobile) precisarão atualizar as URLs de acesso e os mapeamentos de HTTP Status, especialmente ao escutar o código `201` no lugar do `200` para criações e o `204` para deleções.