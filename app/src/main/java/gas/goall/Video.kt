package gas.goall

import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import java.io.File

class Video : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var seekBar: SeekBar
    private lateinit var playButton: ImageView
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video)

        val dariBerkasPath = intent.getStringExtra("file")

        if (dariBerkasPath == null) {
            Toast.makeText(this, "TIDAK DIDUKUNG: File tidak ditemukan", Toast.LENGTH_SHORT).show()
        } else {
            val videoFile = File(dariBerkasPath)
            TampilkanVideo(videoFile)
        }

        findViewById<TextView>(R.id.keluar).setOnClickListener { finish() }
    }

    private fun TampilkanVideo(file: File) {
        videoView = findViewById(R.id.lihat)
        seekBar = findViewById(R.id.berjalan)
        playButton = findViewById(R.id.mulai)

        videoView.setVideoPath(file.absolutePath)

        playButton.setOnClickListener {
            if (isPlaying) {
                videoView.pause()
                playButton.setImageResource(R.drawable.play)
            } else {
                videoView.start()
                playButton.setImageResource(R.drawable.pause)
            }
            isPlaying = !isPlaying
        }

        videoView.setOnPreparedListener { mp ->
            seekBar.max = mp.duration
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        videoView.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        videoView.setOnCompletionListener {
            isPlaying = false
            playButton.setImageResource(R.drawable.play)
            videoView.seekTo(0)
        }
    }
}
