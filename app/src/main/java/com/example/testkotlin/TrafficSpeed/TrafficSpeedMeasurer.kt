package com.example.testkotlin.TrafficSpeed

import android.net.TrafficStats
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.os.SystemClock


class TrafficSpeedMeasurer(private var mTrafficType: TrafficType) {

    private var mTrafficSpeedListener: ITrafficSpeedListener? = null
    private var mHandler: SamplingHandler
    private var mLastTimeReading: Long = 0
    private var mPreviousUpStream: Long = -1
    private var mPreviousDownStream: Long = -1

    init {
        val thread = HandlerThread("ParseThread")
        thread.start()
        mHandler = SamplingHandler(thread.looper)
    }

    fun registerListener(iTrafficSpeedListener: ITrafficSpeedListener) {
        mTrafficSpeedListener = iTrafficSpeedListener
    }

    fun removeListener(mStreamSpeedListener: ITrafficSpeedListener) {
        mTrafficSpeedListener = null
    }

    fun startMeasuring() {
        mHandler.startSamplingThread()
        mLastTimeReading = SystemClock.elapsedRealtime()
    }

    fun stopMeasuring() {
        mHandler.stopSamplingThread()
        finalReadTrafficStats()
    }

    private fun readTrafficStats() {
        val newBytesUpStream = (if (mTrafficType == TrafficType.MOBILE) TrafficStats.getMobileTxBytes() else TrafficStats.getTotalTxBytes()) * 1024
        val newBytesDownStream = (if (mTrafficType == TrafficType.MOBILE) TrafficStats.getMobileRxBytes() else TrafficStats.getTotalRxBytes()) * 1024

        val byteDiffUpStream = newBytesUpStream - mPreviousUpStream
        val byteDiffDownStream = newBytesDownStream - mPreviousDownStream

        synchronized(this) {
            val currentTime = SystemClock.elapsedRealtime()
            var bandwidthUpStream = 0.0
            var bandwidthDownStream = 0.0

            if (mPreviousUpStream >= 0) {
                bandwidthUpStream = (byteDiffUpStream.toDouble()) / (currentTime - mLastTimeReading)
            }
            if (mPreviousDownStream >= 0) {
                bandwidthDownStream = (byteDiffDownStream.toDouble()) / (currentTime - mLastTimeReading)
            }
            mTrafficSpeedListener?.onTrafficSpeedMeasured(bandwidthUpStream, bandwidthDownStream)

            mLastTimeReading = currentTime
        }

        mPreviousDownStream = newBytesDownStream
        mPreviousUpStream = newBytesUpStream
    }

    private fun finalReadTrafficStats() {
        readTrafficStats()
        mPreviousUpStream = -1
        mPreviousDownStream = -1
    }



    private inner class SamplingHandler(looper: Looper) : Handler(looper) {


        val SAMPLE_TIME: Long = 1000
        val MSG_START: Int = 1


        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_START -> {
                    readTrafficStats()
                    sendEmptyMessageDelayed(MSG_START, SAMPLE_TIME)
                }
                else -> throw IllegalArgumentException("Unknown what=${msg.what}")
            }
        }

        fun startSamplingThread() {
            sendEmptyMessage(MSG_START)
        }

        fun stopSamplingThread() {
            removeMessages(MSG_START)
        }
    }

    enum class TrafficType {
        MOBILE,
        ALL
    }
}