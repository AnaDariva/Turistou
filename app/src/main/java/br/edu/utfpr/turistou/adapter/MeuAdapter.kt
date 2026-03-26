package br.edu.utfpr.turistou.adapter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import br.edu.utfpr.turistou.MainActivity
import br.edu.utfpr.turistou.R
import br.edu.utfpr.turistou.entity.Cadastro

class MeuAdapter(val contexto : Context, val lista : Cursor) : BaseAdapter() {

    // Metodo responsável por retornar a quantidade de itens da lista
    override fun getCount(): Int {
        return lista.count
    }

    // Metodo responsável por retornar o item da lista
    override fun getItem(id: Int): Any? {
        lista.moveToPosition(id)

        val cadastro = Cadastro(
            lista.getInt(0),
            lista.getString(1),
            lista.getString(2)
        )

        return cadastro
    }

    // Metodo responsável por retornar o id do item da lista
    override fun getItemId(id: Int): Long {
        lista.moveToPosition(id)
        return lista.getInt(0).toLong()
    }

    // Metodo responsável por criar a view de cada elemento da lista
    override fun getView( id : Int, p1 : View?, p2 : ViewGroup? ): View? {

        // recupera a referência do arquivo xml do elemento da lista
        val inflater = contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.elemento_lista, null)

        // recupera os componentes visuais do elemento da lista
        val tvNome = v.findViewById<TextView>(R.id.tvNomeElementoLista)
        val tvTelefone = v.findViewById<TextView>(R.id.tvTelefoneElementoLista)
        val btEditar = v.findViewById<ImageButton>(R.id.btEditarElementoLista)

        // posiciona o cursor na linha correspondente ao id
        lista.moveToPosition(id)

        // preenche os componentes visuais com os dados do cursor
        tvNome.text = lista.getString(1)
        tvTelefone.text = lista.getString(2)

        btEditar.setOnClickListener {
            lista.moveToPosition(id)
            val intent = Intent(contexto, MainActivity::class.java)
            intent.putExtra("id", lista.getInt(0))
            intent.putExtra("nome", lista.getString(1))
            intent.putExtra("telefone", lista.getString(2))
            contexto.startActivity(intent)
        }

        return v

    }

}