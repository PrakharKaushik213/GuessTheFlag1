package com.example.guesstheflag

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.guesstheflag.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: viewmodel
    var code: String? = null
    var flagUrls = ArrayList<String>()
    var flagNames = ArrayList<String>()
    var downloaded: Bitmap? = null
    var answers = arrayOfNulls<String>(4)
    var correctanswer=0
    var chosenflag =0
    inner class photodownloader :
        AsyncTask<String, Void, Bitmap>() {
        protected override fun doInBackground(vararg urls: String): Bitmap? {
            try {
                val url = URL(urls[0])
                val connection =
                    url.openConnection() as HttpsURLConnection
                connection.connect()
                val inputStream = connection.inputStream
                downloaded=BitmapFactory.decodeStream(inputStream)
                return downloaded
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }

    inner class namedownlaoder :
        AsyncTask<String, Void, String>() {
        var result: String? = null
        protected override fun doInBackground(vararg urls: String): String? {
             try {
                val url = URL(urls[0])
                val connection =
                    url.openConnection() as HttpsURLConnection
                val inputStream = connection.inputStream
                val reader = InputStreamReader(inputStream)
                var data = reader.read()
                while (data != -1) {
                    val current = data.toChar()
                    result += current
                    data = reader.read()
                }
                 return result
            } catch (e: Exception) {
                e.printStackTrace()
               return "net not connected"
            }
        }
    }

    fun update() {
        val rand = Random()

         chosenflag = rand.nextInt(flagUrls.size)
        val photodownload = photodownloader()
        try {
            downloaded = photodownload.execute(flagUrls[  chosenflag]).get()
            binding.imageView!!.setImageBitmap(downloaded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var wronganswer: Int
        correctanswer = rand.nextInt(4)

        for (i in 0..3) {
            if (i ==  correctanswer) {
                answers[i] = flagNames[ chosenflag]
            } else {
                wronganswer = rand.nextInt(flagUrls.size)
                while (wronganswer ==   chosenflag) {
                    wronganswer = rand.nextInt(flagNames.size)
                }
                answers[i] = flagNames[wronganswer]
            }
        }
        binding.apply {
            button1.text = answers[0]
            button2.text = answers[1]
            button3.text = answers[2]
            button4.text = answers[3]
            invalidateAll()
        }
    }

    fun checkers(view: View) {
        if (view.tag.toString() == Integer.toString( correctanswer)) {
            binding.textView.text = "Correct!!!"
            viewModel.right++
        } else {
            binding.textView.text = "Wrong answer! Correct ans:" + flagNames[chosenflag]
        }
        viewModel.toatl++
        binding.textView2.text = "${viewModel.right}/${viewModel.toatl}"
        update()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(viewmodel::class.java)
        val download = namedownlaoder()

        try {
            code = download.execute("https://www.flagsimporter.com/asian-flags").get()
            val split = code!!.split("<span class=\"base\" data-ui-id=\"page-title-wrapper\" >Asian Country Flags</span>    </h1>").toTypedArray()
            val code2 = split[1]
            val code2half = code2.split("        <div class=\"owl-control-top desc-center\"><div class=\"block products-grid\">\n").toTypedArray()
            var p = Pattern.compile("<img src=\"(.*?)\" alt=\"\" /></a></div>")
            var m = p.matcher(code2half[0])
            while (m.find()) {
                println(m.group(1))
                flagUrls.add(m.group(1))
            }
            p = Pattern.compile("pricedesc\">(.*?) Flags</a></div>")
            m = p.matcher(code2half[0])
            while (m.find()) {
                println(m.group(1))
                flagNames.add(m.group(1))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        update()
    }
}
