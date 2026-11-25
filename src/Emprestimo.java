import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

public final class Emprestimo {

    private final int id;
    private final Livro livro;
    private final Usuario usuario;
    private final LocalDate dataEmprestimo;
    private final LocalDate dataPrevistaDevolucao;
    private final LocalDate dataDevolucao;

    public Emprestimo(int id, Livro livro, Usuario usuario, LocalDate dataEmprestimo, LocalDate dataPrevistaDevolucao) {
        this(id, livro, usuario, dataEmprestimo, dataPrevistaDevolucao, null);
    }

    private Emprestimo(int id, Livro livro, Usuario usuario, LocalDate dataEmprestimo,
                       LocalDate dataPrevistaDevolucao, LocalDate dataDevolucao) {
        this.id = id;
        this.livro = Objects.requireNonNull(livro, "livro");
        this.usuario = Objects.requireNonNull(usuario, "usuario");
        this.dataEmprestimo = Objects.requireNonNull(dataEmprestimo, "dataEmprestimo");
        this.dataPrevistaDevolucao = Objects.requireNonNull(dataPrevistaDevolucao, "dataPrevistaDevolucao");
        if (dataPrevistaDevolucao.isBefore(dataEmprestimo)) {
            throw new IllegalArgumentException("A data prevista não pode ser anterior à data do empréstimo");
        }
        this.dataDevolucao = dataDevolucao;
    }

    public int getId() {
        return id;
    }

    public Livro getLivro() {
        return livro;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public LocalDate getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }

    public Optional<LocalDate> getDataDevolucao() {
        return Optional.ofNullable(dataDevolucao);
    }

    public boolean estaAtivo() {
        return dataDevolucao == null;
    }

    public boolean estaAtrasado(LocalDate dataReferencia) {
        return estaAtivo() && dataReferencia.isAfter(dataPrevistaDevolucao);
    }

    public long diasDeAtraso(LocalDate dataReferencia) {
        return estaAtrasado(dataReferencia) ? ChronoUnit.DAYS.between(dataPrevistaDevolucao, dataReferencia) : 0;
    }

    public Emprestimo comDevolucao(LocalDate data) {
        Objects.requireNonNull(data, "data");
        if (!estaAtivo()) {
            throw new IllegalStateException("O empréstimo #" + id + " já foi devolvido");
        }
        if (data.isBefore(dataEmprestimo)) {
            throw new IllegalArgumentException("A devolução não pode ser anterior ao empréstimo");
        }
        return new Emprestimo(id, livro, usuario, dataEmprestimo, dataPrevistaDevolucao, data);
    }
}
