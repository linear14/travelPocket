package com.dongldh.travelpocket.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dongldh.travelpocket.R
import kotlinx.android.synthetic.main.fragment_main.view.*

class IntroFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_intro, container, false)
        return view
    }
}