package gas.goall

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class Browser : AppCompatActivity() {
    private lateinit var web: WebView
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.browser)

        findViewById<TextView>(R.id.keluar).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.nav).setOnClickListener {
            val liner = findViewById<LinearLayout>(R.id.liner)
            liner.visibility = if (liner.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        web = findViewById(R.id.web)
        val cari = findViewById<EditText>(R.id.cari)
        loading = findViewById(R.id.loading)

        web.settings.javaScriptEnabled = true
        web.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                loading.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                loading.visibility = View.GONE
                cari.setText(url)
            }
        }
        
        web.webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
        loading.progress = newProgress
        loading.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            }
        }

        val terimaFile = intent.getStringExtra("file")
        if (terimaFile != null) web.loadUrl(terimaFile)
        else web.loadUrl("https://www.google.com/")

        findViewById<TextView>(R.id.mencari).setOnClickListener {
            val link = cari.text.toString()
            web.loadUrl(link)
        }
    }

    override fun onBackPressed() {
        if (web.canGoBack()) {
            web.goBack()
        } else {
            super.onBackPressed()
        }
    }
}