package com.example.minhabiblioteca;

import android.content.DialogInterface;
import android.content.res.ColorStateList; // MOVIDO PARA O TOPO
import android.view.LayoutInflater;
// Removida duplicata de android.view.View
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View; // Mantido para uso geral
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.minhabiblioteca.data.LivroDatabaseHelper;
import com.example.minhabiblioteca.models.Livro;

import java.util.List;

public class Telabiblioteca extends AppCompatActivity { // ÚNICA DECLARAÇÃO DE CLASSE

    private static final String TAG = "TelaBiblioteca_Log";
    private static final int REQUEST_CODE_TELA_EDICAO_OU_CRIACAO = 1;

    private LivroDatabaseHelper dbHelper;
    private List<Livro> todosOsLivros; // Esta é a lista que a UI usa para popular os slots

    // --- Componentes da UI ---
    private ImageButton btnEditar1, btnEditar2, btnEditar3, btnEditar4, btnEditar5;
    private TextView tvTitulo1, tvAutor1, tvGenero1, tvPaginas1, tvStatus1;
    private TextView tvTitulo2, tvAutor2, tvGenero2, tvPaginas2, tvStatus2;
    private TextView tvTitulo3, tvAutor3, tvGenero3, tvPaginas3, tvStatus3;
    private TextView tvTitulo4, tvAutor4, tvGenero4, tvPaginas4, tvStatus4;
    private TextView tvTitulo5, tvAutor5, tvGenero5, tvPaginas5, tvStatus5;

    private Button buttonVoltar;
    private ImageButton addButtonHeader;
    private ImageButton filterButton;


    // --- Variáveis de Filtro ---
    private String filtroGeneroAplicado = null;
    private String filtroStatusAplicado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_biblioteca);

        View mainLayout = findViewById(R.id.main);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } else {
            Log.e(TAG, "Layout raiz para EdgeToEdge (R.id.main) não encontrado!");
        }

        dbHelper = new LivroDatabaseHelper(this);
        inicializarComponentesUI();
        configurarListeners();
        carregarLivrosEConfigurarUI(); // Carregamento inicial
    }

    private void abrirDialogoParaSelecionarStatus() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.radio, null);
        builder.setView(dialogView);

        final RadioGroup radioGroupFiltroStatus = dialogView.findViewById(R.id.radio_group_filtro_status);
        // REMOVIDO: final RadioButton radioStatusTodos = dialogView.findViewById(R.id.radio_staratus_todos);
        final RadioButton radioStatusJaLi = dialogView.findViewById(R.id.radio_status_ja_li);
        final RadioButton radioStatusWishlist = dialogView.findViewById(R.id.radio_status_wishlist);
        final RadioButton radioStatusLendo = dialogView.findViewById(R.id.radio_status_lendo);

        if (radioGroupFiltroStatus == null || radioStatusJaLi == null ||
                radioStatusWishlist == null || radioStatusLendo == null) {
            Log.e(TAG, "Um ou mais RadioButtons (excluindo 'Todos') não foram encontrados no layout.");
            Toast.makeText(this, "Erro ao carregar opções de filtro.", Toast.LENGTH_SHORT).show();
            return;
        }

        final Runnable updateRadioColors = () -> {
            ColorStateList naoSelecionadoColor = ColorStateList.valueOf(ContextCompat.getColor(Telabiblioteca.this, R.color.cor_radio_nao_selecionado));

            // REMOVIDA A LINHA PARA radioStatusTodos.setButtonTintList
            radioStatusJaLi.setButtonTintList(radioStatusJaLi.isChecked() ? ColorStateList.valueOf(ContextCompat.getColor(Telabiblioteca.this, R.color.cor_radio_selecionado_jali)) : naoSelecionadoColor);
            radioStatusWishlist.setButtonTintList(radioStatusWishlist.isChecked() ? ColorStateList.valueOf(ContextCompat.getColor(Telabiblioteca.this, R.color.cor_radio_selecionado_wishlist)) : naoSelecionadoColor);
            radioStatusLendo.setButtonTintList(radioStatusLendo.isChecked() ? ColorStateList.valueOf(ContextCompat.getColor(Telabiblioteca.this, R.color.cor_radio_selecionado_lendo)) : naoSelecionadoColor);
        };

        if (filtroStatusAplicado == null) {
            radioGroupFiltroStatus.clearCheck();
        } else {
            switch (filtroStatusAplicado.toUpperCase()) {
                case "JÁ LI":
                    radioStatusJaLi.setChecked(true);
                    break;
                case "LISTA DE DESEJOS":
                    radioStatusWishlist.setChecked(true);
                    break;
                case "LENDO":
                    radioStatusLendo.setChecked(true);
                    break;
                default:
                    radioGroupFiltroStatus.clearCheck();
                    break;
            }
        }
        updateRadioColors.run(); // Aplicar cores iniciais

        radioGroupFiltroStatus.setOnCheckedChangeListener((group, checkedId) -> {
            updateRadioColors.run(); // Atualizar cores quando a seleção mudar
        });

        builder.setPositiveButton("Aplicar", (dialog, which) -> {
            int selectedId = radioGroupFiltroStatus.getCheckedRadioButtonId();
            String statusSelecionado = null;

            if (selectedId == R.id.radio_status_ja_li) {
                statusSelecionado = "JÁ LI";
            } else if (selectedId == R.id.radio_status_wishlist) {
                statusSelecionado = "LISTA DE DESEJOS";
            } else if (selectedId == R.id.radio_status_lendo) {
                statusSelecionado = "LENDO";
            }
            filtroStatusAplicado = statusSelecionado;
            Log.d(TAG, "Filtro de Status definido para: " + filtroStatusAplicado);
            carregarLivrosEConfigurarUI();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void inicializarComponentesUI() {
        btnEditar1 = findViewById(R.id.btnEditar);
        btnEditar2 = findViewById(R.id.btnEditar2);
        btnEditar3 = findViewById(R.id.btnEditar3);
        btnEditar4 = findViewById(R.id.btnEditar4);
        btnEditar5 = findViewById(R.id.btnEditar5);

        tvTitulo1 = findViewById(R.id.tvTitulo);
        tvAutor1 = findViewById(R.id.tvAutor);
        tvGenero1 = findViewById(R.id.tvGenero);
        tvPaginas1 = findViewById(R.id.tvPaginas);
        tvStatus1 = findViewById(R.id.tvStatus);

        tvTitulo2 = findViewById(R.id.tvTitulo2);
        tvAutor2 = findViewById(R.id.tvAutor2);
        tvGenero2 = findViewById(R.id.tvGenero2);
        tvPaginas2 = findViewById(R.id.tvPaginas2);
        tvStatus2 = findViewById(R.id.tvStatus2);

        tvTitulo3 = findViewById(R.id.tvTitulo3);
        tvAutor3 = findViewById(R.id.tvAutor3);
        tvGenero3 = findViewById(R.id.tvGenero3);
        tvPaginas3 = findViewById(R.id.tvPaginas3);
        tvStatus3 = findViewById(R.id.tvStatus3);

        tvTitulo4 = findViewById(R.id.tvTitulo4);
        tvAutor4 = findViewById(R.id.tvAutor4);
        tvGenero4 = findViewById(R.id.tvGenero4);
        tvPaginas4 = findViewById(R.id.tvPaginas4);
        tvStatus4 = findViewById(R.id.tvStatus4);

        tvTitulo5 = findViewById(R.id.tvTitulo5);
        tvAutor5 = findViewById(R.id.tvAutor5);
        tvGenero5 = findViewById(R.id.tvGenero5);
        tvPaginas5 = findViewById(R.id.tvPaginas5);
        tvStatus5 = findViewById(R.id.tvStatus5);

        buttonVoltar = findViewById(R.id.button2);
        addButtonHeader = findViewById(R.id.addButton);
        filterButton = findViewById(R.id.filterButton);
    }

    private void configurarListeners() {
        if (buttonVoltar != null) {
            buttonVoltar.setOnClickListener(v -> finish());
        }

        if (addButtonHeader != null) {
            addButtonHeader.setOnClickListener(v -> {
                Intent intent = new Intent(Telabiblioteca.this, Tela_criacao.class);
                startActivityForResult(intent, REQUEST_CODE_TELA_EDICAO_OU_CRIACAO);
            });
        }

        if (filterButton != null) {
            filterButton.setOnClickListener(this::mostrarMenuDeFiltroPrincipal);
        } else {
            Log.e(TAG, "filterButton não encontrado! Não foi possível configurar o listener.");
        }
    }

    private void mostrarMenuDeFiltroPrincipal(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.menu_filtros, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.popup_filtrar_genero) {
                abrirDialogoParaSelecionarGenero();
                return true;
            } else if (itemId == R.id.popup_filtrar_status) {
                abrirDialogoParaSelecionarStatus();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void abrirDialogoParaSelecionarGenero() {
        final String[] generosDisponiveis = {"Romance", "Fantasia", "Fábula", "Ficção", "Suspense", "Biografia", "Drama", "Comédia", "Educação", "Distopia"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filtrar por Gênero")
                .setItems(generosDisponiveis, (dialog, which) -> {
                    String generoSelecionado = generosDisponiveis[which];
                    filtroGeneroAplicado = "Todos os Gêneros".equals(generoSelecionado) ? null : generoSelecionado;
                    Log.d(TAG, "Filtro de Gênero definido para: " + filtroGeneroAplicado);
                    carregarLivrosEConfigurarUI();
                });
        builder.create().show();
    }

    private void carregarLivrosEConfigurarUI() {
        Log.d(TAG, "Carregando livros com filtro Gênero: " + filtroGeneroAplicado + ", Status: " + filtroStatusAplicado);
        todosOsLivros = dbHelper.listarLivrosFiltrados(filtroGeneroAplicado, filtroStatusAplicado);
        Log.d(TAG, "Livros carregados (após filtro): " + (todosOsLivros != null ? todosOsLivros.size() : "null"));

        limparTodosOsSlotsDeLivros();

        if (todosOsLivros != null && !todosOsLivros.isEmpty()) {
            if (todosOsLivros.size() >= 1 && btnEditar1 != null && tvTitulo1 != null) {
                Livro livro = todosOsLivros.get(0);
                View slot = findViewById(R.id.select_book1);
                if (slot != null) {
                    preencherDadosLivroSlot(livro, tvTitulo1, tvAutor1, tvGenero1, tvPaginas1, tvStatus1, slot);
                    btnEditar1.setOnClickListener(v -> abrirTelaEdicao(livro.getId()));
                    slot.setVisibility(View.VISIBLE);
                }
            }
            if (todosOsLivros.size() >= 2 && btnEditar2 != null && tvTitulo2 != null) {
                Livro livro = todosOsLivros.get(1); View slot = findViewById(R.id.select_book2);
                if (slot != null) { preencherDadosLivroSlot(livro, tvTitulo2, tvAutor2, tvGenero2, tvPaginas2, tvStatus2, slot);
                    btnEditar2.setOnClickListener(v -> abrirTelaEdicao(livro.getId())); slot.setVisibility(View.VISIBLE); }
            }
            if (todosOsLivros.size() >= 3 && btnEditar3 != null && tvTitulo3 != null) {
                Livro livro = todosOsLivros.get(2); View slot = findViewById(R.id.select_book3);
                if (slot != null) { preencherDadosLivroSlot(livro, tvTitulo3, tvAutor3, tvGenero3, tvPaginas3, tvStatus3, slot);
                    btnEditar3.setOnClickListener(v -> abrirTelaEdicao(livro.getId())); slot.setVisibility(View.VISIBLE); }
            }
            if (todosOsLivros.size() >= 4 && btnEditar4 != null && tvTitulo4 != null) {
                Livro livro = todosOsLivros.get(3); View slot = findViewById(R.id.select_book4);
                if (slot != null) { preencherDadosLivroSlot(livro, tvTitulo4, tvAutor4, tvGenero4, tvPaginas4, tvStatus4, slot);
                    btnEditar4.setOnClickListener(v -> abrirTelaEdicao(livro.getId())); slot.setVisibility(View.VISIBLE); }
            }
            if (todosOsLivros.size() >= 5 && btnEditar5 != null && tvTitulo5 != null) {
                Livro livro = todosOsLivros.get(4); View slot = findViewById(R.id.select_book5);
                if (slot != null) { preencherDadosLivroSlot(livro, tvTitulo5, tvAutor5, tvGenero5, tvPaginas5, tvStatus5, slot);
                    btnEditar5.setOnClickListener(v -> abrirTelaEdicao(livro.getId())); slot.setVisibility(View.VISIBLE); }
            }

            if (todosOsLivros.size() == 0) {
                Log.w(TAG, "Nenhum livro encontrado para este filtro.");
                Toast.makeText(this, "Não há livros.", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Nenhum livro encontrado com os filtros aplicados.");
        }
    }

    private void preencherDadosLivroSlot(Livro livro, TextView tvTitulo, TextView tvAutor,
                                         TextView tvGenero, TextView tvPaginas, TextView tvStatus,
                                         View layoutLivro) {
        if (livro == null || layoutLivro == null) {
            Log.e(TAG, "preencherDadosLivroSlot: Livro ou Layout é null.");
            return;
        }
        if (tvTitulo != null) tvTitulo.setText(livro.getTitulo());
        else Log.w(TAG, "tvTitulo é null para o livro: " + (livro.getTitulo() != null ? livro.getTitulo() : "ID " +livro.getId()));

        if (tvAutor != null) tvAutor.setText("Autor(a): " + livro.getAutor());
        else Log.w(TAG, "tvAutor é null para o livro: " + (livro.getTitulo() != null ? livro.getTitulo() : "ID " +livro.getId()));

        if (tvGenero != null) tvGenero.setText("Gênero: " + livro.getGenero());
        else Log.w(TAG, "tvGenero é null para o livro: " + (livro.getTitulo() != null ? livro.getTitulo() : "ID " +livro.getId()));

        if (tvPaginas != null) tvPaginas.setText("Páginas: " + livro.getPaginas());
        else Log.w(TAG, "tvPaginas é null para o livro: " + (livro.getTitulo() != null ? livro.getTitulo() : "ID " +livro.getId()));

        if (tvStatus != null) {
            String statusLivro = livro.getStatus();
            tvStatus.setText(statusLivro);

            Log.w(TAG, "tvStatus é null para o livro: " + (livro.getTitulo() != null ? livro.getTitulo() : "ID " +livro.getId()));
        }
        layoutLivro.setVisibility(View.VISIBLE);
    }


    private void limparTodosOsSlotsDeLivros() {
        View slot;
        slot = findViewById(R.id.select_book1); if (slot != null) slot.setVisibility(View.GONE);
        slot = findViewById(R.id.select_book2); if (slot != null) slot.setVisibility(View.GONE);
        slot = findViewById(R.id.select_book3); if (slot != null) slot.setVisibility(View.GONE);
        slot = findViewById(R.id.select_book4); if (slot != null) slot.setVisibility(View.GONE);
        slot = findViewById(R.id.select_book5); if (slot != null) slot.setVisibility(View.GONE);
    }

    private void abrirTelaEdicao(int livroId) {
        Intent intent = new Intent(this, Tela_edicao.class);
        intent.putExtra(Tela_edicao.EXTRA_LIVRO_ID, livroId);
        startActivityForResult(intent, REQUEST_CODE_TELA_EDICAO_OU_CRIACAO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TELA_EDICAO_OU_CRIACAO && resultCode == RESULT_OK) {
            carregarLivrosEConfigurarUI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLivrosEConfigurarUI();
    }
}
