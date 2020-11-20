package tech.DevAsh.keyOS.Model

class AppUsageInfo(var packageName: String) {
    var appName: String? = null
    var timeInForeground: Long = 0
}

class TimeExhaustApps{
    var startTime :Long=0
    var blockedApps = hashSetOf<String>()
}

class Time{
    var day:Long?=0
    var hour:Long?=0
    var minute:Long?=0
    var seconds:Long?=0

    override fun toString(): String {
        return "Time(day=$day, hour=$hour, minute=$minute, seconds=$seconds)"
    }


    fun isGreaterThan(time: Time):Boolean{
        if(this.hour!!>time.hour!!){
            return true
        }else if(this.hour!!<time.hour!!){
            return false
        }

        return this.minute!! > time.minute!!

    }

    companion object{
        fun fromString(string: String):Time{
            val time = Time()
            time.seconds = 0
            time.hour = string.split(":")[0].toLong()
            time.minute = string.split(":")[1].toLong()
            time.seconds = 0
            return time
        }
        fun convertLongToTime(milliSeconds: Long):Time{
            val SECOND = 1000
            val MINUTE = 60 * SECOND
            val HOUR = 60 * MINUTE
            val DAY = 24 * HOUR
            val time = Time()
            var ms = milliSeconds
            if (ms > DAY) {
                time.day = ms / DAY
                ms %= DAY.toLong()
            }
            if (ms > HOUR) {
                time.hour=ms / HOUR
                ms %= HOUR.toLong()
            }
            if (ms > MINUTE) {
                time.minute = ms / MINUTE
                ms %= MINUTE.toLong()
            }
            if (ms > SECOND) {
                time.seconds = ms / SECOND
                ms %= SECOND.toLong()
            }
            return time
        }
    }
}