package com.example.minhabiblioteca.models;

public class Livro {
    private int id;
    private String titulo;
    private String autor;
    private String genero;
    private int paginas;
    private String status;
    private String anotacoes;

    // Construtor vazio (necessário para quando criamos o objeto e depois setamos os valores)
    public Livro() {
    }

    // Construtor para criar um novo livro (antes de inserir no BD, pois o ID é autogerado)
    public Livro(String titulo, String autor, String genero, int paginas, String status, String anotacoes) {
        this.titulo = titulo;
        this.autor = autor;
        this.genero = genero;
        this.paginas = paginas;
        this.status = status;
        this.anotacoes = anotacoes;
    }

    // Getters
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getGenero() { return genero; }
    public int getPaginas() { return paginas; }
    public String getStatus() { return status; }
    public String getAnotacoes() { return anotacoes; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setAutor(String autor) { this.autor = autor; }
    public void setGenero(String genero) { this.genero = genero; }
    public void setPaginas(int paginas) { this.paginas = paginas; }
    public void setStatus(String status) { this.status = status; }
    public void setAnotacoes(String anotacoes) { this.anotacoes = anotacoes; }

    // Opcional: toString() para facilitar a depuração ao imprimir objetos Livro
    @Override
    public String toString() {
        return "Livro{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                '}';
    }
}
