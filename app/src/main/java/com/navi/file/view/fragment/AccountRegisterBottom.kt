package com.navi.file.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.navi.file.databinding.BottomRegisterBinding
import com.navi.file.helper.ViewModelFactory
import com.navi.file.model.intercommunication.DisplayScreen
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.viewmodel.AccountViewModel
import com.navi.file.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.ResponseBody
import javax.inject.Inject

@AndroidEntryPoint
class AccountRegisterBottom @Inject constructor(): BottomSheetDialogFragment() {
    private var _binding: BottomRegisterBinding? = null
    private val binding: BottomRegisterBinding get() = _binding!!

    // View Model Factory
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    // Account View Model[Or DisplayViewModel]
    private val accountViewModel: AccountViewModel by activityViewModels { viewModelFactory.accountViewModelFactory }

    // Register View Model
    private val registerViewModel: RegisterViewModel by viewModels { viewModelFactory.registerViewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe
        registerViewModel.registerResult.observe(viewLifecycleOwner) {
            when (it.resultType) {
                ResultType.Success -> {
                    accountViewModel.displayLiveData.value = DisplayScreen.Login
                }
                else -> {handleRegisterError(it)}
            }
        }

        // When GOTO Login Clicked -> Change Display to Login.
        binding.gotoLoginFromRegister.setOnClickListener {
            accountViewModel.displayLiveData.value = DisplayScreen.Login
        }

        // When 'GetStarted' Clicked -> Register
        binding.registerButton.setOnClickListener {
            registerViewModel.requestUserRegister(
                email = binding.registerEmail.editText!!.text.toString(),
                password = binding.registerPassword.editText!!.text.toString(),
                name = "random"
            )
        }
    }

    override fun onDestroyView() {
        Log.d(this::class.java.simpleName, "Destroying AccountRegisterBottom!")
        _binding = null
//        registerViewModel.registerResult.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }

    /**
     * Handle Registration Error, likely empty-ing text fields and show us an error.
     *
     * @param result Execution Result of Registration.
     */
    private fun handleRegisterError(result: ExecutionResult<ResponseBody>) {
        emptyFields()
        toastError(result.message)
    }

    /**
     * Wrapper for showing toast messages.
     *
     * @param errorMessage Error Messages to show.
     */
    private fun toastError(errorMessage: String) =
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()

    /**
     * Clear out all input registration-fields.
     *
     */
    private fun emptyFields() {
        binding.registerEmail.editText!!.setText("")
        binding.registerPassword.editText!!.setText("")
    }
}