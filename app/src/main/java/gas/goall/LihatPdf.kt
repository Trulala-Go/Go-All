package gas.goall

import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import android.view.View
import java.io.File
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import java.io.InputStream
import androidx.appcompat.app.AlertDialog

class LihatPdf : AppCompatActivity() {

    private lateinit var pdfList: List<Pair<String, android.net.Uri>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lihat_pdf)

        findViewById<ImageView>(R.id.nav).setOnClickListener {
            val liner = findViewById<LinearLayout>(R.id.liner)
            liner.visibility = if (liner.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        findViewById<TextView>(R.id.keluar).setOnClickListener { finish() }

        tampilkanSemuaPdf()
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

    private fun tampilkanSemuaPdf() {
        val listView = findViewById<ListView>(R.id.lis)
        val lihat = findViewById<PDFView>(R.id.lihat)
        val liner = findViewById<LinearLayout>(R.id.liner)
        val nama = findViewById<TextView>(R.id.nama)

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME
        )
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
        val selectionArgs = arrayOf("application/pdf")

        val cursor = contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        )

        pdfList = mutableListOf()

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)

            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val name = it.getString(nameCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Files.getContentUri("external"), id
                )
                (pdfList as MutableList).add(name to uri)
            }
        }

        if (pdfList.isEmpty()) {
            Toast.makeText(this, "Tidak ada PDF ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            pdfList.map { it.first }
        )
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val uri = pdfList[position].second
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val tempFile = File.createTempFile("temp", ".pdf", cacheDir)
                tempFile.outputStream().use { fileOut -> inputStream.copyTo(fileOut) }

                lihat.fromFile(tempFile)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .load()

                liner.visibility = View.GONE
                nama.text = tempFile.name
            } else {
                Toast.makeText(this, "Gagal membuka PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }
}