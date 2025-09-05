package com.example.minhabiblioteca.data;

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.content.ContentValues;
import android.content.Context;
// Removidos imports duplicados de Cursor, SQLiteDatabase, Log, ArrayList, List
import android.database.sqlite.SQLiteOpenHelper;
import com.example.minhabiblioteca.models.Livro;

public class LivroDatabaseHelper extends SQLiteOpenHelper { // ABERTURA DA CLASSE

    // Constantes e membros da classe devem vir PRIMEIRO ou ANTES dos métodos que os usam
    private static final String TAG = "LivroDbHelper"; // Tag para Logs

    // Informações do Banco de Dados
    private static final String DATABASE_NAME = "biblioteca.db";
    private static final int DATABASE_VERSION = 1; // Incremente se mudar o esquema

    // Nomes da Tabela e Colunas
    public static final String TABLE_LIVROS = "livros";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITULO = "titulo";
    public static final String COLUMN_AUTOR = "autor";
    public static final String COLUMN_GENERO = "genero";
    public static final String COLUMN_PAGINAS = "paginas";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_ANOTACOES = "anotacoes";

    // Comando SQL para criar a tabela de livros
    private static final String CREATE_TABLE_LIVROS_SQL =
            "CREATE TABLE " + TABLE_LIVROS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_TITULO + " TEXT NOT NULL," +
                    COLUMN_AUTOR + " TEXT NOT NULL," +
                    COLUMN_GENERO + " TEXT NOT NULL," +
                    COLUMN_PAGINAS + " INTEGER NOT NULL," +
                    COLUMN_STATUS + " TEXT NOT NULL," +
                    COLUMN_ANOTACOES + " TEXT" +
                    ")";

    // Construtor
    public LivroDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: Criando tabela livros: " + CREATE_TABLE_LIVROS_SQL);
        db.execSQL(CREATE_TABLE_LIVROS_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade: Atualizando banco de dados da versão " + oldVersion + " para " +
                newVersion + ". Todos os dados antigos da tabela '" + TABLE_LIVROS + "' serão perdidos.");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIVROS);
        onCreate(db);
    }

    // Agora o método listarLivrosFiltrados está DENTRO da classe
    public List<Livro> listarLivrosFiltrados(@Nullable String genero, @Nullable String status) {
        List<Livro> listaLivros = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + TABLE_LIVROS);
        List<String> selectionArgs = new ArrayList<>();
        boolean hasWhereClause = false;

        if (genero != null && !genero.isEmpty() && !"Todos os Gêneros".equalsIgnoreCase(genero)) {
            queryBuilder.append(" WHERE ").append(COLUMN_GENERO).append(" = ?");
            selectionArgs.add(genero);
            hasWhereClause = true;
        }

        if (status != null && !status.isEmpty() && !"Todos os Status".equalsIgnoreCase(status)) {
            if (hasWhereClause) {
                queryBuilder.append(" AND ");
            } else {
                queryBuilder.append(" WHERE ");
                hasWhereClause = true;
            }
            queryBuilder.append(COLUMN_STATUS).append(" = ?");
            selectionArgs.add(status);
        }

        String query = queryBuilder.toString();
        String[] argsArray = selectionArgs.toArray(new String[0]);

        Log.d("DBHelper_Filtro", "Query: " + query + " Args: " + Arrays.toString(argsArray));

        try {
            cursor = db.rawQuery(query, argsArray);
            if (cursor.moveToFirst()) {
                do {
                    Livro livro = new Livro();
                    livro.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    livro.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITULO)));
                    livro.setAutor(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTOR)));
                    livro.setGenero(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENERO)));
                    livro.setPaginas(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PAGINAS)));
                    livro.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
                    livro.setAnotacoes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANOTACOES))); // Adicionado para consistência
                    listaLivros.add(livro);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DBHelper_Filtro", "Erro ao listar livros filtrados", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return listaLivros;
    }


    public boolean adicionarLivro(Livro livro) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITULO, livro.getTitulo());
        values.put(COLUMN_AUTOR, livro.getAutor());
        values.put(COLUMN_GENERO, livro.getGenero());
        values.put(COLUMN_PAGINAS, livro.getPaginas());
        values.put(COLUMN_STATUS, livro.getStatus());
        values.put(COLUMN_ANOTACOES, livro.getAnotacoes());

        long resultado = -1;
        try {
            resultado = db.insertOrThrow(TABLE_LIVROS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "adicionarLivro: Erro ao inserir livro '" + livro.getTitulo() + "': " + e.getMessage());
        }

        if (resultado != -1) {
            Log.d(TAG, "adicionarLivro: Livro '" + livro.getTitulo() + "' inserido com ID: " + resultado);
            return true;
        }
        return false;
    }

    public List<Livro> listarLivros() {
        List<Livro> listaLivros = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        String query = "SELECT * FROM " + TABLE_LIVROS + " ORDER BY " + COLUMN_TITULO + " ASC";
        Log.d(TAG, "listarLivros: Executando query: " + query);

        try {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    Livro livro = new Livro();
                    livro.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    livro.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITULO)));
                    livro.setAutor(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTOR)));
                    livro.setGenero(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENERO)));
                    livro.setPaginas(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PAGINAS)));
                    livro.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
                    livro.setAnotacoes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANOTACOES)));
                    listaLivros.add(livro);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "listarLivros: Erro ao listar livros: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG, "listarLivros: " + listaLivros.size() + " livros encontrados.");
        return listaLivros;
    }

    public int editarLivro(Livro livro) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITULO, livro.getTitulo());
        values.put(COLUMN_AUTOR, livro.getAutor());
        values.put(COLUMN_GENERO, livro.getGenero());
        values.put(COLUMN_PAGINAS, livro.getPaginas());
        values.put(COLUMN_STATUS, livro.getStatus());
        values.put(COLUMN_ANOTACOES, livro.getAnotacoes());

        int rowsAffected = 0;
        try {
            String selection = COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(livro.getId())};
            rowsAffected = db.update(TABLE_LIVROS, values, selection, selectionArgs);
        } catch (Exception e) {
            Log.e(TAG, "editarLivro: Erro ao editar livro ID " + livro.getId() + ": " + e.getMessage());
        }

        if (rowsAffected > 0) {
            Log.d(TAG, "editarLivro: Livro ID " + livro.getId() + " atualizado com sucesso.");
        } else {
            Log.w(TAG, "editarLivro: Nenhuma linha afetada ao tentar editar livro ID " + livro.getId() + ". O livro existe?");
        }
        return rowsAffected;
    }

    public int excluirLivro(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = 0;
        try {
            String selection = COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(id)};
            rowsDeleted = db.delete(TABLE_LIVROS, selection, selectionArgs);
        } catch (Exception e) {
            Log.e(TAG, "excluirLivro: Erro ao excluir livro ID " + id + ": " + e.getMessage());
        }
        if (rowsDeleted > 0) {
            Log.d(TAG, "excluirLivro: Livro ID " + id + " excluído com sucesso.");
        } else {
            Log.w(TAG, "excluirLivro: Nenhuma linha afetada ao tentar excluir livro ID " + id + ". O livro existe?");
        }
        return rowsDeleted;
    }

    public Livro getLivroPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Livro livro = null;

        try {
            String selection = COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(id)};
            cursor = db.query(
                    TABLE_LIVROS,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                livro = new Livro();
                livro.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                livro.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITULO)));
                livro.setAutor(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTOR)));
                livro.setGenero(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENERO)));
                livro.setPaginas(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PAGINAS)));
                livro.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
                livro.setAnotacoes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANOTACOES)));
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "getLivroPorId: Erro ao obter índice da coluna: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "getLivroPorId: Erro ao buscar livro por ID " + id + ": " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return livro;
    }

} // <--- ESTE É O FECHAMENTO CORRETO DA CLASSE LivroDatabaseHelper
