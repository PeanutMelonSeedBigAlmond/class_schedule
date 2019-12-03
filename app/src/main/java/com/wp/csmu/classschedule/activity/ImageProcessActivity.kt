package com.wp.csmu.classschedule

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.widget.SeekBar
import com.wp.csmu.classschedule.activity.BaseActivity
import com.wp.csmu.classschedule.io.IO

import kotlinx.android.synthetic.main.activity_image_process.*
import java.io.File
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class ImageProcessActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_process)
        setSupportActionBar(toolbar)

        seekBar.max = 100
        seekBar2.max = 25

        val raw = BitmapFactory.decodeFile(IO.backgroundImg)
        //删除图片
        with(File(IO.backgroundImg)) {
            if (exists()) {
                delete()
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(self: SeekBar?, progress: Int, fromUser: Boolean) {
                val inputBitmap = raw.copy(raw.config,raw.isMutable)
                inputBitmap.setFuzzy(seekBar2.progress)
                inputBitmap.setAlpha(self!!.max - progress)
                previewTimetableView.background = BitmapDrawable(inputBitmap)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(self: SeekBar?, progress: Int, fromUser: Boolean) {
                val inputBitmap = raw.copy(raw.config,raw.isMutable)
                inputBitmap.setFuzzy(progress)
                inputBitmap.setAlpha(seekBar!!.max - seekBar.progress)
                previewTimetableView.background = BitmapDrawable(inputBitmap)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        button.setOnClickListener {
            raw.setFuzzy(seekBar2.progress)
            raw.setAlpha(seekBar!!.max - seekBar.progress)
            saveBitmap(raw)
            finishActivity(2)
        }
    }

    private infix fun Bitmap.setAlpha(number: Int): Bitmap {
        val argb = IntArray(width * height)
        getPixels(argb, 0, width, 0, 0, width, height)
        val alpha = number * 255 / 100
        for (i in argb.indices) {
            argb[i] = (alpha.shl(24).or(argb[i].and(0x00ffffff)))
        }
        return Bitmap.createBitmap(argb, width, height, Bitmap.Config.ARGB_8888)
    }

    private infix fun Bitmap.setFuzzy(number: Int): Bitmap {
        if (number == 0) {
            return this
        }
        val inputBitmap = this
        val renderScript = RenderScript.create(this@ImageProcessActivity)
        val input = Allocation.createFromBitmap(renderScript, this)
        val output = Allocation.createTyped(renderScript, input.type)
        val scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        scriptIntrinsicBlur.setInput(input)
        scriptIntrinsicBlur.setRadius(number.toFloat())
        scriptIntrinsicBlur.forEach(output)
        output.copyTo(inputBitmap)
        renderScript.destroy()
        return inputBitmap
    }

    private fun saveBitmap(mBitmap: Bitmap) {
        val f = File(IO.backgroundImg)
        try {
            f.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var fOut: FileOutputStream? = null
        try {
            fOut = FileOutputStream(f)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        try {
            fOut!!.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            fOut!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
