package com.example.minhabiblioteca; // Ou seu pacote correto

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
// Remova import android.widget.RadioGroup;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// EdgeToEdge imports
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.minhabiblioteca.data.LivroDatabaseHelper;
import com.example.minhabiblioteca.models.Livro;

import java.util.Arrays;
import java.util.List;

public class Tela_criacao extends AppCompatActivity {

    private static final String TAG = "Tela_Criacao";

    private EditText editTextTitulo, editTextAutor, editTextPaginas, editTextAnotacoes;
    private Spinner spinnerGenero;
    // Remova: private RadioGroup radioGroupStatus;
    private RadioButton radioButtonJaLi, radioButtonWishlist, radioButtonLendo;
    private Button buttonAdicionar;
    private ImageButton imageButtonVoltar;

    private LivroDatabaseHelper dbHelper;

    // Para armazenar qual RadioButton está atualmente selecionado
    private RadioButton ultimoStatusSelecionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_criacao);
        View mainLayout = findViewById(R.id.main);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        dbHelper = new LivroDatabaseHelper(this);

        editTextTitulo = findViewById(R.id.txt_titulo);
        editTextAutor = findViewById(R.id.txt_autor);
        spinnerGenero = findViewById(R.id.slect_genero); // Corrija o ID se for "select_genero"
        editTextPaginas = findViewById(R.id.txt_pag);
        editTextAnotacoes = findViewById(R.id.txt_anotacoes);
        buttonAdicionar = findViewById(R.id.btn_adicionar);
        imageButtonVoltar = findViewById(R.id.btn_back);

        // Inicializar RadioButtons individualmente
        radioButtonJaLi = findViewById(R.id.btn_jali);
        radioButtonWishlist = findViewById(R.id.btn_wishlist);
        radioButtonLendo = findViewById(R.id.btn_lendo);

        // Configurar listeners para simular RadioGroup
        configurarListenersDosRadioButtons();

        configurarSpinnerGenero();

        buttonAdicionar.setOnClickListener(v -> adicionarNovoLivro());
        imageButtonVoltar.setOnClickListener(v -> finish());
    }

    private void configurarListenersDosRadioButtons() {
        android.view.View.OnClickListener listenerStatus = v -> {
            RadioButton rbClicado = (RadioButton) v;

            // Desmarcar todos os outros RadioButtons
            if (rbClicado != radioButtonJaLi && radioButtonJaLi.isChecked()) {
                radioButtonJaLi.setChecked(false);
            }
            if (rbClicado != radioButtonWishlist && radioButtonWishlist.isChecked()) {
                radioButtonWishlist.setChecked(false);
            }
            if (rbClicado != radioButtonLendo && radioButtonLendo.isChecked()) {
                radioButtonLendo.setChecked(false);
            }

            // Marcar o RadioButton clicado (se não estiver já marcado)
            // Ou permitir desmarcar se clicar novamente no mesmo
            if (ultimoStatusSelecionado == rbClicado && rbClicado.isChecked()) {
                // Já estava marcado e foi clicado novamente, nada a fazer ou desmarcar
                // Para este exemplo, vamos manter marcado, mas a lógica de um RadioGroup
                // não permite desmarcar clicando novamente.
            } else if (ultimoStatusSelecionado == rbClicado && !rbClicado.isChecked()){
                // O usuário clicou no mesmo botão para desmarcá-lo (comportamento não padrão de RadioGroup)
                rbClicado.setChecked(true); // Força a manter marcado, como um RadioGroup faria
            } else {
                rbClicado.setChecked(true);
            }
            ultimoStatusSelecionado = rbClicado.isChecked() ? rbClicado : null;
        };

        radioButtonJaLi.setOnClickListener(listenerStatus);
        radioButtonWishlist.setOnClickListener(listenerStatus);
        radioButtonLendo.setOnClickListener(listenerStatus);
    }


    private void configurarSpinnerGenero() {
        List<String> generos = Arrays.asList("Romance", "Fantasia", "Fábula", "Ficção", "Suspense", "Biografia", "Drama", "Comédia", "Educação", "Distopia");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, generos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenero.setAdapter(adapter);
    }

    private void adicionarNovoLivro() {
        String titulo = editTextTitulo.getText().toString().trim();
        String autor = editTextAutor.getText().toString().trim();
        String paginasStr = editTextPaginas.getText().toString().trim();
        String anotacoes = editTextAnotacoes.getText().toString().trim();
        String genero = "";
        String status = "";

        // Validações (Título, Autor, Gênero, Páginas - como antes)
        if (TextUtils.isEmpty(titulo)) { /* ... setError ... */ return; }
        if (TextUtils.isEmpty(autor)) { /* ... setError ... */ return; }
        if (spinnerGenero.getSelectedItem() == null || TextUtils.isEmpty(spinnerGenero.getSelectedItem().toString())) { /* ... Toast ... */ return; }
        genero = spinnerGenero.getSelectedItem().toString();
        int paginas = 0;
        if (TextUtils.isEmpty(paginasStr)) { /* ... setError ... */ return; }
        try {
            paginas = Integer.parseInt(paginasStr);
            if (paginas <= 0) { /* ... setError ... */ return; }
        } catch (NumberFormatException e) { /* ... setError ... */ return; }


        // Obter status dos RadioButtons individuais
        if (radioButtonJaLi.isChecked()) {
            status = radioButtonJaLi.getText().toString();
        } else if (radioButtonWishlist.isChecked()) {
            status = radioButtonWishlist.getText().toString();
        } else if (radioButtonLendo.isChecked()) {
            status = radioButtonLendo.getText().toString();
        } else {
            Toast.makeText(this, "Selecione um status", Toast.LENGTH_SHORT).show();
            return; // Status é obrigatório
        }

        Livro novoLivro = new Livro();
        novoLivro.setTitulo(titulo);
        novoLivro.setAutor(autor);
        novoLivro.setGenero(genero);
        novoLivro.setPaginas(paginas);
        novoLivro.setStatus(status);
        novoLivro.setAnotacoes(anotacoes);

        boolean sucesso = dbHelper.adicionarLivro(novoLivro);

        if (sucesso) {
            Toast.makeText(this, "Livro '" + titulo + "' adicionado com sucesso!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Erro ao adicionar o livro.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Falha ao adicionar livro no banco de dados: " + titulo);
        }
    }
}
