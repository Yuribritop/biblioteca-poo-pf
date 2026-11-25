# Sistema de Gerenciamento de Biblioteca

Atividade prática de Linguagens de Programação: Orientação a Objetos + Programação Funcional.

## Integrantes

- NOME COMPLETO DO INTEGRANTE 1
- NOME COMPLETO DO INTEGRANTE 2

## Linguagem

Java 17 ou superior (testado com OpenJDK 21). Sem dependências externas e sem banco de dados: os dados ficam em memória.

## Como executar

Na pasta do projeto:

```bash
javac -encoding UTF-8 -d out src/*.java
java -cp out Main
```

A data de referência padrão é 25/11/2026. Para usar outra data (formato `AAAA-MM-DD`):

```bash
java -cp out Main 2026-12-10
```

Testes de verificação das regras:

```bash
java -cp out Testes
```

## Estrutura

| Arquivo | Responsabilidade |
|---|---|
| `Livro.java` | Livro do acervo (id, título, autor, categoria). Imutável. |
| `Usuario.java` | Usuário da biblioteca (id, nome, matrícula). Imutável. |
| `Emprestimo.java` | Liga um `Livro` a um `Usuario` com datas. Imutável: a devolução cria um novo objeto (`comDevolucao`). Sabe se está ativo, se está atrasado e quantos dias de atraso tem. |
| `Biblioteca.java` | Único lugar com estado mutável. Cadastra e consulta livros e usuários, registra empréstimos e devoluções e entrega cópias somente leitura dos dados. |
| `Relatorios.java` | Funções puras: filtro por categoria, cálculo de multa, empréstimos atrasados, relatório de multas e estatísticas. Também define os records `ItemMulta`, `RelatorioMultas` e `Estatisticas`. |
| `ComparacaoParadigmas.java` | Total de multas em duas versões: imperativa e funcional. |
| `Main.java` | Monta os dados de exemplo e demonstra todas as funcionalidades no console. |
| `Testes.java` | Verifica as regras de negócio e os resultados esperados. |

## Relacionamentos

- `Biblioteca` 1 — 0..* `Livro`, 1 — 0..* `Usuario`, 1 — 0..* `Emprestimo` (composição: a biblioteca guarda e gerencia todos).
- `Emprestimo` * — 1 `Livro` e * — 1 `Usuario` (associação: cada empréstimo tem exatamente um livro e um usuário).
- `Usuario` 1 — 0..* `Emprestimo` (um usuário pode ter vários empréstimos).
- `Relatorios` e `ComparacaoParadigmas` dependem de `Livro` e `Emprestimo`, mas não guardam estado.

## Funcionalidades

- Cadastro e consulta de livros e usuários (`cadastrarLivro`, `cadastrarUsuario`, `buscarLivro`, `buscarUsuario`, `listarLivros`, `listarUsuarios`).
- Registro de empréstimos e devoluções (`registrarEmprestimo`, `registrarDevolucao`).
- Consulta de empréstimos (`listarEmprestimos`, `buscarEmprestimo`, `listarEmprestimosDoUsuario`, `livroDisponivel`).
- Filtro de livros por categoria (`filter`).
- Cálculo de multa por atraso (R$ 2,00 por dia).
- Lista de empréstimos ativos em atraso (`filter`).
- Relatório de multas: `filter` → `map` → `reduce`.
- Estatísticas: livros por categoria, empréstimos por usuário, empréstimos ativos (`reduce`), empréstimos ativos em atraso e valor total das multas (`reduce`).
- Comparação entre as versões imperativa e funcional do total de multas.

## Regras de negócio

1. Cada empréstimo tem um único livro e um único usuário; um usuário pode ter vários empréstimos.
2. Um livro com empréstimo ativo não pode ser emprestado de novo (`registrarEmprestimo` lança `IllegalStateException`).
3. Cada livro é um exemplar único.
4. A devolução registra a data e libera o livro para novo empréstimo.
5. Atrasado = ainda não devolvido e data de referência depois da data prevista.
6. Multa de R$ 2,00 por dia de atraso; sem atraso, multa zero.
7. O relatório de multas considera apenas empréstimos ativos e atrasados, com a data de referência recebida como parâmetro.
8. Empréstimos já devolvidos não geram multa.

## Programação funcional

### Funções puras (`Relatorios`)

| Função | Operação | Recebe | Devolve |
|---|---|---|---|
| `filtrarPorCategoria` | `filter` | lista de livros, categoria | nova lista com os livros da categoria |
| `calcularMulta` | — | dias de atraso, multa diária | valor da multa |
| `emprestimosAtrasados` | `filter` | lista de empréstimos, data de referência | nova lista com os ativos em atraso |
| `paraItemMulta` | usada no `map` | empréstimo, data de referência | `ItemMulta` com dias e valor |
| `somarMultas` | `map` + `reduce` | lista de `ItemMulta` | soma dos valores |
| `gerarRelatorioMultas` | `filter` + `map` + `reduce` | lista de empréstimos, data de referência | `RelatorioMultas` |
| `livrosPorCategoria` | agrupamento | lista de livros | mapa somente leitura categoria → quantidade |
| `emprestimosPorUsuario` | agrupamento | lista de empréstimos | mapa somente leitura usuário → quantidade |
| `quantidadeEmprestimosAtivos` | `filter` + `map` + `reduce` | lista de empréstimos | quantidade |
| `quantidadeEmprestimosAtrasados` | `filter` | lista de empréstimos, data de referência | quantidade |
| `gerarEstatisticas` | composição das anteriores | livros, empréstimos, data de referência | `Estatisticas` |

Os métodos `estaAtivo`, `estaAtrasado` e `diasDeAtraso` de `Emprestimo` também são puros: dependem apenas dos campos imutáveis do objeto e do parâmetro recebido.

Nenhuma função pura lê a data do sistema, imprime na tela ou altera o que recebe. Por isso a mesma entrada sempre produz a mesma saída.

### Fluxo do relatório de multas

1. `Biblioteca.listarEmprestimos()` — impura (lê o estado mutável da biblioteca) — devolve cópia somente leitura da lista.
2. `Relatorios.emprestimosAtrasados(emprestimos, data)` — pura, `filter` — devolve só os ativos em atraso.
3. `Relatorios.paraItemMulta(emprestimo, data)` — pura, `map` — transforma cada empréstimo em `ItemMulta` usando `calcularMulta`.
4. `Relatorios.somarMultas(itens)` — pura, `reduce` — soma os valores.
5. `Relatorios.gerarRelatorioMultas` monta o `RelatorioMultas` (data, itens, total).
6. `Main.exibirRelatorioMultas(relatorio)` — impura (escreve no console).

### Imutabilidade

- `Livro`, `Usuario` e `Emprestimo` têm todos os campos `private final` e nenhum setter.
- A devolução não altera o empréstimo: `comDevolucao` cria outro objeto, e a `Biblioteca` troca o antigo pelo novo.
- `listarLivros`, `listarUsuarios` e `listarEmprestimos` devolvem `List.copyOf(...)`: quem recebe não consegue alterar a biblioteca.
- As funções de `Relatorios` usam streams, que criam novas listas (`toList()`) e mapas somente leitura; a coleção original nunca é modificada.
- Os records `ItemMulta`, `RelatorioMultas` e `Estatisticas` são imutáveis; `RelatorioMultas` copia a lista recebida.

### Separação entre puro e impuro

- Impuro (altera estado ou faz entrada/saída): `Biblioteca.cadastrar*`, `registrarEmprestimo`, `registrarDevolucao` e os métodos de exibição de `Main`.
- Puro: tudo em `Relatorios`, `ComparacaoParadigmas` e os métodos de consulta de `Emprestimo`.
- `registrarEmprestimo` e `registrarDevolucao` são `synchronized` para que a verificação "livro disponível" e o registro aconteçam juntos, evitando que duas threads emprestem o mesmo livro ao mesmo tempo.

## Comparação entre paradigmas

`ComparacaoParadigmas` calcula o total de multas de duas formas:

- `totalMultasImperativo`: laço `for`, `if` e variável acumuladora `total` que muda a cada volta.
- `totalMultasFuncional`: `filter` → `map` → `reduce`, sem variável que muda.

A saída do `Main` e o `Testes` mostram que as duas dão o mesmo resultado (R$ 76,00 nos dados de exemplo).

## Dados de exemplo e resultado esperado (data de referência 25/11/2026)

| Empréstimo | Livro | Usuário | Prevista | Situação | Multa |
|---|---|---|---|---|---|
| #1 | O Cortiço | Carla Mendes | 15/10/2026 | devolvido em 20/10/2026 | — |
| #2 | Dom Casmurro | Ana Souza | 03/11/2026 | 22 dias de atraso | R$ 44,00 |
| #3 | Código Limpo | Bruno Lima | 15/11/2026 | 10 dias de atraso | R$ 20,00 |
| #4 | O Cortiço | Bruno Lima | 19/11/2026 | 6 dias de atraso | R$ 12,00 |
| #5 | Sapiens | Ana Souza | 29/11/2026 | no prazo | — |

Total de multas: R$ 76,00. Empréstimos ativos: 4. Ativos em atraso: 3.
