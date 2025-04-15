
package gas.goall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import android.content.Intent
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<LinearLayout>(R.id.berkas).setOnClickListener {
            startActivity(Intent(this, Berkas::class.java))
        }
        
        findViewById<LinearLayout>(R.id.editor).setOnClickListener {
            startActivity(Intent(this, Editor::class.java))
        }
        
        findViewById<LinearLayout>(R.id.gambar).setOnClickListener {
            startActivity(Intent(this, Gambar::class.java))
        }
        
        findViewById<LinearLayout>(R.id.video).setOnClickListener {
            startActivity(Intent(this, Video::class.java))
        }

    }
    
    override fun onBackPressed() {
    AlertDialog.Builder(this)
        .setTitle("INGIN KELUAR APLIKASI?")
        .setPositiveButton("Ya") { _, _ -> finish() }
        .setNegativeButton("Tidak", null)
        .show()
}

}
