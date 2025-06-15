package com.example.smarthybridwebclient.presentation.main

import android.annotation.SuppressLint
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.smarthybridwebclient.utils.FileUtils
import com.example.smarthybridwebclient.utils.WebAppInterface
import androidx.core.net.toUri
import com.example.smarthybridwebclient.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var isOfflinePageShown = false
    private var shouldReload = false



    private val fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val uris = arrayOf(result.data!!.data!!)
            filePathCallback?.onReceiveValue(uris)
            val fileName = FileUtils.getFileName(this, uris[0])
            binding.webView.evaluateJavascript("notifyUploadSuccess('$fileName');", null)
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    finish()
                }
            }
        })



        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                if (shouldReload) {
                    runOnUiThread {
                        binding.webView.loadUrl(ONLINE_URL)
                        shouldReload = false
                    }
                }
            }
        })

        binding.githubLoginBtn.setOnClickListener {
            binding.webView.loadUrl(AUTH_URL)
        }

        WebView.setWebContentsDebuggingEnabled(true)

        val webView = binding.webView
        val progressBar: ProgressBar = binding.progressBar
        val errorText: TextView = binding.errorText

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.addJavascriptInterface(WebAppInterface(this), "Android")

        if (isNetworkAvailable()) {
            webView.loadUrl(ONLINE_URL)
        } else {
            isOfflinePageShown = true
            webView.loadUrl("file:///android_asset/offline.html")
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                errorText.visibility = View.GONE
                webView.visibility = View.VISIBLE
                if (url?.contains("offline.html") == true) shouldReload = true
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (url.startsWith(REDIRECT_URI)) {
                    val code = url.toUri().getQueryParameter("code")
                    Toast.makeText(this@MainActivity, "OAuth Code: $code", Toast.LENGTH_LONG).show()
                    return true
                }
                return false
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                progressBar.visibility = View.GONE
                webView.visibility = View.GONE
                errorText.text = "Не удалось загрузить страницу"
                errorText.visibility = View.VISIBLE
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
                return if (!isNetworkAvailable()) {
                    try {
                        val inputStream = assets.open("offline.html")
                        WebResourceResponse("text/html", "UTF-8", inputStream)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    super.shouldInterceptRequest(view, request)
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback = filePathCallback
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                fileChooserLauncher.launch(Intent.createChooser(intent, "Выберите файл"))
                return true

            }

        }



    }


    fun startGitHubOAuth() {
        runOnUiThread {
            binding.webView.loadUrl(AUTH_URL)
        }
    }

    fun tryReload() {
        if (isNetworkAvailable()) {
            binding.webView.loadUrl(ONLINE_URL)
            isOfflinePageShown = false
        } else {
            Toast.makeText(this, "Интернет всё ещё недоступен", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }


    companion object{
    private const val GITHUB_CLIENT_ID = "Ov23liEqNT9njhxc7txQ"
    private const val REDIRECT_URI = "https://example.com/oauth"
    private const val AUTH_URL = "https://github.com/login/oauth/authorize?client_id=$GITHUB_CLIENT_ID&scope=read:user&redirect_uri=$REDIRECT_URI"
    private const val ONLINE_URL = "https://10fastfingers.com"
    }

}
