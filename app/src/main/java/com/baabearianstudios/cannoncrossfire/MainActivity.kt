package com.baabearianstudios.cannoncrossfire

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.baabearianstudios.cannoncrossfire.ui.CannonCrossfireApp
import com.baabearianstudios.cannoncrossfire.ui.theme.CannonCrossfireTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    private val viewModel: GameViewModel by viewModels()
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Keep screen awake during active tilt arcade gameplay
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        sensorManager = getSystemService(SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            CannonCrossfireTheme {
                CannonCrossfireApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResumed()
        accelerometer?.also { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
        viewModel.onAppPaused()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            viewModel.onAccelerometerChanged(event.values[0], event.values[1], event.values[2])
        }
    }
}
