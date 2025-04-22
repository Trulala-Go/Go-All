package gas.goall

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import java.io.File
import androidx.appcompat.app.AppCompatActivity

class Musik : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var sedangMain = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.musik)

        findViewById<TextView>(R.id.keluar).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.nav).setOnClickListener {
            val liner = findViewById<LinearLayout>(R.id.liner)
            liner.visibility = if (liner.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        
        val dariBerkas = intent.getStringExtra("file")
        IsiDaftar()

        if (dariBerkas != null) {
            val file = File(dariBerkas)
            val uri = Uri.fromFile(file)
            MulaiMainkan(file.name, uri)
        }

        
    }

    private fun IsiDaftar() {
        val daftar = findViewById<ListView>(R.id.daftarMusik)
        val laguList = mutableListOf<Pair<String, Uri>>() // Nama + Uri

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media._ID
        )

        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)

            while (it.moveToNext()) {
                val title = it.getString(titleColumn)
                val id = it.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(uri, id.toString())
                laguList.add(title to contentUri)
            }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, laguList.map { it.first })
        daftar.adapter = adapter

        daftar.setOnItemClickListener { _, _, pos, _ ->
            val lagu = laguList[pos]
            MulaiMainkan(lagu.first, lagu.second)
        }
    }

    private fun MulaiMainkan(namaLagu: String, fileUri: Uri) {
        val mulai = findViewById<ImageView>(R.id.mulai)
        val judul = findViewById<TextView>(R.id.nama)
        val waktu = findViewById<TextView>(R.id.waktu)

        judul.text = namaLagu
        waktu.text = "00:00"

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, fileUri)
        mediaPlayer?.start()
        sedangMain = true

        mulai.setImageResource(R.drawable.pause)

        mulai.setOnClickListener {
            if (sedangMain) {
                mediaPlayer?.pause()
                mulai.setImageResource(R.drawable.play)
            } else {
                mediaPlayer?.start()
                mulai.setImageResource(R.drawable.pause)
            }
            sedangMain = !sedangMain
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
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