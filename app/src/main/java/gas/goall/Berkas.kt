package gas.goall

import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.os.Environment
import android.view.*
import java.io.File
import android.os.Handler
import android.os.Looper
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import com.bumptech.glide.Glide
import android.content.Intent

class Berkas : AppCompatActivity() {

    private lateinit var currentPath: File
    private var selectedFiles: MutableList<File> = mutableListOf()
    private var cutFile: File? = null
    private var isSelecting = false
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private val STORAGE_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.berkas)

        if (checkStoragePermission()) {
            initializePaths()
        } else {
            requestStoragePermission()
        }

        findViewById<ImageView>(R.id.nav)?.setOnClickListener {
            val liner = findViewById<LinearLayout>(R.id.liner)
            liner.visibility = if (liner.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        findViewById<TextView>(R.id.keluar)?.setOnClickListener { finish() }

        findViewById<ImageView>(R.id.apk)?.setOnClickListener {
            currentPath = filesDir
            refreshExplorer()
        }

        findViewById<ImageView>(R.id.sdCard)?.setOnClickListener {
            if (checkStoragePermission()) {
                currentPath = Environment.getExternalStorageDirectory()
                refreshExplorer()
            } else {
                requestStoragePermission()
            }
        }
        
        findViewById<TextView>(R.id.baru)?.setOnClickListener {
            BuatBaru()
        }
    }

    private fun initializePaths() {
        currentPath = filesDir
        MulaiExplorer(currentPath)
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.isNotEmpty() && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializePaths()
        } else {
            Toast.makeText(this, "Izin diperlukan untuk mengakses penyimpanan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshExplorer() {
        findViewById<GridLayout>(R.id.grid)?.removeAllViews()
        MulaiExplorer(currentPath)
    }

    private fun MulaiExplorer(file: File) {
        val grid = findViewById<GridLayout>(R.id.grid) ?: return
        val kembali = findViewById<ImageView>(R.id.kembali) ?: return

        currentPath = file
        JalurSekarang()

        kembali.visibility = if (file.parentFile == null || file == filesDir || 
            file.absolutePath == "/storage/emulated/0") View.GONE else View.VISIBLE
        kembali.setOnClickListener {
            MulaiExplorer(file.parentFile ?: file)
        }

        grid.removeAllViews()

        executor.execute {
            val filesAndDirs = file.listFiles() ?: emptyArray()
            
            handler.post {
                filesAndDirs.forEach { itemFile ->
                    val itemView = LayoutInflater.from(this).inflate(R.layout.item_vertical, grid, false)
                    val gambar = itemView.findViewById<ImageView>(R.id.gambar)
                    val nama = itemView.findViewById<TextView>(R.id.nama)
                    val centang = itemView.findViewById<TextView>(R.id.centang)

                    nama.text = itemFile.name

                    if (itemFile.isDirectory) {
                        gambar.setImageResource(R.drawable.folder)
                    } else {
                        val extension = itemFile.extension.lowercase()
                        val ekstensiGambar = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
                        
                        if (extension in ekstensiGambar) {
                        Glide.with(this)
                        .load(itemFile)
                        .centerCrop()
                        .into(gambar)
                        }
                        val drawableRes = when (extension) {
                            "py" -> R.drawable.python
                            "kt" -> R.drawable.kotlin
                            "java" -> R.drawable.java
                            "js" -> R.drawable.js
                            "c", "h", "cpp" -> R.drawable.c
                            "html" -> R.drawable.html
                            "css" -> R.drawable.css
                            "gradle", "kts" -> R.drawable.gradle
                            "apk", "deb", "sh", "exe" -> R.drawable.apk
                            "zip", "xz", "tar", "gz" -> R.drawable.ekstrak
                            else -> R.drawable.file
                        }
                        gambar.setImageResource(drawableRes)
                    }

                    itemView.setOnClickListener {
                        if (itemFile.isDirectory) {
                            MulaiExplorer(itemFile)
                        } else {
                            MembukaFile(itemFile)
                        }
                    }
                    
                    itemView.setOnLongClickListener {
                        TekanLama(itemFile)
                        true
                    }

                    itemView.tag = itemFile
                    grid.addView(itemView)
                }
                
                SegarkanGrid()
            }
        }
    }

    private fun operasiFileDiBackground(operation: () -> Unit) {
        executor.execute {
            try {
                operation()
                handler.post { MulaiExplorer(currentPath) }
            } catch (e: Exception) {
                handler.post {
                    Toast.makeText(this@Berkas, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
    
    private fun JalurSekarang() {
        val jalur = findViewById<TextView>(R.id.jalur)
        jalur.text = currentPath.absolutePath
    }
    
    private fun BuatBaru() {
    val item = LayoutInflater.from(this).inflate(R.layout.item_tulis, null)
    val tulis = item.findViewById<EditText>(R.id.tulis)
    val batal = item.findViewById<Button>(R.id.batal)
    val simpan = item.findViewById<Button>(R.id.simpan)
    val popup = PopupWindow(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

    val opsi = arrayOf("Folder", "File")

    AlertDialog.Builder(this)
        .setTitle("Buat Baru")
        .setItems(opsi) { _, which ->
            popup.showAtLocation(findViewById(R.id.grid), Gravity.CENTER, 0, 0)

            batal.setOnClickListener { popup.dismiss() }

            simpan.setOnClickListener {
                val namaBaru = tulis.text.toString().trim()
                if (namaBaru.isNotEmpty()) {
                    val pathBaru = File(currentPath, namaBaru)
                    if (which == 0) {
                        pathBaru.mkdirs()
                    } else {
                        pathBaru.createNewFile()
                    }
                    popup.dismiss()
                     SegarkanGrid(true)
                } else {
                    Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
        }
        .show()
    }
    
    private fun SegarkanGrid(refreshAll: Boolean = false) {
    if (refreshAll) {
        refreshExplorer()
        return
    }

    val grid = findViewById<GridLayout>(R.id.grid)
    for (i in 0 until grid.childCount) {
        val itemView = grid.getChildAt(i)
        val file = itemView.tag as? File ?: continue
        val centang = itemView.findViewById<TextView>(R.id.centang)
        centang.visibility = if (selectedFiles.contains(file)) View.VISIBLE else View.GONE
    }
}

    private fun TekanLama(file: File) {
        val lama = findViewById<LinearLayout>(R.id.lama)
        val tempel = findViewById<ImageView>(R.id.tempel)
        val centang = findViewById<TextView>(R.id.centang)

        if (!isSelecting) {
            isSelecting = true
            lama.visibility = View.VISIBLE
            selectedFiles.add(file)
            centang.visibility = View.VISIBLE
            SegarkanGrid()
        } else {
            if (selectedFiles.contains(file)) {
                selectedFiles.remove(file)
                centang.visibility = View.GONE
            } else {
                selectedFiles.add(file)
                centang.visibility = View.VISIBLE
            }

            if (selectedFiles.isEmpty()) {
                lama.visibility = View.GONE
                isSelecting = false
            }
        }

        tempel.visibility = if (cutFile != null) View.VISIBLE else View.GONE

        findViewById<ImageView>(R.id.salin).setOnClickListener {
            Toast.makeText(this, "Salin dipilih (${selectedFiles.size} file)", Toast.LENGTH_SHORT).show()
            cutFile = null
            isSelecting = false
            lama.visibility = View.GONE
        }

        findViewById<ImageView>(R.id.potong).setOnClickListener {
            Toast.makeText(this, "Potong dipilih (${selectedFiles.size} file)", Toast.LENGTH_SHORT).show()
            cutFile = null 
            isSelecting = false
            lama.visibility = View.GONE
        }

        tempel.setOnClickListener {
            if (cutFile != null && selectedFiles.isNotEmpty()) {
                val destinationDir = currentPath
                var successCount = 0
                selectedFiles.forEach { fileToMove ->
                    val newFile = File(destinationDir, fileToMove.name)
                    try {
                        fileToMove.renameTo(newFile)
                        successCount++
                    } catch (e: Exception) {
                        Toast.makeText(this, "Gagal memindahkan ${fileToMove.name}", Toast.LENGTH_SHORT).show()
                    }
                }
                Toast.makeText(this, "$successCount file berhasil dipindahkan", Toast.LENGTH_SHORT).show()
                cutFile = null
                selectedFiles.clear()
                isSelecting = false
                lama.visibility = View.GONE
                SegarkanGrid(true)
            } else if (cutFile != null && selectedFiles.isEmpty()) {
                val destinationDir = currentPath
                val newFile = File(destinationDir, cutFile!!.name)
                if (cutFile!!.renameTo(newFile)) {
                    Toast.makeText(this, "${cutFile!!.name} berhasil dipindahkan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal memindahkan ${cutFile!!.name}", Toast.LENGTH_SHORT).show()
                }
                cutFile = null
                lama.visibility = View.GONE
                MulaiExplorer(currentPath)
            } else {
                Toast.makeText(this, "Tidak ada file yang disalin atau dipotong", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.rename).setOnClickListener {
            if (selectedFiles.size == 1) {
                TampilkanDialogRename(selectedFiles.first())
            } else {
                Toast.makeText(this, "Pilih satu file untuk diubah namanya", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.hapus).setOnClickListener {
            if (selectedFiles.isNotEmpty()) {
                TampilkanDialogHapus(selectedFiles.toList())
            } else {
                Toast.makeText(this, "Tidak ada file yang dipilih untuk dihapus", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.lainya).setOnClickListener {
            if (selectedFiles.size == 1) {
                TampilkanDetailFile(selectedFiles.first())
            } else {
                Toast.makeText(this, "Pilih satu file untuk melihat detail", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
        private fun TampilkanDialogRename(file: File) {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Ubah Nama")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    val newFile = File(file.parentFile, newName)
                    if (file.renameTo(newFile)) {
                        Toast.makeText(this, "Berhasil diubah namanya menjadi $newName", Toast.LENGTH_SHORT).show()
                        MulaiExplorer(currentPath)
                    } else {
                        Toast.makeText(this, "Gagal mengubah nama", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Nama baru tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
                isSelecting = false
                findViewById<LinearLayout>(R.id.lama)?.visibility = View.GONE
                selectedFiles.clear()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
                isSelecting = false
                findViewById<LinearLayout>(R.id.lama)?.visibility = View.GONE
                selectedFiles.clear()
            }
            .show()
    }
    
        private fun TampilkanDetailFile(file: File) {
        val details = """
            Nama: ${file.name}
            Path: ${file.absolutePath}
            Ukuran: ${formatFileSize(file.length())}
            Tipe: ${if (file.isDirectory) "Direktori" else "File"}
            Terakhir diubah: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(file.lastModified()))}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Detail File")
            .setMessage(details)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                isSelecting = false
                findViewById<LinearLayout>(R.id.lama)?.visibility = View.GONE
                selectedFiles.clear()
            }
            .show()
    }
    
        private fun TampilkanDialogHapus(filesToDelete: List<File>) {
        AlertDialog.Builder(this)
            .setTitle("Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus ${filesToDelete.size} file?")
            .setPositiveButton("Hapus") { _, _ ->
                var successCount = 0
                filesToDelete.forEach { file ->
                    if (file.deleteRecursively()) {
                        successCount++
                    } else {
                        Toast.makeText(this, "Gagal menghapus ${file.name}", Toast.LENGTH_SHORT).show()
                    }
                }
                Toast.makeText(this, "$successCount file berhasil dihapus", Toast.LENGTH_SHORT).show()
                MulaiExplorer(currentPath)
                isSelecting = false
                findViewById<LinearLayout>(R.id.lama)?.visibility = View.GONE
                selectedFiles.clear()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
                isSelecting = false
                findViewById<LinearLayout>(R.id.lama)?.visibility = View.GONE
                selectedFiles.clear()
            }
            .show()
    }

    fun File.deleteRecursively(): Boolean {
        if (isDirectory) {
            listFiles()?.forEach {
                if (!it.deleteRecursively()) return false
            }
        }
        return delete()
    }


    private fun formatFileSize(size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var bytes = size.toDouble()
        var i = 0
        while (bytes >= 1024 && i < units.size - 1) {
            bytes /= 1024
            i++
        }
        return String.format("%.2f %s", bytes, units[i])
    }

    private fun MembukaFile(file: File) {
    val gambar = Intent(this, Gambar::class.java)
    val editor = Intent(this, Editor::class.java)
    val video = Intent(this, Video::class.java)
    gambar.putExtra("file", file.absolutePath)
    editor.putExtra("file", file.absolutePath)
    video.putExtra("file", file.absolutePath)

    AlertDialog.Builder(this)
        .setTitle("Pilih Aksi")
        .setItems(arrayOf("Text", "Gambar", "Vidio", "Musik", "Lainya")) { _, which ->
            when (which) {
                0 -> startActivity(editor)
                1 -> startActivity(gambar)
                2 -> startActivity(video)
                3 -> Toast.makeText(this, "Sabar", Toast.LENGTH_SHORT).show()
                4 -> Toast.makeText(this, "Sabar", Toast.LENGTH_SHORT).show()
            }
        }
        .show()
}
    
}