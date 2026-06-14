package com.example.intents

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.intents.databinding.ActivityCintentExplicitoParametrosBinding

class CIntentExplicitoParametros : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cintent_explicito_parametros)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val entrenador = intent.getParcelableExtra<
                BEntrenador
                >(
            "entrenador"
        )

        val tvId = findViewById<TextView>(R.id.tvId)
        val tvNombre = findViewById<TextView>(R.id.tvNombre)
        val tvDescripcion = findViewById<TextView>(R.id.tvDescripcion)
        tvId.text = entrenador?.id.toString()
        tvNombre.text = entrenador?.nombre
        tvDescripcion.text = entrenador?.descripcion

        val nombre = intent.getStringExtra("nombre")
        val apellido = intent.getStringExtra("apellido")
        val edad = intent.getIntExtra("edad", 0)

        val botonDevolverRespuesta = findViewById<Button>(
            R.id.btn_devolver_respuesta
        )
        botonDevolverRespuesta
            .setOnClickListener {
                val intenRespuesta = Intent()
                intenRespuesta.putExtra(
                    "nombreModificado",
                    nombre + apellido + edad + " :) "
                )
                setResult(RESULT_OK, intenRespuesta)
                finish()
            }
    }
}