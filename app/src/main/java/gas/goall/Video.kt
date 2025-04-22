package gas.goall

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import androidx.appcompat.app.AlertDialog

class Video : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var playButton: ImageView
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video)

        videoView = findViewById(R.id.tampil)
        playButton = findViewById(R.id.play)

        val path = intent.getStringExtra("file")
        if (path != null) {
            val uri = Uri.fromFile(File(path))
            videoView.setVideoURI(uri)
            videoView.setOnCompletionListener {
                isPlaying = false
                playButton.setImageResource(R.drawable.play)
            }
        } else {
            Toast.makeText(this, "File tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        playButton.setOnClickListener {
            MulaiStop()
        }
    }

    private fun MulaiStop() {
        if (!videoView.isPlaying) {
            videoView.start()
            playButton.setImageResource(R.drawable.pause)
            isPlaying = true
        } else {
            videoView.pause()
            playButton.setImageResource(R.drawable.play)
            isPlaying = false
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