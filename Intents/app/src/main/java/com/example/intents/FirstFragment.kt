package com.example.intents

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.intents.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    val callbackContenidoIntentImplicito =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
                result ->
            if(result.resultCode == Activity.RESULT_OK){
                if(result.data != null){
                    // validacion de datos
                    if(result.data!!.data != null){
                        var uri: Uri = result.data!!.data!!
                        val cursor = requireContext().contentResolver.query(
                            uri,
                            null,
                            null,
                            null,
                            null,
                        )
                        cursor?.moveToFirst()
                        val indiceTelefono = cursor?.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        )
                        val telefono = cursor?.getString(
                            indiceTelefono!!
                        )
                        cursor?.close()
                        Log.i("resultado", "Telefono: $telefono")
                    }
                }

            }
        }

    val callbackContenidoIntentExplicito =
        registerForActivityResult(
            ActivityResultContracts
                .StartActivityForResult()
        ){
                result ->
            if(result.resultCode== Activity.RESULT_OK){
                if(result.data != null){
                    val nombre = result.data!!
                        .getStringExtra("nombreModificado")

                    Log.i("resultado","Nombre: $nombre")
                }
            }

        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        binding.btnIrIntentImplicito
            .setOnClickListener {
                val intentConRespuesta = Intent(
                    Intent.ACTION_PICK,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                )
                callbackContenidoIntentImplicito.launch(intentConRespuesta)
            }
        binding.btnIrIntentExplicito
            .setOnClickListener {
                val intentExplicito = Intent(
                    context,
                    CIntentExplicitoParametros::class.java
                )
                intentExplicito.putExtra("nombre", "Mateo")
                intentExplicito.putExtra("apellido", "Simbaña")
                intentExplicito.putExtra("edad", 22)
                intentExplicito.putExtra("entrenador",
                    BEntrenador(1,"Mateo Simbaña", "Estudiante")
                )
                callbackContenidoIntentExplicito.launch(
                    intentExplicito
                )
                // startActivity(intentExplicito) <=
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}