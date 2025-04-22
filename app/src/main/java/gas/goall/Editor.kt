package gas.goall

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import androidx.appcompat.app.AlertDialog

class Editor : AppCompatActivity() {
    private lateinit var tulis: EditText
    private var file: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editor)

        tulis = findViewById(R.id.tulis)
        val nama = findViewById<TextView>(R.id.nama)
        val simpan = findViewById<ImageView>(R.id.simpan)

        val path = intent.getStringExtra("file")

        if (path != null) {
            file = File(path)
            nama.text = file?.name
            tulis.setText(file?.readText())
        } else {
            nama.text = "main.txt"
            file = File(filesDir, "main.txt")
        }

        simpan.setOnClickListener {
            try {
                file?.writeText(tulis.text.toString())
                Toast.makeText(this, "${file?.name} tersimpan", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal menyimpan", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Keluar")
            .setMessage("Apakah anda ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                finish()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
}