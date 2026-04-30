package br.edu.utfpr.turistou.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import br.edu.utfpr.turistou.entity.Cadastro

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, BD_NAME, null, BD_VERSION) {

    // Cria a tabela principal do banco
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                _id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT,
                descricao TEXT,
                latitude TEXT,
                longitude TEXT
                ,endereco TEXT,
                imagem BLOB
            )
            """.trimIndent()
        )
    }

    // Aplica as migrações de versão do banco
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Migração v2 -> v3: adiciona endereco
        if (oldVersion < 3) {
            try {
                db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN endereco TEXT DEFAULT ''")
            } catch (e: Exception) {
                // Ignora se a coluna já existir
                e.printStackTrace()
            }
        }

        // Migração v3 -> v4: adiciona imagem
        if (oldVersion < 4) {
            try {
                db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN imagem BLOB")
            } catch (e: Exception) {
                // Ignora se a coluna já existir
                e.printStackTrace()
            }
        }
    }

    // Insere um novo cadastro no banco
    fun insert(cadastro: Cadastro) {
        val db = this.writableDatabase
        val registro = ContentValues()
        registro.put(COL_NOME, cadastro.nome)
        registro.put(COL_DESCRICAO, cadastro.descricao)
        registro.put(COL_LATITUDE, cadastro.latitude)
        registro.put(COL_LONGITUDE, cadastro.longitude)
        registro.put(COL_ENDERECO, cadastro.endereco)
        if (cadastro.imagem != null) {
            registro.put(COL_IMAGEM, cadastro.imagem)
        } else {
            registro.putNull(COL_IMAGEM)
        }
        db.insert(TABLE_NAME, null, registro)
        db.close()
    }

    // Atualiza um cadastro existente no banco
    fun update(cadastro: Cadastro) {
        val db = this.writableDatabase
        val registro = ContentValues()
        registro.put(COL_NOME, cadastro.nome)
        registro.put(COL_DESCRICAO, cadastro.descricao)
        registro.put(COL_LATITUDE, cadastro.latitude)
        registro.put(COL_LONGITUDE, cadastro.longitude)
        registro.put(COL_ENDERECO, cadastro.endereco)
        if (cadastro.imagem != null) {
            registro.put(COL_IMAGEM, cadastro.imagem)
        } else {
            registro.putNull(COL_IMAGEM)
        }
        db.update(TABLE_NAME, registro, "_id = ?", arrayOf(cadastro.id.toString()))
        db.close()
    }

    // Remove um cadastro pelo id
    fun delete(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "_id = ?", arrayOf(id.toString()))
        db.close()
    }

    // Lista todos os cadastros gravados
    fun listar(): Cursor {
        val db = this.readableDatabase
        return db.query(TABLE_NAME, null, null, null, null, null, null)
    }

    companion object {
        private const val BD_NAME = "dbfile.sqlite"
        private const val TABLE_NAME = "cadastro"
        private const val BD_VERSION = 4
        private const val COL_NOME = "nome"
        private const val COL_DESCRICAO = "descricao"
        private const val COL_LATITUDE = "latitude"
        private const val COL_LONGITUDE = "longitude"
        private const val COL_ENDERECO = "endereco"
        private const val COL_IMAGEM = "imagem"
    }
}