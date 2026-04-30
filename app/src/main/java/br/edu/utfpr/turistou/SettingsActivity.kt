package br.edu.utfpr.turistou

import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val PREF_MAP_DEFAULT_ZOOM = "pref_map_default_zoom"
        const val PREF_MAP_TYPE = "pref_map_type"

        const val MIN_MAP_ZOOM = 1
        const val MAX_MAP_ZOOM = 20
        const val DEFAULT_MAP_ZOOM = 14
        const val DEFAULT_MAP_TYPE = "roadmap"

        // Migra o zoom salvo no formato antigo
        fun migrateLegacyZoomPreference(context: Context) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val value = prefs.all[PREF_MAP_DEFAULT_ZOOM]

            if (value is String) {
                val migratedZoom = value.toIntOrNull()?.coerceIn(MIN_MAP_ZOOM, MAX_MAP_ZOOM) ?: DEFAULT_MAP_ZOOM
                // Garante o valor convertido antes do SeekBarPreference carregar.
                prefs.edit { putInt(PREF_MAP_DEFAULT_ZOOM, migratedZoom) }
            }
        }

        // Lê o zoom padrão do mapa
        fun getDefaultZoom(context: Context): Int {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return when (val value = prefs.all[PREF_MAP_DEFAULT_ZOOM]) {
                is Int -> value.coerceIn(MIN_MAP_ZOOM, MAX_MAP_ZOOM)
                is String -> value.toIntOrNull()?.coerceIn(MIN_MAP_ZOOM, MAX_MAP_ZOOM) ?: DEFAULT_MAP_ZOOM
                else -> DEFAULT_MAP_ZOOM
            }
        }

        // Lê o tipo do mapa salvo
        fun getMapType(context: Context): String {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getString(PREF_MAP_TYPE, DEFAULT_MAP_TYPE) ?: DEFAULT_MAP_TYPE
        }

        // Salva zoom e tipo do mapa
        @Suppress("unused")
        fun saveMapSettings(context: Context, zoom: Int, mapType: String) {
            PreferenceManager.getDefaultSharedPreferences(context).edit {
                putInt(PREF_MAP_DEFAULT_ZOOM, zoom.coerceIn(MIN_MAP_ZOOM, MAX_MAP_ZOOM))
                putString(PREF_MAP_TYPE, mapType)
            }
        }
    }

    // Inicializa a tela de configurações e o botão de voltar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        findViewById<Button>(R.id.btnBackToMain).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        // Carrega as preferências da tela
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            migrateLegacyZoomPreference(requireContext())
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}