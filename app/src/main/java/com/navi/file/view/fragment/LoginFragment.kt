package com.navi.file.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.navi.file.databinding.FragmentLoginBinding
import com.navi.file.model.UserLoginRequest
import com.navi.file.model.UserLoginResponse
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.viewmodel.UserViewModel

class LoginFragment(viewModelFactory: ViewModelProvider.Factory? = null): Fragment() {
    // Custom Injection
    private val userViewModel: UserViewModel by viewModels {
        viewModelFactory ?: ViewModelProvider.NewInstanceFactory()
    }

    // Binding
    private var _loginFragmentBinding: FragmentLoginBinding? = null
    private val loginFragmentBinding get() = _loginFragmentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _loginFragmentBinding = FragmentLoginBinding.inflate(inflater, container, false)
        return loginFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe()

        with(loginFragmentBinding) {
            // When Login Button Clicked
            loginButton.setOnClickListener {
                val userEmail = loginEmailInput.editText!!.text.toString()
                val userPassword = loginPasswordInput.editText!!.text.toString()
                userViewModel.requestUserLogin(UserLoginRequest(userEmail, userPassword))
            }
        }
    }

    private fun observe() {
        userViewModel.loginUser.observe(viewLifecycleOwner) {
            when (it.resultType) {
                ResultType.Success -> {}
                else -> {handleLoginError(it)}
            }
        }
    }

    /**
     * Handle Login Error, likely empty-ing text fields and show us an error.
     *
     * @param result Execution Result of Registration.
     */
    private fun handleLoginError(result: ExecutionResult<UserLoginResponse>) {
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
        with (loginFragmentBinding) {
            loginEmailInput.editText!!.setText("")
            loginPasswordInput.editText!!.setText("")
        }
    }
}