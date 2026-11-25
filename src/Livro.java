import java.util.Objects;

public final class Livro {

    private final int id;
    private final String titulo;
    private final String autor;
    private final String categoria;

    public Livro(int id, String titulo, String autor, String categoria) {
        this.id = id;
        this.titulo = Objects.requireNonNull(titulo, "titulo");
        this.autor = Objects.requireNonNull(autor, "autor");
        this.categoria = Objects.requireNonNull(categoria, "categoria");
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getCategoria() {
        return categoria;
    }

    @Override
    public String toString() {
        return "#" + id + " " + titulo + " - " + autor + " [" + categoria + "]";
    }
}
