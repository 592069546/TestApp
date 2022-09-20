package com.example.pagingtest.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pagingtest.R

class NavStartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_nav_start, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.tv_to_first).setOnClickListener {
            findNavController().navigate(R.id.activity_to_first)
        }

        view.findViewById<View>(R.id.tv_to_second).setOnClickListener {
            findNavController().navigate(R.id.activity_to_second)
        }
    }
}