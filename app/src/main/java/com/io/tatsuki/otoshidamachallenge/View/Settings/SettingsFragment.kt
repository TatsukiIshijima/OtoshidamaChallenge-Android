package com.io.tatsuki.otoshidamachallenge.View.Settings

import android.annotation.SuppressLint
import android.os.Bundle
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

        viewModel.lotteryNumbersEvent.observe(
            viewLifecycleOwner,
            Observer {
                setLotteryNumbers(it)
            }
        )

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
            viewModel.getLotteryNumbers()
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

    @SuppressLint("SetTextI18n")
    private fun setLotteryNumbers(lotteryNumbers: LotteryNumbers) {
        firstClassNumber.setText(lotteryNumbers.firstClass)
        secondClassNumber.setText("**${lotteryNumbers.secondClass}")
        thirdClassNumberPrimary.setText("****${lotteryNumbers.thirdClassNumberPrimary}")
        thirdClassNumberSecondary.setText("****${lotteryNumbers.thirdClassNumberSecondary}")
        thirdClassNumberTertiary.setText("****${lotteryNumbers.thirdClassNumberTertiary}")
        specialLotteryPrimaryNumberEditText
            .setLotteryNumber(
                lotteryNumbers.specialPrimaryForward,
                lotteryNumbers.specialPrimaryBackward
            )
        specialLotterySecondaryNumberEditText
            .setLotteryNumber(
                lotteryNumbers.specialSecondaryForward,
                lotteryNumbers.specialSecondaryBackward
            )
        specialLotteryTertiaryNumberEditText
            .setLotteryNumber(
                lotteryNumbers.specialTertiaryForward,
                lotteryNumbers.specialTertiaryBackward
            )
        specialLotteryQuaternaryNumberEditText
            .setLotteryNumber(
                lotteryNumbers.specialQuaternaryForward,
                lotteryNumbers.specialQuaternaryBackward
            )
        specialLotteryQuinaryNumberEditText
            .setLotteryNumber(
                "***${lotteryNumbers.specialQuinaryForward}",
                lotteryNumbers.specialQuinaryBackward
            )
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
            .setNegativeButton(R.string.cancel) { _, _ ->

            }
            .show()
    }
}
