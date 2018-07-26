package com.example.den.testhttp2_okhttp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MainActivity : AppCompatActivity() {

   private lateinit var progressView: ProgressBar
   private lateinit var callResponseView: TextView
   private lateinit var protocolView: TextView
   private lateinit var makeCallButton: Button
   private lateinit var httpsCheckBox: CheckBox

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_main)

      progressView = findViewById(R.id.progress)
      callResponseView = findViewById(R.id.call_response_text_view)
      protocolView = findViewById(R.id.protocol_caption)
      makeCallButton = findViewById(R.id.make_call_button)
      httpsCheckBox = findViewById(R.id.use_https_checkbox)

      makeCallButton.setOnClickListener { makeApiCall(httpsCheckBox.isChecked) }
      findViewById<TextView>(R.id.android_version_caption).text =
            getString(R.string.android_version_caption_format, android.os.Build.VERSION.RELEASE,
                  android.os.Build.VERSION.SDK_INT)
   }

   private fun makeApiCall(useHttps: Boolean) {
      makeCallButton.isEnabled = false
      protocolView.visibility = GONE
      callResponseView.text = ""
      progressView.visibility = VISIBLE

      // try block below is needed for correct https call on lower Android versions
      try {
         ProviderInstaller.installIfNeeded(this)
      } catch (e: GooglePlayServicesRepairableException) {
         // Indicates that Google Play services is out of date, disabled, etc.
         // Prompt the user to install/update/enable Google Play services.
         GoogleApiAvailability.getInstance()
               .showErrorNotification(this, e.connectionStatusCode)
         return
      } catch (e: GooglePlayServicesNotAvailableException) {
         // Indicates a non-recoverable error; the ProviderInstaller is not able
         // to install an up-to-date Provider.
         return
      }

      launch(UI) {
         val response = withContext(CommonPool) {
            val url = if (useHttps) {
               "https://jsonplaceholder.typicode.com/users/1/posts"
            } else {
               "http://jsonplaceholder.typicode.com/users/1/posts"
            }

            val client = OkHttpClient()

            val request = Request.Builder()
                  .url(url)
                  .method("GET", null)
                  .build()

            val response = client.newCall(request).execute()
            Pair(response.protocol().name, response.body()?.string())
         }

         processResponse(response)
      }
   }

   private fun processResponse(response: Pair<String, String?>) {
      makeCallButton.isEnabled = true
      protocolView.text = getString(R.string.using_protocol_caption_format, response.first)
      protocolView.visibility = VISIBLE
      progressView.visibility = GONE
      callResponseView.text = response.second
   }
}
