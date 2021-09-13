package com.navi.file.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.navi.file.databinding.BottomMainBinding
import com.navi.file.helper.ViewModelFactory
import com.navi.file.model.intercommunication.DisplayScreen
import com.navi.file.viewmodel.AccountViewModel
import com.navi.file.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccountMainBottom @Inject constructor(): BottomSheetDialogFragment() {
    private var _binding: BottomMainBinding? = null
    private val binding: BottomMainBinding get() = _binding!!

    // View Model Factory
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    // Account View Model[Or DisplayViewModel]
    private val accountViewModel: AccountViewModel by activityViewModels { viewModelFactory.accountViewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainSignInButton.setOnClickListener {
            accountViewModel.displayLiveData.value = DisplayScreen.Login
        }

        binding.mainRegisterButton.setOnClickListener {
            accountViewModel.displayLiveData.value = DisplayScreen.Register
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}