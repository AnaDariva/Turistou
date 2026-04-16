package br.edu.utfpr.turistou

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.utfpr.turistou.database.DatabaseHandler
import br.edu.utfpr.turistou.entity.Cadastro
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
        private const val MAP_FRAGMENT_TAG = "main_map_fragment"
        // Ponto de abertura inicial do mapa.
        private val DEFAULT_MAP_CENTER = LatLng(-26.1976727, -52.690157)
    }

    private lateinit var mMap: GoogleMap
    private lateinit var banco: DatabaseHandler
    private lateinit var locationManager: LocationManager

    private val pinsDoBanco = mutableListOf<Marker>()
    private var marcadorLocalAtual: Marker? = null
    private var cameraInicialPosicionada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        banco = DatabaseHandler(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)
        val btnListar = findViewById<Button>(R.id.btnListar)
        val btnConfig = findViewById<Button>(R.id.btnConfig)

        val mapFragment = (supportFragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG) as? SupportMapFragment)
            ?: SupportMapFragment.newInstance().also {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mapContainer, it, MAP_FRAGMENT_TAG)
                    .commitNow()
            }
        mapFragment.getMapAsync(this)

        btnCadastrar.setOnClickListener {
            startActivity(Intent(this, CadastrarActivity::class.java))
        }

        btnListar.setOnClickListener {
            startActivity(Intent(this, ListarActivity::class.java))
        }

        btnConfig.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        verificarPermissaoEIniciarGPS()
    }

    override fun onResume() {
        super.onResume()
        if (::mMap.isInitialized) {
            aplicarPreferenciasDoMapa()
            carregarPinsDoBanco()
        }
        verificarPermissaoEIniciarGPS()
    }

    override fun onPause() {
        super.onPause()
        if (::locationManager.isInitialized) {
            try {
                locationManager.removeUpdates(this)
            } catch (_: Exception) {
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        aplicarPreferenciasDoMapa()
        posicionarCameraInicialSeNecessario()
        carregarPinsDoBanco()
        verificarPermissaoEIniciarGPS()
    }

    private fun aplicarPreferenciasDoMapa() {
        if (!::mMap.isInitialized) return

        mMap.mapType = when (SettingsActivity.getMapType(this)) {
            "satellite" -> GoogleMap.MAP_TYPE_SATELLITE
            else -> GoogleMap.MAP_TYPE_NORMAL
        }
    }

    private fun posicionarCameraInicialSeNecessario() {
        if (!::mMap.isInitialized || cameraInicialPosicionada) return
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_MAP_CENTER, zoomPreferido()))
        cameraInicialPosicionada = true
    }

    private fun zoomPreferido(): Float {
        return SettingsActivity.getDefaultZoom(this).toFloat()
    }

    private fun carregarPinsDoBanco() {
        Thread {
            val registros = mutableListOf<Cadastro>()

            banco.listar().use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
                    val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")) ?: "Sem nome"
                    val descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao")) ?: ""
                    val latitude = cursor.getString(cursor.getColumnIndexOrThrow("latitude")) ?: ""
                    val longitude = cursor.getString(cursor.getColumnIndexOrThrow("longitude")) ?: ""
                    val endereco = cursor.getString(cursor.getColumnIndexOrThrow("endereco")) ?: ""
                    val imagemIndex = cursor.getColumnIndexOrThrow("imagem")
                    val imagem = if (cursor.isNull(imagemIndex)) null else cursor.getBlob(imagemIndex)

                    registros.add(Cadastro(id, nome, descricao, latitude, longitude, endereco, imagem))
                }
            }

            runOnUiThread {
                desenharPinsNoMapa(registros)
            }
        }.start()
    }

    private fun desenharPinsNoMapa(registros: List<Cadastro>) {
        if (!::mMap.isInitialized) return

        pinsDoBanco.forEach { it.remove() }
        pinsDoBanco.clear()

        registros.forEach { cadastro ->
            val lat = cadastro.latitude.toDoubleOrNull()
            val lng = cadastro.longitude.toDoubleOrNull()

            if (lat != null && lng != null) {
                val ponto = LatLng(lat, lng)
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(ponto)
                        .title(cadastro.nome)
                        .snippet(cadastro.endereco.ifBlank { cadastro.descricao })
                )
                if (marker != null) {
                    pinsDoBanco.add(marker)
                }
            }
        }
    }

    private fun verificarPermissaoEIniciarGPS() {
        val fineGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            // Solicita permissão
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            if (::mMap.isInitialized) {
                try {
                    mMap.isMyLocationEnabled = true
                } catch (_: SecurityException) {
                }
            }

            try {
                // Já tem permissão, inicia atualização.
                // DICA: Use GPS_PROVIDER e NETWORK_PROVIDER para funcionar melhor em locais fechados
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5f, this)
            } catch (_: SecurityException) {
            } catch (_: IllegalArgumentException) {
                // se o provider estiver indisponível, apenas ignora
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
            val concedida = grantResults.any { it == PackageManager.PERMISSION_GRANTED }
            if (concedida) {
                verificarPermissaoEIniciarGPS()
            } else {
                Toast.makeText(this, "Permissao de GPS negada!", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        if (!::mMap.isInitialized) return

        marcadorLocalAtual?.remove()
        val currentLatLng = LatLng(location.latitude, location.longitude)
        marcadorLocalAtual = mMap.addMarker(
            MarkerOptions().position(currentLatLng).title("Sua Posição")
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            locationManager.removeUpdates(this)
        } catch (_: Exception) {
        }
    }
}