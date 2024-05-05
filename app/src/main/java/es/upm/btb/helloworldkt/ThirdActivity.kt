package es.upm.btb.helloworldkt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast

class ThirdActivity : AppCompatActivity() {
    private val TAG = "ThirdActivityRegister"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        // Toast adicional para la tercera actividad
        Toast.makeText(this, "Has abierto la tercera actividad", Toast.LENGTH_SHORT).show()

        Log.d(TAG, "Register. The third activity has being created.");


        val buttonPrevious: Button = findViewById(R.id.thirdButton)
        buttonPrevious.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }

    }
}