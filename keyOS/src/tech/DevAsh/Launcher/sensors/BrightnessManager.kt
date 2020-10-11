/*
 *     This file is part of Kiosk Launcher.
 *
 *     Kiosk Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Kiosk Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Kiosk Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.DevAsh.Launcher.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import tech.DevAsh.Launcher.ensureOnMainThread
import tech.DevAsh.Launcher.useApplicationContext
import tech.DevAsh.Launcher.util.SingletonHolder

class BrightnessManager private constructor(private val context: Context): SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val listeners = mutableSetOf<OnBrightnessChangeListener>()
    private val shouldListen get() = listeners.isNotEmpty()
    private var isListening = false

    fun startListening() {
        if (shouldListen && !isListening) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        val illuminance = event.values[0]
        for (listener in listeners) {
            listener.onBrightnessChanged(illuminance)
        }
    }

    fun stopListening() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
        }
    }

    fun addListener(listener: OnBrightnessChangeListener) {
        listeners.add(listener)
        startListening()
    }

    fun removeListener(listener: OnBrightnessChangeListener) {
        listeners.remove(listener)
        if (!shouldListen) {
            stopListening()
        }
    }

    companion object : SingletonHolder<BrightnessManager, Context>(ensureOnMainThread(useApplicationContext(::BrightnessManager)))

    interface OnBrightnessChangeListener {
        fun onBrightnessChanged(illuminance: Float)
    }
}