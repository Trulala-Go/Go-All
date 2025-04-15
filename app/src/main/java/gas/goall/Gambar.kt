package gas.goall

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import android.net.Uri
import android.content.Intent
import android.provider.MediaStore
import android.content.ContentUris
import com.bumptech.glide.Glide
import android.view.LayoutInflater

class Gambar : AppCompatActivity() {

    private lateinit var besar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gambar)

        val file: File? = intent.getSerializableExtra("file") as? File
        besar = findViewById(R.id.gambarBesar)

        if (file == null) {
            besar.setImageResource(R.drawable.foto)
            TampilkanSemua(null)
        } else {
            Glide.with(this).load(file).into(besar)
            TampilkanSemua(file.parentFile)
        }
    }

    private fun TampilkanSemua(folder: File?) {
        val grid = findViewById<GridLayout>(R.id.grid)
        val uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val cursor = contentResolver.query(uriExternal, projection, null, null, null)

        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(uriExternal, id)

                val item = LayoutInflater.from(this).inflate(R.layout.item_gambar, grid, false)
                val gambar = item.findViewById<ImageView>(R.id.gambar)
                Glide.with(this).load(contentUri).centerCrop().into(gambar)

                item.setOnClickListener {
                    Glide.with(this).load(contentUri).into(besar)
                }

                grid.addView(item)
            }
        }
    }
}
