import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Biblioteca {

    private final Map<Integer, Livro> livros = new LinkedHashMap<>();
    private final Map<Integer, Usuario> usuarios = new LinkedHashMap<>();
    private final Map<Integer, Emprestimo> emprestimos = new LinkedHashMap<>();
    private int proximoIdEmprestimo = 1;

    public void cadastrarLivro(Livro livro) {
        if (livros.putIfAbsent(livro.getId(), livro) != null) {
            throw new IllegalArgumentException("Já existe livro com id " + livro.getId());
        }
    }

    public void cadastrarUsuario(Usuario usuario) {
        if (usuarios.putIfAbsent(usuario.getId(), usuario) != null) {
            throw new IllegalArgumentException("Já existe usuário com id " + usuario.getId());
        }
    }

    public Optional<Livro> buscarLivro(int id) {
        return Optional.ofNullable(livros.get(id));
    }

    public Optional<Usuario> buscarUsuario(int id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    public Optional<Emprestimo> buscarEmprestimo(int id) {
        return Optional.ofNullable(emprestimos.get(id));
    }

    public List<Livro> listarLivros() {
        return List.copyOf(livros.values());
    }

    public List<Usuario> listarUsuarios() {
        return List.copyOf(usuarios.values());
    }

    public List<Emprestimo> listarEmprestimos() {
        return List.copyOf(emprestimos.values());
    }

    public List<Emprestimo> listarEmprestimosDoUsuario(int usuarioId) {
        return emprestimos.values().stream()
                .filter(emprestimo -> emprestimo.getUsuario().getId() == usuarioId)
                .toList();
    }

    public boolean livroDisponivel(int livroId) {
        return emprestimos.values().stream()
                .noneMatch(emprestimo -> emprestimo.estaAtivo() && emprestimo.getLivro().getId() == livroId);
    }

    public synchronized Emprestimo registrarEmprestimo(int livroId, int usuarioId, LocalDate data, int prazoEmDias) {
        Livro livro = buscarLivro(livroId)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado: " + livroId));
        Usuario usuario = buscarUsuario(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + usuarioId));
        if (!livroDisponivel(livroId)) {
            throw new IllegalStateException("O livro \"" + livro.getTitulo() + "\" já está emprestado");
        }
        Emprestimo emprestimo = new Emprestimo(proximoIdEmprestimo++, livro, usuario, data, data.plusDays(prazoEmDias));
        emprestimos.put(emprestimo.getId(), emprestimo);
        return emprestimo;
    }

    public synchronized Emprestimo registrarDevolucao(int emprestimoId, LocalDate data) {
        Emprestimo atual = buscarEmprestimo(emprestimoId)
                .orElseThrow(() -> new IllegalArgumentException("Empréstimo não encontrado: " + emprestimoId));
        Emprestimo devolvido = atual.comDevolucao(data);
        emprestimos.put(emprestimoId, devolvido);
        return devolvido;
    }
}
