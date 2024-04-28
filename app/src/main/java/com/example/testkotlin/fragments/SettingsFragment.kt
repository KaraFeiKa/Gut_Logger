package com.example.testkotlin.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SeekBar
import com.example.testkotlin.R
import com.example.testkotlin.databinding.FragmentHomeBinding
import com.example.testkotlin.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        seekBar()

        return binding.root
    }

    private fun seekBar() = with(binding) {
        seekBar3.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView6.text = "Точность измерений: $progress (м)"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Начало перемещения ползунка
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Окончание перемещения ползунка
                seekBar?.let { (it.progress) }
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingsFragment()

    }
}

