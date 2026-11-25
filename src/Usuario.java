import java.util.Objects;

public final class Usuario {

    private final int id;
    private final String nome;
    private final String matricula;

    public Usuario(int id, String nome, String matricula) {
        this.id = id;
        this.nome = Objects.requireNonNull(nome, "nome");
        this.matricula = Objects.requireNonNull(matricula, "matricula");
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getMatricula() {
        return matricula;
    }

    @Override
    public String toString() {
        return nome + " (" + matricula + ")";
    }
}
