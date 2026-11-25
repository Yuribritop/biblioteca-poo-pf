import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class ComparacaoParadigmas {

    private ComparacaoParadigmas() {
    }

    public static BigDecimal totalMultasImperativo(List<Emprestimo> emprestimos, LocalDate dataReferencia) {
        BigDecimal total = BigDecimal.ZERO;
        for (Emprestimo emprestimo : emprestimos) {
            if (emprestimo.estaAtrasado(dataReferencia)) {
                long dias = emprestimo.diasDeAtraso(dataReferencia);
                total = total.add(Relatorios.MULTA_DIARIA.multiply(BigDecimal.valueOf(dias)));
            }
        }
        return total;
    }

    public static BigDecimal totalMultasFuncional(List<Emprestimo> emprestimos, LocalDate dataReferencia) {
        return emprestimos.stream()
                .filter(emprestimo -> emprestimo.estaAtrasado(dataReferencia))
                .map(emprestimo -> Relatorios.calcularMulta(emprestimo.diasDeAtraso(dataReferencia), Relatorios.MULTA_DIARIA))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
