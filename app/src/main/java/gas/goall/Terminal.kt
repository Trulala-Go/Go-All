package gas.goall

import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog

class Terminal : AppCompatActivity(){
override fun onCreate(savedInstanceState:Bundle?){
    super.onCreate(savedInstanceState)
        setContentView(R.layout.terminal)
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
