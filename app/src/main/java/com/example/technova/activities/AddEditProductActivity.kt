package com.example.technova.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.technova.R
import com.example.technova.database.ProductoDAO
import com.example.technova.models.Producto
import kotlin.concurrent.thread

class AddEditProductActivity : AppCompatActivity() {
    private lateinit var edtName: EditText
    private lateinit var edtDesc: EditText
    private lateinit var edtPrice: EditText
    private lateinit var edtStock: EditText
    private lateinit var imgPreview: ImageView
    private lateinit var btnPickImage: Button
    private lateinit var btnSave: Button

    private var selectedImageUri: String? = null
    private val productoDAO by lazy { ProductoDAO(this) }
    private var editingProductId: Long = 0
    private var esAdmin: Boolean = false

    private val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            // Limpiamos permisos viejos por si el usuario cambia la imagen
            try {
                val oldUriString = selectedImageUri
                if (oldUriString != null) {
                    val oldUri = Uri.parse(oldUriString)
                    contentResolver.releasePersistableUriPermission(oldUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } catch (e: SecurityException) {
                // Ignorar si el permiso ya no existía
            }

            // Tomamos el nuevo permiso persistente
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val intentFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            grantUriPermission(packageName, it, intentFlags)



            selectedImageUri = it.toString()
            imgPreview.setImageURI(it)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_product)

        val prefs = getSharedPreferences("technova_prefs", Context.MODE_PRIVATE)
        esAdmin = prefs.getBoolean("esAdmin", false)

        edtName = findViewById(R.id.edtProductName)
        edtDesc = findViewById(R.id.edtProductDesc)
        edtPrice = findViewById(R.id.edtProductPrice)
        edtStock = findViewById(R.id.edtProductStock)
        imgPreview = findViewById(R.id.imgPreview)
        btnPickImage = findViewById(R.id.btnPickImage)
        btnSave = findViewById(R.id.btnSaveProduct)

        // si no es admin, bloquear edición completa
        if (!esAdmin) {
            Toast.makeText(this, "No tienes permiso para crear o editar productos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnPickImage.setOnClickListener { pickImage.launch(arrayOf("image/*")) }

        // comprobar si venimos a editar
        editingProductId = intent.getLongExtra("productoId", 0L)
        if (editingProductId != 0L) {
            cargarProducto(editingProductId)
        }


        btnSave.setOnClickListener { guardarProducto() }
    }

    private fun cargarProducto(id: Long) {
        thread {
            val p = productoDAO.obtenerPorId(id)
            runOnUiThread {
                p?.let {
                    edtName.setText(it.nombre)
                    edtDesc.setText(it.descripcion)
                    edtPrice.setText(it.precio.toString())
                    edtStock.setText(it.stock.toString())
                    selectedImageUri = it.imagenUrl

                    if (!selectedImageUri.isNullOrEmpty()) {
                        // Envolver la carga de la URI en un bloque try-catch
                        // para manejar excepciones si la URI ya no es válida o
                        // si el permiso se revocó por alguna razón externa.
                        try {
                            imgPreview.setImageURI(Uri.parse(selectedImageUri))
                        } catch (e: SecurityException) {
                            // Si falla, mostramos un mensaje amigable y una imagen por defecto.
                            Toast.makeText(
                                this,
                                "No se pudo cargar la imagen. Por favor, selecciónala de nuevo.",
                                Toast.LENGTH_LONG
                            ).show()
                            imgPreview.setImageResource(R.drawable.image_placeholder) // O tu logo
                        }
                    } else {
                        // Si no hay imagen guardada, muestra la imagen por defecto.
                        imgPreview.setImageResource(R.drawable.image_placeholder)
                    }
                }
            }
        }
    }


    private fun guardarProducto() {
        val nombre = edtName.text.toString().trim()
        val descripcion = edtDesc.text.toString().trim()
        val precioStr = edtPrice.text.toString().trim()
        val stockStr = edtStock.text.toString().trim()
        val selectedImageUri = selectedImageUri


        if (nombre.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Completa nombre, precio y stock", Toast.LENGTH_SHORT).show()
            return
        }

        val precio = precioStr.toDoubleOrNull()
        val stock = stockStr.toIntOrNull()
        if (precio == null || stock == null) {
            Toast.makeText(this, "Precio o stock inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val producto = Producto(
            id = if (editingProductId != 0L) editingProductId else 0,
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            stock = stock,
            imagenUrl = selectedImageUri
        )

        thread {
            val ok = if (editingProductId != 0L) productoDAO.actualizar(producto) else (productoDAO.insertar(producto) != -1L)
            runOnUiThread {
                if (ok) {
                    Toast.makeText(this, "Producto guardado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}