package com.example.testkotlin.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.testkotlin.R
import com.example.testkotlin.databinding.FragmentHeighborsBinding
import com.example.testkotlin.databinding.FragmentHomeBinding

class NeighborsFragment : Fragment() {
    private lateinit var binding: FragmentHeighborsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHeighborsBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = NeighborsFragment()
    }
}

