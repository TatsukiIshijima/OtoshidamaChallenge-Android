package com.io.tatsuki.otoshidamachallenge.View.Settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.io.tatsuki.otoshidamachallenge.DI.AppContainer
import com.io.tatsuki.otoshidamachallenge.DI.SettingsContainer
import com.io.tatsuki.otoshidamachallenge.Model.LotteryNumbers
import com.io.tatsuki.otoshidamachallenge.OtoshidamaChallengeApplication
import com.io.tatsuki.otoshidamachallenge.R
import kotlinx.android.synthetic.main.settings_fragment.*


class SettingsFragment : Fragment() {

    companion object {
        private val TAG = SettingsFragment::class.java.simpleName
    }

    private lateinit var appContainer: AppContainer
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appContainer = (requireActivity().application as OtoshidamaChallengeApplication).appContainer
        appContainer.settingsContainer = SettingsContainer(appContainer.lotteryNumbersRepository)
        val settingsContainer =
            appContainer.settingsContainer ?: throw NullPointerException("SettingsContainer is null.")
        viewModel = settingsContainer.settingsViewModelFactory.create()

        viewModel.lotteryNumbersEvent.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                Log.d(TAG, it.firstClass)
            }
        })

        editButton.setOnClickListener {
            updateBottomButtonLayout(true)
            setEditMode(true)
        }

        saveButton.setOnClickListener {
            showSaveConfirmDialog()
        }

        cancelButton.setOnClickListener {
            updateBottomButtonLayout(false)
            setEditMode(false)
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.getLotteryNumbers()
    }

    override fun onDestroyView() {
        appContainer.settingsContainer = null
        super.onDestroyView()
    }

    private fun setEditMode(isEnable: Boolean) {
        firstClassNumber.isEnabled = isEnable
        secondClassNumber.isEnabled = isEnable
        thirdClassNumberPrimary.isEnabled = isEnable
        thirdClassNumberSecondary.isEnabled = isEnable
        thirdClassNumberTertiary.isEnabled = isEnable
        specialLotteryPrimaryNumberEditText.setEnableEdit(isEnable)
        specialLotterySecondaryNumberEditText.setEnableEdit(isEnable)
        specialLotteryTertiaryNumberEditText.setEnableEdit(isEnable)
        specialLotteryQuaternaryNumberEditText.setEnableEdit(isEnable)
        specialLotteryQuinaryNumberEditText.setEnableEdit(isEnable)
    }

    private fun updateBottomButtonLayout(isEditMode: Boolean) {
        editButton.isVisible = !isEditMode
        saveAndCancelButtonLayout.isVisible = isEditMode
    }

    private fun showSaveConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setTitle(R.string.save_confirm_dialog_title)
            .setMessage(R.string.save_confirm_dialog_message)
            .setPositiveButton(R.string.alert_dialog_positive_button_title) { _, _ ->
                // TODO:EditTextの値取得
                viewModel.saveLotteryNumbers(LotteryNumbers())
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .show()
    }
}
