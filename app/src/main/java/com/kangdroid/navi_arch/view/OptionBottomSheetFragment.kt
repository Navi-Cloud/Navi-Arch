package com.kangdroid.navi_arch.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kangdroid.navi_arch.databinding.DialogOptionBoxBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@AndroidEntryPoint
class OptionBottomSheetFragment @Inject constructor() : BottomSheetDialogFragment() {
    private val logTag: String = this::class.java.simpleName
    private var _dialogOptionBoxBinding: DialogOptionBoxBinding? = null
    private val dialogOptionBoxBinding: DialogOptionBoxBinding get() = _dialogOptionBoxBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _dialogOptionBoxBinding = DialogOptionBoxBinding.inflate(inflater, container, false)
        return dialogOptionBoxBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialogOptionBoxBinding.apply {
            deleteRecentLayout.setOnClickListener {

            }
            storageAnalysisLayout.setOnClickListener {

            }
            recycleBinLayout.setOnClickListener {

            }
            settingLayout.setOnClickListener {

            }
        }

    }

    override fun onDestroyView() {
        _dialogOptionBoxBinding = null
        super.onDestroyView()
    }
}