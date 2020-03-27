package com.wp.csmu.classschedule.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.widget.SeekBar
import com.wp.csmu.classschedule.R
import com.wp.csmu.classschedule.io.IO
import com.wp.csmu.classschedule.view.scheduletable.AppSubjects
import kotlinx.android.synthetic.main.activity_image_process.*
import java.io.*
import kotlin.concurrent.thread


class ImageProcessActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_process)
        setSupportActionBar(toolbar)
        init()

        seekBarBlur.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                imageViewRaw.background.mutate().alpha = 255 - progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                buttonConfirm.isEnabled = false
                seekBarMask.isEnabled = false
                val objectAnimLinearLayout = ObjectAnimator.ofFloat(view1, "alpha", 1.0f, 0.0f)
                val objectAnimTextView = ObjectAnimator.ofFloat(textView5, "alpha", 1.0f, 0.0f)
                val objectAnimSeekBar = ObjectAnimator.ofFloat(seekBarMask, "alpha", 1.0f, 0.0f)
                val objectAnimConfirm = ObjectAnimator.ofFloat(buttonConfirm, "alpha", 1.0f, 0.0f)
                val animatorSet = AnimatorSet()
                animatorSet.duration = 300
                animatorSet.playTogether(objectAnimLinearLayout, objectAnimTextView, objectAnimSeekBar, objectAnimConfirm)
                animatorSet.start()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                buttonConfirm.isEnabled = true
                seekBarMask.isEnabled = true
                val objectAnimLinearLayout = ObjectAnimator.ofFloat(view1, "alpha", 0.0f, 1.0f)
                val objectAnimTextView = ObjectAnimator.ofFloat(textView5, "alpha", 0.0f, 1.0f)
                val objectAnimSeekBar = ObjectAnimator.ofFloat(seekBarMask, "alpha", 0.0f, 1.0f)
                val objectAnimConfirm = ObjectAnimator.ofFloat(buttonConfirm, "alpha", 0.0f, 1.0f)
                val animatorSet = AnimatorSet()
                animatorSet.duration = 300
                animatorSet.playTogether(objectAnimLinearLayout, objectAnimTextView, objectAnimSeekBar, objectAnimConfirm)
                animatorSet.start()
            }
        })
        seekBarMask.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                imageViewMask.background.mutate().alpha = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                seekBarBlur.isEnabled = false
                buttonConfirm.isEnabled = false
                val objectAnimLinearLayout = ObjectAnimator.ofFloat(view1, "alpha", 1.0f, 0.0f)
                val objectAnimTextView = ObjectAnimator.ofFloat(textView4, "alpha", 1.0f, 0.0f)
                val objectAnimSeekBar = ObjectAnimator.ofFloat(seekBarBlur, "alpha", 1.0f, 0.0f)
                val objectAnimConfirm = ObjectAnimator.ofFloat(buttonConfirm, "alpha", 1.0f, 0.0f)
                val animatorSet = AnimatorSet()
                animatorSet.duration = 300
                animatorSet.playTogether(objectAnimLinearLayout, objectAnimTextView, objectAnimSeekBar, objectAnimConfirm)
                animatorSet.start()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBarBlur.isEnabled = true
                buttonConfirm.isEnabled = true
                val objectAnimLinearLayout = ObjectAnimator.ofFloat(view1, "alpha", 0.0f, 1.0f)
                val objectAnimTextView = ObjectAnimator.ofFloat(textView4, "alpha", 0.0f, 1.0f)
                val objectAnimSeekBar = ObjectAnimator.ofFloat(seekBarBlur, "alpha", 0.0f, 1.0f)
                val objectAnimConfirm = ObjectAnimator.ofFloat(buttonConfirm, "alpha", 0.0f, 1.0f)
                val animatorSet = AnimatorSet()
                animatorSet.duration = 300
                animatorSet.playTogether(objectAnimLinearLayout, objectAnimTextView, objectAnimSeekBar, objectAnimConfirm)
                animatorSet.start()
            }
        })

        buttonConfirm.setOnClickListener {
            val progressDialog = ProgressDialog(this@ImageProcessActivity)
            progressDialog.setMessage("正在处理")
            progressDialog.setCancelable(false)
            progressDialog.show()
            thread {
                saveBitmap(drawableToBitmap(mergePicture(imageViewBulr.background, imageViewRaw.background, imageViewMask.background)))
                runOnUiThread {
                    progressDialog.dismiss()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }

    private fun init() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("正在处理")
        progressDialog.show()
        view1.setBackgroundColor(Color.argb(255, 255, 255, 255))
        imageViewMask.setBackgroundColor(Color.argb(255, 0, 0, 0))
        timetableView.source(AppSubjects.subjects.toList())
        timetableView.alpha(0f, 0f, 1f)
        timetableView.showView()
        imageViewMask.background.mutate().alpha = 0
        thread {
            val image = readImage()
            runOnUiThread {
                imageViewRaw.background = BitmapDrawable(resources, image)
            }
            val blur = image.copy(image.config, true)
                    .setFuzzy(25)
                    .setFuzzy(25)
                    .setFuzzy(25)
                    .setFuzzy(25)
                    .setFuzzy(25)
                    .setFuzzy(25)
            runOnUiThread {
                imageViewBulr.background = BitmapDrawable(resources, blur)
            }
        }
        progressDialog.dismiss()
    }


    private fun readImage(): Bitmap {
        val fis = FileInputStream(IO.backgroundImg)
        val bitmap = BitmapFactory.decodeStream(fis)
        fis.close()
        return bitmap
    }

    //number=0..25
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

    private fun mergePicture(vararg images: Drawable) = LayerDrawable(images)

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
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
