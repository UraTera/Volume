package com.tera.volume

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tera.visualizer.VisualizerManager
import com.tera.volume.databinding.ActivityMainBinding
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var visualizer: VisualizerManager? = null
    private var maxVolume = 0
    private var currVolume = 0
    private var slCurr = 0f // Значение слайдера

    private lateinit var sp: SharedPreferences

    private var frameColor = 0
    private var barColor = 0
    private var panelColor = 0
    private var winColor = 0
    private var colorEnabled = false
    private var style = 0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            initVisualizer()
        } else {
            finish()
        }
    }

    private var launcher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Не выключать экран
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        sp = getSharedPreferences("settings", MODE_PRIVATE)
//        sp.edit {
//            clear()
//        }
        winColor = sp.getInt(MyConst.WIN_COLOR, Color.BLACK)
        frameColor = sp.getInt(MyConst.FRAME_COLOR, Color.WHITE)
        barColor = sp.getInt(MyConst.BAR_COLOR, Color.GREEN)
        panelColor = sp.getInt(MyConst.PANEL_COLOR, Color.BLACK)
        colorEnabled = sp.getBoolean(MyConst.COLOR_ENABLED, true)
        style = sp.getInt(MyConst.STYLE, 0)

        visualizer = VisualizerManager(this)
        visualizer!!.setView(binding.surface)

        initButtons()
        initVolume()
        setParams()
        setText()
        setTheme()

        // Проверить разрешение
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
            initVisualizer()
        else
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    val intent = result.data
                    if (intent != null) {
                        winColor = intent.getIntExtra(MyConst.WIN_COLOR, 0)
                        frameColor = intent.getIntExtra(MyConst.FRAME_COLOR, 0)
                        barColor = intent.getIntExtra(MyConst.BAR_COLOR, 0)
                        panelColor = intent.getIntExtra(MyConst.PANEL_COLOR, 0)
                        style = intent.getIntExtra(MyConst.STYLE, 0)
                        colorEnabled = intent.getBooleanExtra(MyConst.COLOR_ENABLED, true)
                        setParams()
                        setTheme()
                    }
                }
            }

        // Добавить слушателя обратного вызова onBackPressed.
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun initVisualizer(){
        visualizer!!.init(0)
        visualizer!!.start()
    }

    private fun initButtons() = with(binding) {
        imSetting.setOnClickListener {
            openSetting()
        }

        tvExit.setOnClickListener {
            finish()
        }
    }

    private fun setParams() = with(binding) {
        cdFrame.backgroundTintList = ColorStateList.valueOf(frameColor)
        cdTop.backgroundTintList = ColorStateList.valueOf(winColor)
        main.backgroundTintList = ColorStateList.valueOf(winColor)
        visualizer!!.style = style
        visualizer!!.barColor = barColor
        visualizer!!.groundColor = panelColor
        visualizer!!.colorEnabled = colorEnabled
    }

    private fun openSetting() {
        val intent = Intent(this, SettingActivity::class.java)
        intent.putExtra(MyConst.WIN_COLOR, winColor)
        intent.putExtra(MyConst.FRAME_COLOR, frameColor)
        intent.putExtra(MyConst.BAR_COLOR, barColor)
        intent.putExtra(MyConst.PANEL_COLOR, panelColor)
        intent.putExtra(MyConst.COLOR_ENABLED, colorEnabled)
        intent.putExtra(MyConst.STYLE, style)
        launcher?.launch(intent)
    }

    // Громкость
    private fun initVolume() = with(binding) {
        // Получить аудио менеджер
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        // Установите максимальную громкость
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        slVolume.valueMax = maxVolume.toFloat()
        // Установите текущую громкость
        currVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        slVolume.value = currVolume.toFloat()
        slCurr = slVolume.value // Значение слайдера

        // Изменение значения
        slVolume.setOnChangeListener {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, it.toInt(), 0)
            slCurr = it
            visualizer!!.volume = it
            setText()
        }
    }

    private fun setText() = with(binding) {
        val percent = slCurr * 100 / maxVolume
        val txt = percent.toInt().toString() + " %"
        tvVolume.text = txt
    }

    private fun setTheme() {
        val red = Color.red(winColor)
        val green = Color.green(winColor)
        val blue = Color.blue(winColor)
        val brightness = sqrt(
            0.299 * red * red +
                    0.587 * green * green +
                    0.114 * blue * blue
        )
        if (brightness > 150)
            lightTheme()
        else
            darkTheme()
    }

    private fun lightTheme() = with(binding) {
        tvVolume.setTextColor(Color.BLACK)
        tvExit.setTextColor(Color.BLACK)
        imSetting.setImageResource(R.drawable.ic_setting_black)
        imSetting.setBackgroundResource(R.drawable.rectangle_black)
        tvExit.setBackgroundResource(R.drawable.rectangle_black)
    }

    private fun darkTheme() = with(binding) {
        tvVolume.setTextColor(Color.WHITE)
        tvExit.setTextColor(Color.WHITE)
        imSetting.setImageResource(R.drawable.ic_setting_white)
        imSetting.setBackgroundResource(R.drawable.rectangle_white)
        tvExit.setBackgroundResource(R.drawable.rectangle_white)
    }

    override fun onStop() {
        super.onStop()
        sp.edit {
            putInt(MyConst.WIN_COLOR, winColor)
            putInt(MyConst.FRAME_COLOR, frameColor)
            putInt(MyConst.BAR_COLOR, barColor)
            putInt(MyConst.PANEL_COLOR, panelColor)
            putInt(MyConst.STYLE, style)
            putBoolean(MyConst.COLOR_ENABLED, colorEnabled)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        visualizer?.release()
    }

    // Кнопка Back
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
            finishAffinity() // Закрыть все
        }
    }

}