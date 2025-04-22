package gas.goall

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import androidx.appcompat.app.AlertDialog

class Asisten : AppCompatActivity() {

    private lateinit var logika: File
    private lateinit var perbarui: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.asisten)

        findViewById<TextView>(R.id.keluar).setOnClickListener { finish() }

        BuatFile()
        Obrolan()
    }

    private fun BuatFile() {
        logika = File(filesDir, "logika/logika.txt")
        if (!logika.exists()) {
            logika.parentFile?.mkdirs()
            logika.writeText("") // buat file kosong
        }

        perbarui = File(filesDir, "logika/perbarui.txt")
        if (!perbarui.exists()) {
            perbarui.writeText("") // file baru juga
        }
    }

    private fun Obrolan() {
        val jawab = findViewById<TextView>(R.id.jawab)
        val tanya = findViewById<EditText>(R.id.tanya)

        findViewById<TextView>(R.id.kirim).setOnClickListener {
            val semuaIsi = logika.readText()
            val pertanyaan = tanya.text.toString()

            // Contoh: cocokin pertanyaan dengan isi logika
            if (semuaIsi.contains(pertanyaan)) {
                jawab.append("\n$pertanyaan ditemukan di logika.")
            } else {
                jawab.append("\nTidak ada jawaban. Ditandai untuk dipelajari.")
                DataKosong(pertanyaan)
            }

            tanya.setText("")
        }
    }

    private fun DataKosong(pertanyaan: String) {
        perbarui.appendText("$pertanyaan\n")
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