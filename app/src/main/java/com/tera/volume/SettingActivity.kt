package com.tera.volume

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tera.palettedialog.PaletteDialog
import com.tera.volume.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    private var frameColor = 0
    private var barColor = 0
    private var panelColor = 0
    private var winColor = 0
    private var colorEnabled = false
    private var style = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Не выключать экран
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Запретить поворот экрана
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        frameColor = intent.getIntExtra(MyConst.FRAME_COLOR, 0)
        barColor = intent.getIntExtra(MyConst.BAR_COLOR, 0)
        panelColor = intent.getIntExtra(MyConst.PANEL_COLOR, 0)
        winColor = intent.getIntExtra(MyConst.WIN_COLOR, 0)
        colorEnabled = intent.getBooleanExtra(MyConst.COLOR_ENABLED, true)
        style = intent.getIntExtra(MyConst.STYLE, 0)

        initButtons()
        setColor()
        setParams()
    }

    private fun initButtons() = with(binding) {
        bnWinColor.setOnClickListener {
            openDialog(winColor, 1)
        }
        bnFrameColor.setOnClickListener {
            openDialog(frameColor, 2)
        }
        bnBarColor.setOnClickListener {
            openDialog(barColor, 3)
        }
        bnPanelColor.setOnClickListener {
            openDialog(panelColor, 4)
        }


        bnOk.setOnClickListener {
            goHome()
        }
        bnCansel.setOnClickListener {
            finish()
        }

        chColor.setOnCheckedChangeListener { _, isChecked ->
            colorEnabled = isChecked
        }

        rgStyle.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.rbWave -> {
                    style = 0
                }
                R.id.rbBar -> {
                    style = 1
                }
                R.id.rbSeg -> {
                    style = 2
                }
            }
        }
    }

    private fun goHome() {
        val intent = Intent()
        intent.putExtra(MyConst.WIN_COLOR, winColor)
        intent.putExtra(MyConst.FRAME_COLOR, frameColor)
        intent.putExtra(MyConst.BAR_COLOR, barColor)
        intent.putExtra(MyConst.PANEL_COLOR, panelColor)
        intent.putExtra(MyConst.COLOR_ENABLED, colorEnabled)
        intent.putExtra(MyConst.STYLE, style)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun openDialog(color: Int, pos: Int) {
        PaletteDialog(this, color)
            .setOnClickListener {
                when (pos) {
                    1 -> winColor = it
                    2 -> frameColor = it
                    3 -> barColor = it
                    4 -> panelColor = it
                }
                setColor()
            }
    }

    private fun setColor() = with(binding) {
        vWinColor.setBackgroundColor(winColor)
        vFrameColor.setBackgroundColor(frameColor)
        vBarColor.setBackgroundColor(barColor)
        vPanelColor.setBackgroundColor(panelColor)

    }

    private fun setParams() = with(binding) {
        chColor.isChecked = colorEnabled
        when (style) {
            0 -> rbWave.isChecked = true
            1 -> rbBar.isChecked = true
            2 -> rbSeg.isChecked = true
        }
    }

}