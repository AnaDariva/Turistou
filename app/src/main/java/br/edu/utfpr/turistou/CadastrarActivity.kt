package br.edu.utfpr.turistou

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.turistou.database.DatabaseHandler
import br.edu.utfpr.turistou.entity.Cadastro
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class CadastrarActivity : AppCompatActivity(), LocationListener {

    private lateinit var etCod: EditText
    private lateinit var etNome: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var btSalvar: Button
    private lateinit var btExcluir: Button
    private lateinit var locationManager: LocationManager
    private var aguardandoLocalizacao = false

    private lateinit var banco: DatabaseHandler // Declarando a variável do banco de dados

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar)

        etCod = findViewById(R.id.etCod)
        etNome = findViewById(R.id.etNome)
        etDescricao = findViewById(R.id.etDescricao)
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        btSalvar = findViewById(R.id.btSalvar)
        btExcluir = findViewById(R.id.btExcluir)

        if (intent.getIntExtra("id", 0) != 0) {
            btSalvar.text = "Atualizar"
            etCod.setText(intent.getIntExtra("id", 0).toString())
            etNome.setText(intent.getStringExtra("nome"))
            etDescricao.setText(intent.getStringExtra("descricao"))
            etLatitude.setText(intent.getStringExtra("latitude"))
            etLongitude.setText(intent.getStringExtra("longitude"))
        } else {
            btSalvar.text = "Salvar"
            btExcluir.visibility = View.GONE
        }


        banco = DatabaseHandler(this) // Abre ou cria o banco de dados

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        iniciarAtualizacaoLocalizacao()

    }

    private fun iniciarAtualizacaoLocalizacao(exibirToastErro: Boolean = true) {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        try {
            val gpsHabilitado = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val networkHabilitado =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!gpsHabilitado && !networkHabilitado) {
                if (exibirToastErro) {
                    Toast.makeText(this, "Localização indisponível no momento.", Toast.LENGTH_SHORT)
                        .show()
                }
                return
            }

            aguardandoLocalizacao = true

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, this)

        } catch (e: SecurityException) {
            aguardandoLocalizacao = false
            Log.e("CadastrarActivity", "Permissão de localização não concedida.", e)
            if (exibirToastErro) {
                Toast.makeText(
                    this,
                    "Não foi possível atualizar a localização.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            aguardandoLocalizacao = false
            Log.e("CadastrarActivity", "Erro ao iniciar atualização de localização.", e)
            if (exibirToastErro) {
                Toast.makeText(this, "Erro ao atualizar a localização.", Toast.LENGTH_SHORT).show()
            }
        }
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
        try {
            locationManager.removeUpdates(this)
        } catch (e: Exception) {
            Log.w("CadastrarActivity", "Falha ao remover listener de localização.", e)
        } finally {
            aguardandoLocalizacao = false
        }
    }

    override fun onPause() {
        super.onPause()
        if (::locationManager.isInitialized && aguardandoLocalizacao) {
            try {
                locationManager.removeUpdates(this)
            } catch (e: Exception) {
                Log.w("CadastrarActivity", "Falha ao pausar listener de localização.", e)
            } finally {
                aguardandoLocalizacao = false
            }
        }
    }

    fun btAtualizarLocalizacaoOnClick(view: View) {
        // Atualização manual não encerra a Activity - em caso de erro apenas ignora e mantém os dados atuais.
        iniciarAtualizacaoLocalizacao(exibirToastErro = false)
    }

    fun btAlterarOnClick(view: View) {
        val idTexto = etCod.text.toString()
        val nome = etNome.text.toString()
        val descricao = etDescricao.text.toString()
        val latitude = etLatitude.text.toString()
        val longitude = etLongitude.text.toString()

        Thread {
            val endereco = buscarEnderecoPorCoordenadas(latitude, longitude)

            try {
                if (idTexto.isEmpty()) {
                    val cadastro = Cadastro(
                        0,
                        nome,
                        descricao,
                        latitude,
                        longitude,
                        endereco
                    )
                    banco.insert(cadastro)
                } else {
                    val cadastro = Cadastro(
                        idTexto.toInt(),
                        nome,
                        descricao,
                        latitude,
                        longitude,
                        endereco
                    )
                    banco.update(cadastro)
                }

                runOnUiThread {
                    Toast.makeText(this, "Registro salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("CadastrarActivity", "Erro ao salvar cadastro.", e)
                runOnUiThread {
                    Toast.makeText(this, "Erro ao salvar registro.", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()

    }

    private fun buscarEnderecoPorCoordenadas(latitude: String, longitude: String): String {
        if (latitude.isBlank() || longitude.isBlank()) {
            return ""
        }

        var conexao: HttpURLConnection? = null

        return try {
            val endpoint =
                "https://maps.googleapis.com/maps/api/geocode/xml?latlng=$latitude,$longitude&key=$GOOGLE_GEOCODING_API_KEY"
            val url = URL(endpoint)
            conexao = url.openConnection() as HttpURLConnection
            conexao.connectTimeout = 10000
            conexao.readTimeout = 10000
            conexao.requestMethod = "GET"

            if (conexao.responseCode != HttpURLConnection.HTTP_OK) {
                ""
            } else {
                val entrada = BufferedReader(InputStreamReader(conexao.inputStream))
                val saida = StringBuilder()

                var linha = entrada.readLine()
                while (linha != null) {
                    saida.append(linha)
                    linha = entrada.readLine()
                }
                entrada.close()

                val resposta = saida.toString()
                if (resposta.contains("<formatted_address>") && resposta.contains("</formatted_address>")) {
                    resposta.substringAfter("<formatted_address>")
                        .substringBefore("</formatted_address>")
                } else {
                    ""
                }
            }
        } catch (e: Exception) {
            Log.e("CadastrarActivity", "Erro ao consultar endereço por coordenadas.", e)
            ""
        } finally {
            conexao?.disconnect()
        }
    }

    fun btExcluirOnClick(view: View) {
        banco.delete(etCod.text.toString().toInt())
        Toast.makeText(this, "Exclusão realizada com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }


    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
        private const val GOOGLE_GEOCODING_API_KEY = "AIzaSyDsy454kAkXofX828BEMieAQ7EbtpjohZY"
    }

}