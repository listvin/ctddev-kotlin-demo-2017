package ru.ifmo.rain.listvin.linkcut

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.demo.R
import kotlinx.android.synthetic.main.activity_draft.*
import java.util.*

class DraftActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draft)

        (findViewById<View>(R.id.button4) as Button).setOnClickListener(this)
    }

    private var cnt = -1
    private val r = Random(239)
    override fun onClick(v: View?) {
        if (cnt++ == -1) {
            Toast.makeText(this, "GAA!!", Toast.LENGTH_SHORT).show()
        } else {
//            val thr = sample.nodeCount
//            var a: Int; var b: Int
//            do {
//                a = r.nextInt(thr)
//                b = r.nextInt(thr)
//            } while (sample.getEdge("$a->$b") != null || sample.getEdge("$b->$a") != null)
//            sample.addEdge("$a->$b", a, b)
        }
        (findViewById<View>(R.id.button4) as Button).setText(cnt.toString())
    }
}