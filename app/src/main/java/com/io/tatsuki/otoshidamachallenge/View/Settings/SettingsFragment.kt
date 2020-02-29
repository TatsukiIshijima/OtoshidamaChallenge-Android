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
import kotlinx.android.synthetic.main.lottery_number_edittext_view.view.*
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
            removeWildcards()
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
        secondClassNumber.setText(
            if (lotteryNumbers.secondClass.isNotEmpty()) "**${lotteryNumbers.secondClass}"
            else ""
        )
        thirdClassNumberPrimary.setText(
            if (lotteryNumbers.thirdClassNumberPrimary.isNotEmpty()) "****${lotteryNumbers.thirdClassNumberPrimary}"
            else ""
        )
        thirdClassNumberSecondary.setText(
            if (lotteryNumbers.thirdClassNumberSecondary.isNotEmpty()) "****${lotteryNumbers.thirdClassNumberSecondary}"
            else ""
        )
        thirdClassNumberTertiary.setText(
            if (lotteryNumbers.thirdClassNumberTertiary.isNotEmpty()) "****${lotteryNumbers.thirdClassNumberTertiary}"
            else ""
        )
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
                 if (lotteryNumbers.specialQuinaryForward.isNotEmpty()) "***${lotteryNumbers.specialQuinaryForward}"
                 else "",
                lotteryNumbers.specialQuinaryBackward
            )
    }

    private fun removeWildcards() {
        secondClassNumber.apply {
            setText(viewModel.removeWildCard(text.toString()))
        }
        thirdClassNumberPrimary.apply {
            setText(viewModel.removeWildCard(text.toString()))
        }
        thirdClassNumberSecondary.apply {
            setText(viewModel.removeWildCard(text.toString()))
        }
        thirdClassNumberTertiary.apply {
            setText(viewModel.removeWildCard(text.toString()))
        }
        specialLotteryQuinaryNumberEditText.bSetNumber.apply {
            setText(viewModel.removeWildCard(text.toString()))
        }
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
                val messageResource = validate()
                if (messageResource != 0) {
                    showValidateErrorDialog(messageResource)
                } else {
                    saveLotteryNumbers()
                    updateBottomButtonLayout(false)
                    setEditMode(false)
                    viewModel.getLotteryNumbers()
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .show()
    }

    private fun validate(): Int {
        if (!viewModel.validateLength(firstClassNumber.text.toString(), 6)) {
            return R.string.first_class_length_short
        }
        if (!viewModel.validateLength(viewModel.removeWildCard(secondClassNumber.text.toString()), 4)) {
            return R.string.second_class_length_short
        }
        if ((!viewModel.validateLength(viewModel.removeWildCard(thirdClassNumberPrimary.text.toString()), 2)) ||
            (!viewModel.validateLength(viewModel.removeWildCard(thirdClassNumberSecondary.text.toString()), 2)) ||
            (!viewModel.validateLength(viewModel.removeWildCard(thirdClassNumberTertiary.text.toString()), 2))) {
            return R.string.third_class_length_short
        }
        if ((!viewModel.validateLength(specialLotteryPrimaryNumberEditText.bSetNumber.text.toString(), 4)) ||
            (!viewModel.validateLength(specialLotterySecondaryNumberEditText.bSetNumber.text.toString(), 4)) ||
            (!viewModel.validateLength(specialLotteryTertiaryNumberEditText.bSetNumber.text.toString(), 4)) ||
            (!viewModel.validateLength(specialLotteryQuaternaryNumberEditText.bSetNumber.text.toString(), 4)) ||
            (!viewModel.validateLength(viewModel.removeWildCard(specialLotteryQuinaryNumberEditText.bSetNumber.text.toString()), 1))) {
            return R.string.special_set_length_short
        }
        if ((!viewModel.validateLength(specialLotteryPrimaryNumberEditText.classNumber.text.toString(), 6)) ||
            (!viewModel.validateLength(specialLotterySecondaryNumberEditText.classNumber.text.toString(), 6)) ||
            (!viewModel.validateLength(specialLotteryTertiaryNumberEditText.classNumber.text.toString(), 6)) ||
            (!viewModel.validateLength(specialLotteryQuaternaryNumberEditText.classNumber.text.toString(), 6)) ||
            (!viewModel.validateLength(specialLotteryQuinaryNumberEditText.classNumber.text.toString(), 6))) {
            return R.string.special_class_length_short
        }
        return 0
    }

    private fun showValidateErrorDialog(messageResource: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setTitle(R.string.input_class_length_error_title)
            .setMessage(messageResource)
            .setPositiveButton(R.string.alert_dialog_positive_button_title) { _, _ -> }
            .show()
    }

    private fun saveLotteryNumbers() {
        viewModel.saveLotteryNumbers(
            LotteryNumbers(
                firstClass = firstClassNumber.text.toString(),
                secondClass = secondClassNumber.text.toString(),
                thirdClassNumberPrimary = thirdClassNumberPrimary.text.toString(),
                thirdClassNumberSecondary = thirdClassNumberSecondary.text.toString(),
                thirdClassNumberTertiary = thirdClassNumberTertiary.text.toString(),
                specialPrimaryForward = specialLotteryPrimaryNumberEditText.bSetNumber.text.toString(),
                specialPrimaryBackward = specialLotteryPrimaryNumberEditText.classNumber.text.toString(),
                specialSecondaryForward = specialLotterySecondaryNumberEditText.bSetNumber.text.toString(),
                specialSecondaryBackward = specialLotterySecondaryNumberEditText.classNumber.text.toString(),
                specialTertiaryForward = specialLotteryTertiaryNumberEditText.bSetNumber.text.toString(),
                specialTertiaryBackward = specialLotteryTertiaryNumberEditText.classNumber.text.toString(),
                specialQuaternaryForward = specialLotteryQuaternaryNumberEditText.bSetNumber.text.toString(),
                specialQuaternaryBackward = specialLotteryQuaternaryNumberEditText.classNumber.text.toString(),
                specialQuinaryForward = specialLotteryQuinaryNumberEditText.bSetNumber.text.toString(),
                specialQuinaryBackward = specialLotteryQuinaryNumberEditText.classNumber.text.toString()
            )
        )
    }
}
