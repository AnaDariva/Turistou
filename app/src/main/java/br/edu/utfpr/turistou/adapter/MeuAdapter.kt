package br.edu.utfpr.turistou.adapter

import android.graphics.BitmapFactory
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import br.edu.utfpr.turistou.CadastrarActivity
import br.edu.utfpr.turistou.R
import br.edu.utfpr.turistou.entity.Cadastro

class MeuAdapter(val contexto : Context, val lista : Cursor) : BaseAdapter() {

    // Retorna a quantidade de itens da lista
    override fun getCount(): Int {
        return lista.count
    }

    // Monta o cadastro da posição solicitada
    override fun getItem(id: Int): Any? {
        lista.moveToPosition(id)

        val cadastro = Cadastro(
            lista.getInt(lista.getColumnIndexOrThrow("_id")),
            lista.getString(lista.getColumnIndexOrThrow("nome")),
            lista.getString(lista.getColumnIndexOrThrow("descricao")),
            lista.getString(lista.getColumnIndexOrThrow("latitude")),
            lista.getString(lista.getColumnIndexOrThrow("longitude")),
            lista.getString(lista.getColumnIndexOrThrow("endereco")),
            lista.getBlob(lista.getColumnIndexOrThrow("imagem"))
        )

        return cadastro
    }

    // Retorna o id do item da posição solicitada
    override fun getItemId(id: Int): Long {
        lista.moveToPosition(id)
        return lista.getInt(lista.getColumnIndexOrThrow("_id")).toLong()
    }

    // Cria a view de cada item da lista
    override fun getView( id : Int, convertView: View?, parent : ViewGroup? ): View? {

        // Reaproveita a view do item quando existir
        val v = convertView ?: run {
            val inflater = contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.elemento_lista, parent, false)
        }

        // Pega as views do item
        val tvNome = v.findViewById<TextView>(R.id.tvNomeElementoLista)
        val tvDescricao = v.findViewById<TextView>(R.id.tvDescricaoElementoLista)
        val tvEndereco = v.findViewById<TextView>(R.id.tvEnderecoElementoLista)
        val ivFoto = v.findViewById<ImageView>(R.id.ivFotoElementoLista)
        val btEditar = v.findViewById<ImageButton>(R.id.btEditarElementoLista)

        // Move o cursor para a posição atual
        lista.moveToPosition(id)

        // Preenche os campos com os dados do cursor
        val idCol = lista.getColumnIndexOrThrow("_id")
        val nomeCol = lista.getColumnIndexOrThrow("nome")
        val descricaoCol = lista.getColumnIndexOrThrow("descricao")
        val latitudeCol = lista.getColumnIndexOrThrow("latitude")
        val longitudeCol = lista.getColumnIndexOrThrow("longitude")
        val enderecoCol = lista.getColumnIndexOrThrow("endereco")
        val imagemCol = lista.getColumnIndexOrThrow("imagem")

        tvNome.text = lista.getString(nomeCol)
        tvDescricao.text = lista.getString(descricaoCol)
        tvEndereco.text = lista.getString(enderecoCol)

        val imagemBlob = lista.getBlob(imagemCol)
        if (imagemBlob != null && imagemBlob.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(imagemBlob, 0, imagemBlob.size)
            ivFoto.setImageBitmap(bitmap)
        } else {
            ivFoto.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        btEditar.setOnClickListener {
            lista.moveToPosition(id)
            val intent = Intent(contexto, CadastrarActivity::class.java)
            intent.putExtra("id", lista.getInt(idCol))
            intent.putExtra("nome", lista.getString(nomeCol))
            intent.putExtra("descricao", lista.getString(descricaoCol))
            intent.putExtra("latitude", lista.getString(latitudeCol))
            intent.putExtra("longitude", lista.getString(longitudeCol))
            intent.putExtra("endereco", lista.getString(enderecoCol))
            intent.putExtra("imagem", imagemBlob)
            contexto.startActivity(intent)
        }

        return v

    }

}