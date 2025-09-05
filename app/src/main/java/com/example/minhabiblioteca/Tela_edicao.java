package com.example.minhabiblioteca; // Ou seu pacote

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

// EdgeToEdge imports
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.minhabiblioteca.data.LivroDatabaseHelper;
import com.example.minhabiblioteca.models.Livro;

public class Tela_edicao extends AppCompatActivity {

    private static final String TAG = "Tela_edicao";
    // Mantém o mesmo nome para consistência com quem chama
    public static final String EXTRA_LIVRO_ID = "com.example.minhabiblioteca.EXTRA_LIVRO_ID";

    // Views para exibir informações fixas
    private TextView textViewTituloLivro, textViewAutorLivro, textViewGeneroLivro, textViewPaginasLivro;

    // Views para campos editáveis
    private RadioButton radioButtonJaLi, radioButtonWishlist, radioButtonLendo;
    private EditText editTextAnotacoes;

    // Botões de ação
    private ImageButton imageButtonVoltar;
    private Button buttonSalvar;
    private ImageButton imageButtonExcluir;

    private LivroDatabaseHelper dbHelper;
    private Livro livroAtualParaEditar = null;
    private int livroIdRecebido = -1;

    // Para gerenciar a seleção dos RadioButtons
    private RadioButton ultimoStatusSelecionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_edicao); // Seu XML fornecido
        View mainLayout = findViewById(R.id.main); // ID do ConstraintLayout raiz
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        dbHelper = new LivroDatabaseHelper(this);

        // --- Inicializar Views ---
        // Views de exibição (não editáveis)
        textViewTituloLivro = findViewById(R.id.txt_titulo_edit);
        textViewAutorLivro = findViewById(R.id.txt_autor_edit);
        textViewGeneroLivro = findViewById(R.id.txt_genero_edit);
        textViewPaginasLivro = findViewById(R.id.txt_pag_edit);

        // Views editáveis
        radioButtonJaLi = findViewById(R.id.btn_jali_edit);
        radioButtonWishlist = findViewById(R.id.btn_wishlist_edit);
        radioButtonLendo = findViewById(R.id.btn_lendo_edit);
        editTextAnotacoes = findViewById(R.id.txt_anotacoes_edit);

        // Botões
        imageButtonVoltar = findViewById(R.id.btn_back);
        buttonSalvar = findViewById(R.id.btn_salvar);
        imageButtonExcluir = findViewById(R.id.btn_excluir);

        configurarListenersDosRadioButtonsStatus();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_LIVRO_ID)) {
            livroIdRecebido = intent.getIntExtra(EXTRA_LIVRO_ID, -1);
            if (livroIdRecebido != -1) {
                Log.d(TAG, "Modo Edição: Carregando Livro ID " + livroIdRecebido);
                carregarDadosDoLivroParaEdicao(livroIdRecebido);
            } else {
                Toast.makeText(this, "Erro: ID do livro inválido.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "ID do livro para edição é -1.");
                finish();
            }
        } else {
            Toast.makeText(this, "Erro: Não foi possível carregar o livro.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Intent não continha EXTRA_LIVRO_ID.");
            finish();
        }

        buttonSalvar.setOnClickListener(v -> salvarAlteracoesLivro());
        imageButtonVoltar.setOnClickListener(v -> finish());
        imageButtonExcluir.setOnClickListener(v -> confirmarExclusaoLivro());
    }

    private void carregarDadosDoLivroParaEdicao(int livroId) {
        livroAtualParaEditar = dbHelper.getLivroPorId(livroId);

        if (livroAtualParaEditar != null) {
            // Preencher TextViews de exibição
            textViewTituloLivro.setText(livroAtualParaEditar.getTitulo());
            textViewAutorLivro.setText("Autor(a): " + livroAtualParaEditar.getAutor()); // Adiciona o prefixo como no XML
            textViewGeneroLivro.setText("Gênero: " + livroAtualParaEditar.getGenero());
            textViewPaginasLivro.setText("Páginas: " + livroAtualParaEditar.getPaginas());

            // Preencher campos editáveis
            editTextAnotacoes.setText(livroAtualParaEditar.getAnotacoes());
            selecionarRadioButtonPorTexto(livroAtualParaEditar.getStatus());

            // Os campos de exibição já são TextViews, então não precisam ser "desabilitados".
            // Os RadioButtons e EditText de anotações já são interativos por natureza.
        } else {
            Toast.makeText(this, "Erro ao carregar dados do livro.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Livro com ID " + livroId + " não encontrado para edição.");
            finish();
        }
    }

    private void configurarListenersDosRadioButtonsStatus() {
        android.view.View.OnClickListener listenerStatus = v -> {
            RadioButton rbClicado = (RadioButton) v;

            // Desmarcar os outros se estiverem marcados
            if (rbClicado != radioButtonJaLi && radioButtonJaLi.isChecked()) {
                radioButtonJaLi.setChecked(false);
            }
            if (rbClicado != radioButtonWishlist && radioButtonWishlist.isChecked()) {
                radioButtonWishlist.setChecked(false);
            }
            if (rbClicado != radioButtonLendo && radioButtonLendo.isChecked()) {
                radioButtonLendo.setChecked(false);
            }

            // Marcar o RadioButton clicado
            // Se já estava marcado e foi clicado de novo, ele pode ser desmarcado pelo sistema
            // Para garantir que sempre haja um selecionado se um foi clicado (e para simular RadioGroup):
            if (!rbClicado.isChecked()) {
                rbClicado.setChecked(true);
            }
            ultimoStatusSelecionado = rbClicado; // Atualiza o último selecionado
        };

        radioButtonJaLi.setOnClickListener(listenerStatus);
        radioButtonWishlist.setOnClickListener(listenerStatus);
        radioButtonLendo.setOnClickListener(listenerStatus);
    }

    private void selecionarRadioButtonPorTexto(String statusTexto) {
        // Desmarcar todos primeiro
        radioButtonJaLi.setChecked(false);
        radioButtonWishlist.setChecked(false);
        radioButtonLendo.setChecked(false);
        ultimoStatusSelecionado = null; // Reset

        if (statusTexto == null) return;

        if (radioButtonJaLi.getText().toString().equalsIgnoreCase(statusTexto)) {
            radioButtonJaLi.setChecked(true);
            ultimoStatusSelecionado = radioButtonJaLi;
        } else if (radioButtonWishlist.getText().toString().equalsIgnoreCase(statusTexto)) {
            radioButtonWishlist.setChecked(true);
            ultimoStatusSelecionado = radioButtonWishlist;
        } else if (radioButtonLendo.getText().toString().equalsIgnoreCase(statusTexto)) {
            radioButtonLendo.setChecked(true);
            ultimoStatusSelecionado = radioButtonLendo;
        } else {
            Log.w(TAG, "Status '" + statusTexto + "' não corresponde a nenhum RadioButton.");
        }
    }

    private void salvarAlteracoesLivro() {
        if (livroAtualParaEditar == null) {
            Toast.makeText(this, "Erro: Nenhum livro carregado para salvar.", Toast.LENGTH_SHORT).show();
            return;
        }

        String novoStatus = "";
        String novasAnotacoes = editTextAnotacoes.getText().toString().trim();

        if (radioButtonJaLi.isChecked()) {
            novoStatus = radioButtonJaLi.getText().toString();
        } else if (radioButtonWishlist.isChecked()) {
            novoStatus = radioButtonWishlist.getText().toString();
        } else if (radioButtonLendo.isChecked()) {
            novoStatus = radioButtonLendo.getText().toString();
        } else {
            Toast.makeText(this, "Por favor, selecione um status.", Toast.LENGTH_SHORT).show();
            return; // Status é obrigatório
        }

        livroAtualParaEditar.setStatus(novoStatus);
        livroAtualParaEditar.setAnotacoes(novasAnotacoes);

        int rowsAffected = dbHelper.editarLivro(livroAtualParaEditar);

        if (rowsAffected > 0) {
            Toast.makeText(this, "Livro atualizado com sucesso!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Livro ID " + livroAtualParaEditar.getId() + " atualizado.");
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Erro ao atualizar o livro.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Falha ao atualizar livro ID " + livroAtualParaEditar.getId() + " no banco.");
        }
    }

    private void confirmarExclusaoLivro() {
        if (livroAtualParaEditar == null) {
            Toast.makeText(this, "Nenhum livro para excluir.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Excluir Livro")
                .setMessage("Tem certeza que deseja excluir o livro '" + livroAtualParaEditar.getTitulo() + "'?")
                .setPositiveButton("Excluir", (dialog, which) -> excluirLivro())
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert) // Ícone de alerta padrão
                .show();
    }

    private void excluirLivro() {
        if (livroAtualParaEditar == null) return;

        int rowsDeleted = dbHelper.excluirLivro(livroAtualParaEditar.getId());
        if (rowsDeleted > 0) {
            Toast.makeText(this, "Livro '" + livroAtualParaEditar.getTitulo() + "' excluído.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Livro ID " + livroAtualParaEditar.getId() + " excluído.");
            setResult(RESULT_OK); // Informa à tela anterior para atualizar a lista
            finish();
        } else {
            Toast.makeText(this, "Erro ao excluir o livro.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Falha ao excluir livro ID " + livroAtualParaEditar.getId());
        }
    }
}
