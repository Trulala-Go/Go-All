package gas.goall

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.*
import android.widget.BaseExpandableListAdapter
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File

class Editor : AppCompatActivity() {

    private var selectedDirectoryUri: Uri? = null
    private lateinit var daftar: ExpandableListView
    private lateinit var fileListAdapter: FileListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editor)

        val file: File? = intent.getSerializableExtra("file") as? File

        val tulis = findViewById<EditText>(R.id.tulis)

        if (file != null && file.exists()) {
            tulis.setText(file.readText())
        } else {
            tulis.setText("")
        }

        findViewById<TextView>(R.id.keluar).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.nav).setOnClickListener {
            val liner = findViewById<LinearLayout>(R.id.liner)
            liner.visibility = if (liner.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        findViewById<ImageView>(R.id.simpan).setOnClickListener {
            if (file != null) {
                file.writeText(tulis.text.toString())
                Toast.makeText(this, "${file.name} Tersimpan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "File tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.pilih).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startActivityForResult(intent, REQUEST_DIRECTORY)
        }

        daftar = findViewById(R.id.daftar)
        fileListAdapter = FileListAdapter(this, emptyList())
        daftar.setAdapter(fileListAdapter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_DIRECTORY && resultCode == RESULT_OK) {
            selectedDirectoryUri = data?.data
            selectedDirectoryUri?.let { uri ->
                val files = getFilesInDirectory(uri)
                fileListAdapter.updateData(files)
            }
        }
    }


private fun getFilesInDirectory(uri: Uri): List<FileItem> {
    val filesList = mutableListOf<FileItem>()
    val context = this

    context.contentResolver.query(
        DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri)),
        arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_DOCUMENT_ID),
        null,
        null,
        null
    )?.use { cursor ->
        while (cursor.moveToNext()) {
            val displayName = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
            val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE))
            val documentId = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID))
            val isDirectory = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
            filesList.add(FileItem(displayName, isDirectory, DocumentsContract.buildDocumentUriUsingTree(uri, documentId)))
        }
    }
    return filesList
}

data class FileItem(val name: String, val isDirectory: Boolean, val uri: Uri? = null)


    companion object {
        private const val REQUEST_DIRECTORY = 101
    }
}

    class FileListAdapter(
        private val context: android.content.Context,
        private var fileList: List<Editor.FileItem>
    ) : BaseExpandableListAdapter() {

    fun updateData(newList: List<Editor.FileItem>) {
        fileList = newList
        notifyDataSetChanged()
    }

    override fun getGroupCount(): Int = 1

    override fun getChildrenCount(groupPosition: Int): Int = fileList.size

    override fun getGroup(groupPosition: Int): Any = "Files"

    override fun getChild(groupPosition: Int, childPosition: Int): Editor.FileItem = fileList[childPosition]

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = false

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = getGroup(groupPosition).toString()
        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val item = getChild(groupPosition, childPosition)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_horizontal, parent, false)
        val gambar = view.findViewById<ImageView>(R.id.gambar)
        val nama = view.findViewById<TextView>(R.id.nama)

        nama.text = item.name
        if (item.isDirectory) {
            gambar.setImageResource(R.drawable.folder)
        } else {
            val fileExtension = item.name.substringAfterLast('.', "")
            val drawableRes = when (fileExtension.lowercase()) {
                "kt" -> R.drawable.kotlin
                "java" -> R.drawable.java
                "py" -> R.drawable.python
                "c", "h", "cpp" -> R.drawable.c
                "html" -> R.drawable.html
                "css" -> R.drawable.css
                "gradle", "kts" -> R.drawable.gradle
                "js" -> R.drawable.js
                else -> R.drawable.file
            }
            gambar.setImageResource(drawableRes)
        }
        return view
    }
}
