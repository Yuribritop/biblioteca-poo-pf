import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class Main {

    static final LocalDate DATA_REFERENCIA_PADRAO = LocalDate.of(2026, 11, 25);
    private static final int PRAZO_EM_DIAS = 14;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat FORMATO_MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    private Main() {
    }

    public static void main(String[] args) {
        LocalDate dataReferencia = args.length > 0 ? LocalDate.parse(args[0]) : DATA_REFERENCIA_PADRAO;
        Biblioteca biblioteca = criarBibliotecaDeExemplo();

        exibirAcervoEUsuarios(biblioteca);
        exibirEmprestimos(biblioteca);
        demonstrarRegrasDeNegocio(biblioteca, dataReferencia);
        demonstrarEncapsulamento(biblioteca);
        demonstrarFiltroPorCategoria(biblioteca.listarLivros(), "Tecnologia");
        demonstrarFuncaoPura();
        exibirEmprestimosAtrasados(biblioteca.listarEmprestimos(), dataReferencia);
        exibirRelatorioMultas(Relatorios.gerarRelatorioMultas(biblioteca.listarEmprestimos(), dataReferencia));
        exibirEstatisticas(Relatorios.gerarEstatisticas(biblioteca.listarLivros(), biblioteca.listarEmprestimos(), dataReferencia));
        exibirComparacaoParadigmas(biblioteca.listarEmprestimos(), dataReferencia);
    }

    static Biblioteca criarBibliotecaDeExemplo() {
        Biblioteca biblioteca = new Biblioteca();
        biblioteca.cadastrarLivro(new Livro(1, "Dom Casmurro", "Machado de Assis", "Romance"));
        biblioteca.cadastrarLivro(new Livro(2, "O Cortiço", "Aluísio Azevedo", "Romance"));
        biblioteca.cadastrarLivro(new Livro(3, "Código Limpo", "Robert C. Martin", "Tecnologia"));
        biblioteca.cadastrarLivro(new Livro(4, "Entendendo Algoritmos", "Aditya Bhargava", "Tecnologia"));
        biblioteca.cadastrarLivro(new Livro(5, "Sapiens", "Yuval Noah Harari", "História"));
        biblioteca.cadastrarLivro(new Livro(6, "1984", "George Orwell", "Ficção"));

        biblioteca.cadastrarUsuario(new Usuario(1, "Ana Souza", "2024001"));
        biblioteca.cadastrarUsuario(new Usuario(2, "Bruno Lima", "2024002"));
        biblioteca.cadastrarUsuario(new Usuario(3, "Carla Mendes", "2024003"));

        Emprestimo corticoComCarla = biblioteca.registrarEmprestimo(2, 3, LocalDate.of(2026, 10, 1), PRAZO_EM_DIAS);
        biblioteca.registrarEmprestimo(1, 1, LocalDate.of(2026, 10, 20), PRAZO_EM_DIAS);
        biblioteca.registrarDevolucao(corticoComCarla.getId(), LocalDate.of(2026, 10, 20));
        biblioteca.registrarEmprestimo(3, 2, LocalDate.of(2026, 11, 1), PRAZO_EM_DIAS);
        biblioteca.registrarEmprestimo(2, 2, LocalDate.of(2026, 11, 5), PRAZO_EM_DIAS);
        biblioteca.registrarEmprestimo(5, 1, LocalDate.of(2026, 11, 15), PRAZO_EM_DIAS);
        return biblioteca;
    }

    private static void exibirAcervoEUsuarios(Biblioteca biblioteca) {
        titulo("Acervo");
        biblioteca.listarLivros().forEach(livro -> System.out.println("  " + livro
                + (biblioteca.livroDisponivel(livro.getId()) ? " | disponível" : " | emprestado")));
        titulo("Usuários");
        biblioteca.listarUsuarios().forEach(usuario -> System.out.println("  #" + usuario.getId() + " " + usuario));
    }

    private static void exibirEmprestimos(Biblioteca biblioteca) {
        titulo("Empréstimos");
        biblioteca.listarEmprestimos().forEach(emprestimo -> System.out.println("  " + descrever(emprestimo)));
        titulo("Consulta: empréstimos de Ana Souza");
        biblioteca.listarEmprestimosDoUsuario(1).forEach(emprestimo -> System.out.println("  " + descrever(emprestimo)));
    }

    private static void demonstrarRegrasDeNegocio(Biblioteca biblioteca, LocalDate dataReferencia) {
        titulo("Regras de negócio");
        tentar("Emprestar \"Dom Casmurro\" para Carla", () -> biblioteca.registrarEmprestimo(1, 3, dataReferencia, PRAZO_EM_DIAS));
        tentar("Devolver de novo o empréstimo #1", () -> biblioteca.registrarDevolucao(1, dataReferencia));
        tentar("Emprestar o livro inexistente #99", () -> biblioteca.registrarEmprestimo(99, 1, dataReferencia, PRAZO_EM_DIAS));
    }

    private static void demonstrarEncapsulamento(Biblioteca biblioteca) {
        titulo("Encapsulamento");
        tentar("Apagar o acervo pela lista de listarLivros()", () -> biblioteca.listarLivros().clear());
        System.out.println("  Livros cadastrados: " + biblioteca.listarLivros().size());
    }

    private static void demonstrarFiltroPorCategoria(List<Livro> acervo, String categoria) {
        titulo("Filtro por categoria: " + categoria);
        List<Livro> filtrados = Relatorios.filtrarPorCategoria(acervo, categoria);
        filtrados.forEach(livro -> System.out.println("  " + livro));
        System.out.println("  Coleção original: " + acervo.size() + " livros | filtrada: " + filtrados.size() + " livros");
    }

    private static void demonstrarFuncaoPura() {
        titulo("Função pura calcularMulta");
        BigDecimal primeira = Relatorios.calcularMulta(5, Relatorios.MULTA_DIARIA);
        BigDecimal segunda = Relatorios.calcularMulta(5, Relatorios.MULTA_DIARIA);
        System.out.println("  calcularMulta(5, 2,00) = " + moeda(primeira) + " | de novo = " + moeda(segunda)
                + " | iguais: " + primeira.equals(segunda));
        System.out.println("  calcularMulta(0, 2,00) = " + moeda(Relatorios.calcularMulta(0, Relatorios.MULTA_DIARIA)));
    }

    private static void exibirEmprestimosAtrasados(List<Emprestimo> emprestimos, LocalDate dataReferencia) {
        titulo("Empréstimos atrasados em " + dataReferencia.format(FORMATO_DATA));
        List<Emprestimo> atrasados = Relatorios.emprestimosAtrasados(emprestimos, dataReferencia);
        if (atrasados.isEmpty()) {
            System.out.println("  Nenhum");
        }
        atrasados.forEach(emprestimo -> System.out.println("  " + descrever(emprestimo)
                + " | " + emprestimo.diasDeAtraso(dataReferencia) + " dia(s) de atraso"));
    }

    private static void exibirRelatorioMultas(Relatorios.RelatorioMultas relatorio) {
        titulo("Relatório de multas em " + relatorio.dataReferencia().format(FORMATO_DATA)
                + " (" + moeda(Relatorios.MULTA_DIARIA) + " por dia)");
        relatorio.itens().forEach(item -> System.out.printf("  #%d %-22s %-13s prevista %s %3d dia(s)  %s%n",
                item.emprestimoId(), item.livro(), item.usuario(), item.dataPrevistaDevolucao().format(FORMATO_DATA),
                item.diasDeAtraso(), moeda(item.valor())));
        System.out.println("  Total: " + moeda(relatorio.total()));
    }

    private static void exibirEstatisticas(Relatorios.Estatisticas estatisticas) {
        titulo("Estatísticas");
        System.out.println("  Livros por categoria: " + estatisticas.livrosPorCategoria());
        System.out.println("  Empréstimos por usuário: " + estatisticas.emprestimosPorUsuario());
        System.out.println("  Empréstimos ativos: " + estatisticas.emprestimosAtivos());
        System.out.println("  Empréstimos ativos em atraso: " + estatisticas.emprestimosAtrasados());
        System.out.println("  Valor total das multas: " + moeda(estatisticas.totalMultas()));
    }

    private static void exibirComparacaoParadigmas(List<Emprestimo> emprestimos, LocalDate dataReferencia) {
        titulo("Comparação de paradigmas: total de multas");
        BigDecimal imperativo = ComparacaoParadigmas.totalMultasImperativo(emprestimos, dataReferencia);
        BigDecimal funcional = ComparacaoParadigmas.totalMultasFuncional(emprestimos, dataReferencia);
        System.out.println("  Imperativo: " + moeda(imperativo));
        System.out.println("  Funcional:  " + moeda(funcional));
        System.out.println("  Resultados equivalentes: " + (imperativo.compareTo(funcional) == 0));
    }

    private static String descrever(Emprestimo emprestimo) {
        String situacao = emprestimo.getDataDevolucao()
                .map(data -> "devolvido em " + data.format(FORMATO_DATA))
                .orElse("ativo");
        return "#" + emprestimo.getId() + " " + emprestimo.getLivro().getTitulo() + " | " + emprestimo.getUsuario().getNome()
                + " | retirada " + emprestimo.getDataEmprestimo().format(FORMATO_DATA)
                + " | prevista " + emprestimo.getDataPrevistaDevolucao().format(FORMATO_DATA)
                + " | " + situacao;
    }

    private static void tentar(String descricao, Runnable operacao) {
        try {
            operacao.run();
            System.out.println("  " + descricao + ": realizado");
        } catch (RuntimeException erro) {
            String motivo = Objects.requireNonNullElse(erro.getMessage(), erro.getClass().getSimpleName());
            System.out.println("  " + descricao + ": recusado (" + motivo + ")");
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("== " + texto + " ==");
    }

    private static String moeda(BigDecimal valor) {
        return FORMATO_MOEDA.format(valor);
    }
}
