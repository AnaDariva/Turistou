package br.edu.utfpr.turistou

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.turistou.database.DatabaseHandler
import br.edu.utfpr.turistou.entity.Cadastro

class CadastrarActivity : AppCompatActivity(), LocationListener {

    private lateinit var etCod: EditText
    private lateinit var etNome: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var btExcluir: Button
    private lateinit var btPesquisar: Button
    private lateinit var locationManager: LocationManager

    private lateinit var banco: DatabaseHandler // Declarando a variável do banco de dados

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar)

        etCod = findViewById(R.id.etCod)
        etNome = findViewById(R.id.etNome)
        etDescricao = findViewById(R.id.etDescricao)
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        btExcluir = findViewById(R.id.btExcluir)
        btPesquisar = findViewById(R.id.btPesquisar)

        if (intent.getIntExtra("id", 0) != 0) {
            etCod.setText(intent.getIntExtra("id", 0).toString())
            etNome.setText(intent.getStringExtra("nome"))
            etDescricao.setText(intent.getStringExtra("descricao"))
            etLatitude.setText(intent.getStringExtra("latitude"))
            etLongitude.setText(intent.getStringExtra("longitude"))
        } else {
            btExcluir.visibility = View.GONE
            btPesquisar.visibility = View.GONE
        }


        banco = DatabaseHandler(this) // Abre ou cria o banco de dados

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        iniciarAtualizacaoLocalizacao()

    }

    private fun iniciarAtualizacaoLocalizacao() {
        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            val permissaoConcedida = grantResults.any { it == PackageManager.PERMISSION_GRANTED }
            if (permissaoConcedida) {
                iniciarAtualizacaoLocalizacao()
            } else {
                Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        // Preenche automaticamente na primeira localização recebida.
        etLatitude.setText(location.latitude.toString())
        etLongitude.setText(location.longitude.toString())
        locationManager.removeUpdates(this)
    }

    override fun onPause() {
        super.onPause()
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(this)
        }
    }

    fun btAlterarOnClick(view: View) {

        if (etCod.text.toString().isEmpty()) {
            val cadastro = Cadastro(
                0,
                etNome.text.toString(),
                etDescricao.text.toString(),
                etLatitude.text.toString(),
                etLongitude.text.toString(),
                ""
            )
            banco.insert(cadastro)

        } else {

            val cadastro = Cadastro(
                etCod.text.toString().toInt(),
                etNome.text.toString(),
                etDescricao.text.toString(),
                etLatitude.text.toString(),
                etLongitude.text.toString(),
                ""
            )

            banco.update(cadastro)
        }

        Toast.makeText(this, "Registro salvo com sucesso!", Toast.LENGTH_SHORT).show()

        finish()

    }

    fun btExcluirOnClick(view: View) {
        banco.delete(etCod.text.toString().toInt())
        Toast.makeText(this, "Exclusão realizada com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }

    fun btPesquisarOnClick(view: View) {

        val etCodPesquisa = EditText(this)
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Código")
        builder.setView(etCodPesquisa)
        builder.setCancelable(false)
        builder.setNegativeButton("Fechar", null)
        builder.setPositiveButton(
            "Pesquisar",
            { dialogInterface, i ->

                val registro = banco.pesquisar(
                    etCodPesquisa.text.toString().toInt()
                )

                if (registro != null) {
                    etCod.setText(registro.id.toString())
                    etNome.setText(registro.nome)
                    etDescricao.setText(registro.descricao)
                    etLatitude.setText(registro.latitude)
                    etLongitude.setText(registro.longitude)
                } else {
                    Toast.makeText(this, "Registro não encontrado!", Toast.LENGTH_SHORT).show()
                }

            })

        builder.show()

    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }

}