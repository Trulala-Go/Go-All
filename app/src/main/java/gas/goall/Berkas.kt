package gas.goall

import android.widget.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import java.io.File
import android.os.Environment
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import android.content.Intent
import androidx.appcompat.app.AlertDialog

class Berkas : AppCompatActivity(){

    private lateinit var currentPath: File
    private val uiScope = CoroutineScope(Dispatchers.Main + Job())
    private val selectedFiles = mutableSetOf<File>()
    private var fileSumber: File? = null
    private var modeTempel: String? = null

override fun onCreate(savedInstanceState:Bundle?){
    super.onCreate(savedInstanceState)
        setContentView(R.layout.berkas)
        
        currentPath = filesDir
        JalurSekarang()
        
        findViewById<ImageView>(R.id.nav).setOnClickListener{
            val liner = findViewById<LinearLayout>(R.id.liner)
            liner.visibility = if(liner.visibility == View.GONE)View.VISIBLE else View.GONE
        }
        
        findViewById<TextView>(R.id.baru).setOnClickListener{
            BuatBaru()
        }
        
        findViewById<TextView>(R.id.keluar).setOnClickListener{
            finish()
        }
        
        findViewById<ImageView>(R.id.apk).setOnClickListener{
            currentPath = filesDir
            TampilkanGrid(currentPath)
            JalurSekarang()
        }
        
        findViewById<ImageView>(R.id.sdCard).setOnClickListener{
            currentPath = Environment.getExternalStorageDirectory()
            TampilkanGrid(currentPath)
            JalurSekarang()
        }
        
        val kembali = findViewById<ImageView>(R.id.kembali)
        kembali.setOnClickListener {
            val parent = currentPath.parentFile
            if (parent != null && parent.canRead()) {
                currentPath = parent
                TampilkanGrid(currentPath)
                JalurSekarang()
            }
            if (currentPath == filesDir || currentPath == Environment.getExternalStorageDirectory()) {
            kembali.visibility = View.GONE
            }
        }
        
        val grid = findViewById<GridLayout>(R.id.grid)
        val loading = findViewById<ProgressBar>(R.id.loading)
        loading.visibility = View.VISIBLE

        grid.post { loading.visibility = View.GONE }
        
        
    }
    private fun JalurSekarang(){
        val jalur = findViewById<TextView>(R.id.jalur)
        jalur.text = currentPath.absolutePath
    }
    
    private fun SegarkanGrid(file: File) {
        TampilkanGrid(file)
    }
    
    private fun TampilkanGrid(folder: File) {
    val grid = findViewById<GridLayout>(R.id.grid)
    val loading = findViewById<ProgressBar>(R.id.loading)

    loading.visibility = View.VISIBLE
    grid.removeAllViews()

    uiScope.launch {
        val files = withContext(Dispatchers.IO) {
            folder.listFiles()?.toList()?.sortedBy { !it.isDirectory } ?: emptyList()
        }

        if (files.isEmpty()) {
            val kosong = TextView(this@Berkas).apply {
                text = "Tidak ada apapun"
                textSize = 16f
                setPadding(16, 32, 16, 32)
                gravity = Gravity.CENTER
                layoutParams = GridLayout.LayoutParams().apply {
                    columnSpec = GridLayout.spec(0, grid.columnCount)
                    width = GridLayout.LayoutParams.MATCH_PARENT
                }
            }
            grid.addView(kosong)
        } else {
            for (file in files) {
                val item = LayoutInflater.from(this@Berkas)
                    .inflate(R.layout.item_vertical, grid, false)

                val gambar = item.findViewById<ImageView>(R.id.gambar)
                val nama = item.findViewById<TextView>(R.id.nama)
                val centang = item.findViewById<TextView>(R.id.centang)

                nama.text = file.name

                if (file.isDirectory) {
                    gambar.setImageResource(R.drawable.folder)
                } else {
                    val ekstensi = file.extension.lowercase()
                    if (ekstensi in listOf("png", "jpg", "jpeg", "gif", "bmp", "webp")) {
                        Glide.with(this@Berkas)
                            .load(file)
                            .centerCrop()
                            .into(gambar)
                    } else {
                        val iconRes = when (ekstensi) {
                            "kt" -> R.drawable.kotlin
                            "java" -> R.drawable.java
                            "py" -> R.drawable.python
                            "js" -> R.drawable.js
                            "c", "h", "cpp" -> R.drawable.c
                            "gradle", "kts" -> R.drawable.gradle
                            "apk", "xapk", "sh", "deb", "exe" -> R.drawable.apk
                            "html" -> R.drawable.html
                            "tar", "zip", "xz", "gz", "rar" -> R.drawable.ekstrak
                            "css" -> R.drawable.css
                            "pdf" -> R.drawable.pdf
                            else -> R.drawable.file
                        }
                        gambar.setImageResource(iconRes)
                    }
                }

                item.setOnClickListener {
                    if (file.isDirectory) {
                        currentPath = file
                        TampilkanGrid(currentPath)
                        JalurSekarang()
                        findViewById<ImageView>(R.id.kembali).visibility = View.VISIBLE
                    } else {
                        BukaFile(file)
                    }
                }
                
                item.setOnLongClickListener {
                if (selectedFiles.contains(file)) {
                    selectedFiles.remove(file)
                    centang.visibility = View.GONE
               } else {
                    selectedFiles.add(file)
                    centang.visibility = View.VISIBLE
                }

                TekanLama(file)

                    true
                }

                grid.addView(item)
            }
        }

        loading.visibility = View.GONE
    }
}

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
    }
    
    override fun onDestroy() {
        super.onDestroy()
            uiScope.cancel()
    }
    
    private fun BuatBaru() {
    val opsi = arrayOf("Folder", "File")
    AlertDialog.Builder(this)
        .setTitle("Pilih Opsi")
        .setItems(opsi) { dialog, which ->
            when (which) {
                0 -> TulisNama(true)   
                1 -> TulisNama(false)  
            }
        }
        .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
        .show()
    }

    private fun TulisNama(isFolder: Boolean) {
    val input = EditText(this)
    input.hint = if (isFolder) "Nama Folder" else "Nama File"
    
    AlertDialog.Builder(this)
        .setTitle("Masukkan Nama")
        .setView(input)
        .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
        .setPositiveButton("Simpan") { dialog, _ ->
            val nama = input.text.toString().trim()
            if (nama.isNotEmpty()) {
                val fileBaru = File(currentPath, nama)
                if (isFolder) {
                    val dibuat = fileBaru.mkdir()
                    if (!dibuat) {
                        Toast.makeText(this, "Gagal buat folder", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val dibuat = fileBaru.createNewFile()
                    if (!dibuat) {
                        Toast.makeText(this, "Gagal buat file", Toast.LENGTH_SHORT).show()
                    }
                }
                TampilkanGrid(currentPath)
            } else {
                Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
        .show()
    }
    
    private fun TekanLama(file: File) {
        val lama = findViewById<LinearLayout>(R.id.lama)
        val tempel = findViewById<ImageView>(R.id.tempel)
        val papan = findViewById<GridLayout>(R.id.papanTempel)
        lama.visibility = View.VISIBLE

        papan.removeAllViews()

        val item = LayoutInflater.from(this).inflate(R.layout.item_gambar, papan, false)
        val gambar = item.findViewById<ImageView>(R.id.gambar)
        if (file.isFile) { Glide.with(this).load(file).centerCrop().into(gambar) }
        else { gambar.setImageResource(R.drawable.folder) }
        papan.addView(item)

        findViewById<ImageView>(R.id.salin).setOnClickListener {
            fileSumber = file
            modeTempel = "salin"
            tempel.visibility = View.VISIBLE
            Toast.makeText(this, "Siap menyalin ${file.name}", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.potong).setOnClickListener {
            fileSumber = file
            modeTempel = "potong"
            tempel.visibility = View.VISIBLE
            Toast.makeText(this, "Siap memotong ${file.name}", Toast.LENGTH_SHORT).show()
        }

        tempel.setOnClickListener {
            val sumber = fileSumber
            val mode = modeTempel

            if (sumber == null || mode == null) {
                Toast.makeText(this, "Tidak ada file yang dipilih", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tujuan = File(currentPath, sumber.name)
            if (tujuan.exists()) {
                Toast.makeText(this, "File sudah ada di tujuan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                if (mode == "salin") {
                    sumber.copyRecursively(tujuan, overwrite = false)
                    Toast.makeText(this, "Berhasil menyalin ke ${currentPath.name}", Toast.LENGTH_SHORT).show()
                } else if (mode == "potong") {
                    sumber.copyRecursively(tujuan, overwrite = false)
                    sumber.deleteRecursively()
                    Toast.makeText(this, "Berhasil memindahkan ke ${currentPath.name}", Toast.LENGTH_SHORT).show()
                }
                fileSumber = null
                modeTempel = null
                tempel.visibility = View.GONE
                lama.visibility = View.GONE
                SegarkanGrid(currentPath)
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal tempel: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.hapus).setOnClickListener {
        AlertDialog.Builder(this)
            .setTitle("Menghapus File")
            .setMessage("Apakah anda yakin ingin menghapus ${file.name}?")
            .setPositiveButton("Ya") { _, _ ->
                if (file.deleteRecursively()) {
                    Toast.makeText(this, "${file.name} berhasil dihapus", Toast.LENGTH_SHORT).show()
                    SegarkanGrid(currentPath)
                } else {
                    Toast.makeText(this, "Gagal menghapus ${file.name}", Toast.LENGTH_SHORT).show()
                }
                lama.visibility = View.GONE
            }
            .setNegativeButton("Batal", null)
            .show()
        }

        findViewById<ImageView>(R.id.rename).setOnClickListener {
            NamaiUlang(file)
            lama.visibility = View.GONE
        }

        findViewById<ImageView>(R.id.lainya).setOnClickListener {
            Toast.makeText(this, "Belum", Toast.LENGTH_SHORT).show()
            lama.visibility = View.GONE
        }
    }
    
    private fun NamaiUlang(file: File) {
    val input = EditText(this)
    input.setText(file.name)
    
    AlertDialog.Builder(this)
        .setTitle("Namai Ulang")
        .setView(input)
        .setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }
        .setPositiveButton("Simpan") { dialog, _ ->
            val namaBaru = input.text.toString().trim()
            if (namaBaru.isNotEmpty()) {
                val fileBaru = File(file.parentFile, namaBaru)
                if (file.renameTo(fileBaru)) {
                    Toast.makeText(this, "Nama berhasil diubah", Toast.LENGTH_SHORT).show()
                    SegarkanGrid(currentPath)
                } else {
                    Toast.makeText(this, "Gagal mengganti nama", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
        .show()
    }
    
    private fun BukaFile(file: File) {
    val ekstensi = file.extension.lowercase()

    val teks = listOf("txt", "xml", "js", "html", "css", "java", "kt", "py", "c", "h", "cpp")
    val gambar = listOf("jpg", "png", "gif", "webp")
    val musik = listOf("mp3", "wav")
    val video = listOf("mp4", "3gp")

    when {
        ekstensi in teks -> {
            val intent = Intent(this, Editor::class.java)
            intent.putExtra("file", file.absolutePath)
            startActivity(intent)
        }
        ekstensi in gambar -> {
            val intent = Intent(this, Gambar::class.java)
            intent.putExtra("file", file.absolutePath)
            startActivity(intent)
        }
        
        ekstensi in video -> {
            val intent = Intent(this, Video::class.java)
            intent.putExtra("file", file.absolutePath)
            startActivity(intent)
        }
        
        ekstensi in musik -> {
            val intent = Intent(this, Musik::class.java)
            intent.putExtra("file", file.absolutePath)
            startActivity(intent)
        }
            else -> {
                Toast.makeText(this, "Belum bisa buka ${file.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onBackPressed() {
        val parent = currentPath.parentFile
            if (parent != null && parent.canRead()) {
                currentPath = parent
                TampilkanGrid(currentPath)
                JalurSekarang()
            }
    }
    
}