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
            lista.getString(2),
            lista.getString(3),
            lista.getString(4),
            lista.getString(5)
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
        val tvDescricao = v.findViewById<TextView>(R.id.tvDescricaoElementoLista)
        val tvEndereco = v.findViewById<TextView>(R.id.tvEnderecoElementoLista)
        val btEditar = v.findViewById<ImageButton>(R.id.btEditarElementoLista)

        // posiciona o cursor na linha correspondente ao id
        lista.moveToPosition(id)

        // preenche os componentes visuais com os dados do cursor
        tvNome.text = lista.getString(1)
        tvDescricao.text = lista.getString(2)
        tvEndereco.text = lista.getString(5)

        btEditar.setOnClickListener {
            lista.moveToPosition(id)
            val intent = Intent(contexto, MainActivity::class.java)
            intent.putExtra("id", lista.getInt(0))
            intent.putExtra("nome", lista.getString(1))
            intent.putExtra("descricao", lista.getString(2))
            intent.putExtra("latitude", lista.getString(3))
            intent.putExtra("longitude", lista.getString(4))
            intent.putExtra("endereco", lista.getString(5))
            contexto.startActivity(intent)
        }

        return v

    }

}