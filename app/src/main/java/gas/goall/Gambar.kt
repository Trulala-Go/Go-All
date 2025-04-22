package gas.goall

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import com.bumptech.glide.Glide
import android.content.ContentUris
import androidx.appcompat.app.AlertDialog

class Gambar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gambar)

        findViewById<TextView>(R.id.keluar).setOnClickListener { finish() }

        val dariBerkas = intent.getStringExtra("file")
        val GBesar = findViewById<ImageView>(R.id.besar)

        val poto = listOf("jpg", "png", "webp", "gif")

        if (dariBerkas == null) {
            GBesar.setImageResource(R.drawable.foto)
        } else {
            val isiFile = File(dariBerkas)
            val ekstensi = isiFile.extension.lowercase()
            if (ekstensi !in poto) {
                Toast.makeText(this, "Hanya Mendukung Gambar", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            Glide.with(this).load(isiFile).into(GBesar)
        }

        SemuaGambar()
    }

    private fun SemuaGambar() {
    val grid = findViewById<GridLayout>(R.id.grid)
    grid.removeAllViews()

    val projection = arrayOf(MediaStore.Images.Media._ID)
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val cursor = contentResolver.query(uri, projection, null, null, null)

    cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val inflater = LayoutInflater.from(this)

        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val imageUri = ContentUris.withAppendedId(uri, id)

            val itemView = inflater.inflate(R.layout.item_gambar, grid, false)
            val imageView = itemView.findViewById<ImageView>(R.id.gambar)

            Glide.with(this).load(imageUri).into(imageView)

            itemView.setOnClickListener {
                Glide.with(this).load(imageUri).into(findViewById(R.id.besar))
            }

            grid.addView(itemView)
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