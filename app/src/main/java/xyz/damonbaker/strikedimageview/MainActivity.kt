package xyz.damonbaker.strikedimageview

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var strikedImage: xyz.damonbaker.strikedimageview.StrikedImageView
    val colors = listOf(Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED, Color.GRAY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        strikedImage = findViewById(R.id.striked_image)
//        strikedImage.isStriked = false

        val colorator = colors.listIterator()
        strikedImage.setOnClickListener {
            if (colorator.hasNext()) strikedImage.imageTintList = ColorStateList.valueOf(colorator.next())

            strikedImage.isStriked = !strikedImage.isStriked
        }
    }
}
