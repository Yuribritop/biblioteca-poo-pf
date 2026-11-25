import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class Relatorios {

    public static final BigDecimal MULTA_DIARIA = new BigDecimal("2.00");

    public record ItemMulta(int emprestimoId, String usuario, String livro,
                            LocalDate dataPrevistaDevolucao, long diasDeAtraso, BigDecimal valor) {
    }

    public record RelatorioMultas(LocalDate dataReferencia, List<ItemMulta> itens, BigDecimal total) {
        public RelatorioMultas {
            itens = List.copyOf(itens);
        }
    }

    public record Estatisticas(Map<String, Long> livrosPorCategoria, Map<String, Long> emprestimosPorUsuario,
                               long emprestimosAtivos, long emprestimosAtrasados, BigDecimal totalMultas) {
    }

    private Relatorios() {
    }

    public static List<Livro> filtrarPorCategoria(List<Livro> livros, String categoria) {
        return livros.stream()
                .filter(livro -> livro.getCategoria().equalsIgnoreCase(categoria))
                .toList();
    }

    public static BigDecimal calcularMulta(long diasDeAtraso, BigDecimal multaDiaria) {
        return multaDiaria.multiply(BigDecimal.valueOf(Math.max(0, diasDeAtraso)));
    }

    public static List<Emprestimo> emprestimosAtrasados(List<Emprestimo> emprestimos, LocalDate dataReferencia) {
        return emprestimos.stream()
                .filter(emprestimo -> emprestimo.estaAtrasado(dataReferencia))
                .toList();
    }

    public static ItemMulta paraItemMulta(Emprestimo emprestimo, LocalDate dataReferencia) {
        long dias = emprestimo.diasDeAtraso(dataReferencia);
        return new ItemMulta(emprestimo.getId(), emprestimo.getUsuario().getNome(), emprestimo.getLivro().getTitulo(),
                emprestimo.getDataPrevistaDevolucao(), dias, calcularMulta(dias, MULTA_DIARIA));
    }

    public static BigDecimal somarMultas(List<ItemMulta> itens) {
        return itens.stream()
                .map(ItemMulta::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static RelatorioMultas gerarRelatorioMultas(List<Emprestimo> emprestimos, LocalDate dataReferencia) {
        List<ItemMulta> itens = emprestimosAtrasados(emprestimos, dataReferencia).stream()
                .map(emprestimo -> paraItemMulta(emprestimo, dataReferencia))
                .toList();
        return new RelatorioMultas(dataReferencia, itens, somarMultas(itens));
    }

    public static Map<String, Long> livrosPorCategoria(List<Livro> livros) {
        return livros.stream().collect(Collectors.collectingAndThen(
                Collectors.groupingBy(Livro::getCategoria, TreeMap::new, Collectors.counting()),
                Collections::unmodifiableMap));
    }

    public static Map<String, Long> emprestimosPorUsuario(List<Emprestimo> emprestimos) {
        return emprestimos.stream().collect(Collectors.collectingAndThen(
                Collectors.groupingBy(emprestimo -> emprestimo.getUsuario().toString(), TreeMap::new, Collectors.counting()),
                Collections::unmodifiableMap));
    }

    public static long quantidadeEmprestimosAtivos(List<Emprestimo> emprestimos) {
        return emprestimos.stream()
                .filter(Emprestimo::estaAtivo)
                .map(emprestimo -> 1L)
                .reduce(0L, Long::sum);
    }

    public static long quantidadeEmprestimosAtrasados(List<Emprestimo> emprestimos, LocalDate dataReferencia) {
        return emprestimosAtrasados(emprestimos, dataReferencia).size();
    }

    public static Estatisticas gerarEstatisticas(List<Livro> livros, List<Emprestimo> emprestimos, LocalDate dataReferencia) {
        return new Estatisticas(
                livrosPorCategoria(livros),
                emprestimosPorUsuario(emprestimos),
                quantidadeEmprestimosAtivos(emprestimos),
                quantidadeEmprestimosAtrasados(emprestimos, dataReferencia),
                gerarRelatorioMultas(emprestimos, dataReferencia).total());
    }
}
