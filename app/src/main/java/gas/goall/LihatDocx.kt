package gas.goall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import java.io.File
import java.io.FileInputStream
import org.apache.poi.xwpf.usermodel.XWPFDocument

class LihatDocx : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lihat_doc)

        findViewById<ImageView>(R.id.nav).setOnClickListener {
            val liner = findViewById<LinearLayout>(R.id.liner)
            liner.visibility = if (liner.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        findViewById<TextView>(R.id.keluar).setOnClickListener { finish() }

        val path = intent.getStringExtra("file")
        if (path == null) {
            MulaiNormal()
        } else {
            TampilkanIsi(File(path))
        }
    }

    private fun TampilkanIsi(file: File) {
        val isi = findViewById<TextView>(R.id.isi)
        try {
            val doc = XWPFDocument(FileInputStream(file))
            val sb = StringBuilder()
            for (para in doc.paragraphs) {
                sb.append(para.text).append("\n\n")
            }
            isi.text = sb.toString()
        } catch (e: Exception) {
            isi.text = "Gagal membaca file: ${e.message}"
        }
    }

    private fun MulaiNormal() {
        Toast.makeText(this, "Belum Siap", Toast.LENGTH_SHORT).show()
    }
}