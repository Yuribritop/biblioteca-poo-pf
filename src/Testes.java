import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class Testes {

    private static int falhas;

    private Testes() {
    }

    public static void main(String[] args) {
        LocalDate referencia = Main.DATA_REFERENCIA_PADRAO;
        Biblioteca biblioteca = Main.criarBibliotecaDeExemplo();
        List<Emprestimo> emprestimos = biblioteca.listarEmprestimos();

        verificar(Relatorios.calcularMulta(5, Relatorios.MULTA_DIARIA).compareTo(new BigDecimal("10.00")) == 0,
                "multa de 5 dias vale R$ 10,00");
        verificar(Relatorios.calcularMulta(0, Relatorios.MULTA_DIARIA).signum() == 0,
                "sem atraso não há multa");
        verificar(Relatorios.filtrarPorCategoria(biblioteca.listarLivros(), "Tecnologia").size() == 2,
                "dois livros de Tecnologia");
        verificar(Relatorios.emprestimosAtrasados(emprestimos, referencia).size() == 3,
                "três empréstimos atrasados");
        verificar(Relatorios.gerarRelatorioMultas(emprestimos, referencia).total().compareTo(new BigDecimal("76.00")) == 0,
                "total de multas R$ 76,00");
        verificar(Relatorios.quantidadeEmprestimosAtivos(emprestimos) == 4,
                "quatro empréstimos ativos");
        verificar(ComparacaoParadigmas.totalMultasImperativo(emprestimos, referencia)
                        .compareTo(ComparacaoParadigmas.totalMultasFuncional(emprestimos, referencia)) == 0,
                "versões imperativa e funcional iguais");
        verificar(lancaExcecao(emprestimos::clear),
                "lista retornada pela biblioteca é somente leitura");
        verificar(lancaExcecao(() -> biblioteca.registrarEmprestimo(1, 3, referencia, 14)),
                "livro emprestado não pode ser emprestado de novo");
        verificar(lancaExcecao(() -> biblioteca.registrarDevolucao(1, referencia)),
                "empréstimo devolvido não pode ser devolvido de novo");

        Emprestimo antes = biblioteca.buscarEmprestimo(2).orElseThrow();
        biblioteca.registrarDevolucao(2, referencia);
        verificar(antes.estaAtivo() && !biblioteca.buscarEmprestimo(2).orElseThrow().estaAtivo(),
                "devolução cria novo objeto e não altera o anterior");
        verificar(biblioteca.livroDisponivel(1),
                "livro devolvido volta a ficar disponível");
        verificar(emprestimos.size() == 5 && emprestimos.get(1).estaAtivo(),
                "lista consultada antes da devolução não mudou");

        System.out.println(falhas == 0 ? "Todos os testes passaram" : falhas + " teste(s) falharam");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    private static void verificar(boolean condicao, String descricao) {
        System.out.println((condicao ? "[ok]    " : "[FALHA] ") + descricao);
        if (!condicao) {
            falhas++;
        }
    }

    private static boolean lancaExcecao(Runnable operacao) {
        try {
            operacao.run();
            return false;
        } catch (RuntimeException erro) {
            return true;
        }
    }
}
