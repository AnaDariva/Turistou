package br.edu.utfpr.turistou.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import br.edu.utfpr.turistou.entity.Cadastro

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, BD_NAME, null, BD_VERSION) {

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

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Migração de versão 2 para 3: adiciona coluna endereco
        if (oldVersion < 3) {
            try {
                db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN endereco TEXT DEFAULT ''")
            } catch (e: Exception) {
                // Se a coluna já existe, ignora o erro
                e.printStackTrace()
            }
        }

        // Migração de versão 3 para 4: adiciona coluna imagem
        if (oldVersion < 4) {
            try {
                db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN imagem BLOB")
            } catch (e: Exception) {
                // Se a coluna já existe, ignora o erro
                e.printStackTrace()
            }
        }
    }

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

    fun delete(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "_id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun pesquisar(id: Int): Cadastro? {
        val db = this.readableDatabase
        val registro = db.query(TABLE_NAME, null, "_id = ?", arrayOf(id.toString()), null, null, null)
        val cadastro = if (registro.moveToNext()) {
            val _id = registro.getInt(COL_ID)
            val nome = registro.getString(registro.getColumnIndexOrThrow(COL_NOME))
            val descricao = registro.getString(registro.getColumnIndexOrThrow(COL_DESCRICAO))
            val latitude = registro.getString(registro.getColumnIndexOrThrow(COL_LATITUDE))
            val longitude = registro.getString(registro.getColumnIndexOrThrow(COL_LONGITUDE))
            val endereco = registro.getString(registro.getColumnIndexOrThrow(COL_ENDERECO))
            val imagem = registro.getBlob(registro.getColumnIndexOrThrow(COL_IMAGEM))
            Cadastro(_id, nome, descricao, latitude, longitude, endereco, imagem)
        } else {
            null
        }
        registro.close()
        db.close()
        return cadastro
    }

    fun listar(): Cursor {
        val db = this.readableDatabase
        return db.query(TABLE_NAME, null, null, null, null, null, null)
    }

    companion object {
        private const val BD_NAME = "dbfile.sqlite"
        private const val TABLE_NAME = "cadastro"
        private const val BD_VERSION = 4
        private const val COL_ID = 0
        private const val COL_NOME = "nome"
        private const val COL_DESCRICAO = "descricao"
        private const val COL_LATITUDE = "latitude"
        private const val COL_LONGITUDE = "longitude"
        private const val COL_ENDERECO = "endereco"
        private const val COL_IMAGEM = "imagem"
    }
}