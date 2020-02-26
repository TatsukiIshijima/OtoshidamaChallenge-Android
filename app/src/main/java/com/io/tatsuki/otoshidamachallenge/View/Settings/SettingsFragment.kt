package com.io.tatsuki.otoshidamachallenge.View.Settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.io.tatsuki.otoshidamachallenge.R


class SettingsFragment : Fragment() {

    companion object {
        private val TAG = SettingsFragment::class.java.simpleName
    }

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.lotteryNumbersEvent.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                Log.d(TAG, it.firstClass)
            }
        })
    }

    override fun onStart() {
        super.onStart()

        viewModel.getLotteryNumbers()
    }
}
