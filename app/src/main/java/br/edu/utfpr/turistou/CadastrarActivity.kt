package br.edu.utfpr.turistou

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.turistou.database.DatabaseHandler
import br.edu.utfpr.turistou.entity.Cadastro
import java.io.ByteArrayOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class CadastrarActivity : AppCompatActivity(), LocationListener {

    private var cadastroId: Int = 0
    private lateinit var etNome: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var ivFoto: ImageView
    private lateinit var btSalvar: Button
    private lateinit var btExcluir: Button
    private lateinit var locationManager: LocationManager
    private var aguardandoLocalizacao = false
    private var imagemBlob: ByteArray? = null

    // Instancia o handler do banco de dados
    private lateinit var banco: DatabaseHandler

    private val register = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            ivFoto.setImageBitmap(bitmap)
            imagemBlob = bitmapToBlob(bitmap)
        } else {
            Toast.makeText(this, "Foto não capturada.", Toast.LENGTH_SHORT).show()
        }
    }

    // Inicializa a Activity, views, banco e atualizações de localização
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar)

        etNome = findViewById(R.id.etNome)
        etDescricao = findViewById(R.id.etDescricao)
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        ivFoto = findViewById(R.id.ivFoto)
        btSalvar = findViewById(R.id.btSalvar)
        btExcluir = findViewById(R.id.btExcluir)

        cadastroId = intent.getIntExtra("id", 0)

        if (cadastroId != 0) {
            btSalvar.text = "Atualizar"
            etNome.setText(intent.getStringExtra("nome"))
            etDescricao.setText(intent.getStringExtra("descricao"))
            etLatitude.setText(intent.getStringExtra("latitude"))
            etLongitude.setText(intent.getStringExtra("longitude"))
            imagemBlob = intent.getByteArrayExtra("imagem")
            if (imagemBlob != null) {
                val bitmap = BitmapFactory.decodeByteArray(imagemBlob, 0, imagemBlob!!.size)
                ivFoto.setImageBitmap(bitmap)
            }
        } else {
            btSalvar.text = "Salvar"
            btExcluir.visibility = View.GONE
        }


        banco = DatabaseHandler(this) // Abre ou cria o banco de dados

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        iniciarAtualizacaoLocalizacao()

    }

    // Verifica permissões e inicia requests de localização
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

    // Trata resposta da solicitação de permissão de localização
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

    // Preenche latitude/longitude na primeira localização e para os updates
    override fun onLocationChanged(location: Location) {
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

    // Pausa updates de localização se estavam ativos
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

    // Solicita atualização de localização sem fechar a Activity
    fun btAtualizarLocalizacaoOnClick(view: View) {
        iniciarAtualizacaoLocalizacao(exibirToastErro = false)
    }

    // Abre câmera para capturar foto e armazenar em blob
    fun btTirarFotoOnClick(view: View) {
        register.launch(null)
    }

    // Salva ou atualiza cadastro no banco em thread de background
    fun btAlterarOnClick(view: View) {
        val nome = etNome.text.toString()
        val descricao = etDescricao.text.toString()
        val latitude = etLatitude.text.toString()
        val longitude = etLongitude.text.toString()

        Thread {
            val endereco = buscarEnderecoPorCoordenadas(latitude, longitude)

            try {
                if (cadastroId == 0) {
                    val cadastro = Cadastro(
                        0,
                        nome,
                        descricao,
                        latitude,
                        longitude,
                        endereco,
                        imagemBlob
                    )
                    banco.insert(cadastro)
                } else {
                    val cadastro = Cadastro(
                        cadastroId,
                        nome,
                        descricao,
                        latitude,
                        longitude,
                        endereco,
                        imagemBlob
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

    // Consulta Geocoding do Google Maps e retorna o endereço formatado
    private fun buscarEnderecoPorCoordenadas(latitude: String, longitude: String): String {
        if (latitude.isBlank() || longitude.isBlank()) {
            return ""
        }

        val googleMapsApiKey = obterGoogleMapsApiKey()
        if (googleMapsApiKey.isBlank()) {
            Log.w("CadastrarActivity", "Chave Google Maps ausente no meta-data do Manifest.")
            return ""
        }

        var conexao: HttpURLConnection? = null

        return try {
            val endpoint =
                "https://maps.googleapis.com/maps/api/geocode/xml?latlng=$latitude,$longitude&key=$googleMapsApiKey"
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

    // Lê a chave do Google Maps a partir do meta-data do Manifest
    private fun obterGoogleMapsApiKey(): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            appInfo.metaData?.getString("com.google.android.geo.API_KEY").orEmpty()
        } catch (e: Exception) {
            Log.e("CadastrarActivity", "Erro ao obter chave Google Maps do Manifest.", e)
            ""
        }
    }

    // Remove o registro do banco e finaliza a Activity
    fun btExcluirOnClick(view: View) {
        if (cadastroId == 0) {
            Toast.makeText(this, "Registro ainda não salvo.", Toast.LENGTH_SHORT).show()
            return
        }
        banco.delete(cadastroId)
        Toast.makeText(this, "Exclusão realizada com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }

    // Converte Bitmap para array de bytes JPEG
    private fun bitmapToBlob(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }


    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }

}