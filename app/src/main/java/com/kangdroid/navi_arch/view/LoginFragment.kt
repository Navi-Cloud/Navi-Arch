package com.kangdroid.navi_arch.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.databinding.FragmentLoginBinding
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    // Log Tag
    private val logTag: String = this::class.java.simpleName

    // View Binding
    var loginBinding: FragmentLoginBinding? = null

    // View Model for Login/Register
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loginBinding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return loginBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginBinding!!.button.setOnClickListener {
            val userId: String = loginBinding!!.idLogin.text.toString()
            val userPassword: String = loginBinding!!.pwLogin.text.toString()

            Log.i(logTag, "$userId+$userPassword")

            val userToken = userViewModel.login(
                userId = userId,
                userPassword = userPassword
            )
            // TODO -> MainActivity
            Log.i(logTag, "userToken: $userToken")
        }

        loginBinding!!.textView2.setOnClickListener {
            // fragment translation to Register Fragment
            val parentActivity = activity as StartActivity
            parentActivity.replaceFragment(RegisterFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loginBinding = null
    }
}