package br.edu.utfpr.turistou

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.turistou.adapter.MeuAdapter
import br.edu.utfpr.turistou.database.DatabaseHandler

class ListarActivity : AppCompatActivity() {

    // ListView que contém os registros carregados
    private lateinit var lvRegistros : ListView

    // Handler para operações no banco
    private lateinit var banco : DatabaseHandler

    // Inicializa layout e instâncias necessárias
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar)

        lvRegistros = findViewById(R.id.lvRegistros)
        banco = DatabaseHandler(this)

    }

    // Carrega registros do banco e aplica adapter
    override fun onStart() {
        super.onStart()
        val registros = banco.listar()
        val adapter = MeuAdapter( this, registros )
        lvRegistros.adapter = adapter
    }

    // Abre a tela de cadastro para incluir novo registro
    fun btIncluirOnClick(view: View) {
        val intent = Intent( this, CadastrarActivity::class.java )
        startActivity(intent)
    }
}