package com.kangdroid.navi_arch.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    // For fragment switching
    var parentActivity: FragmentCallBack? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // init parent activity
        // for now, activity is ALWAYS FragmentCallBack
        if(activity is FragmentCallBack) parentActivity = activity as FragmentCallBack

        loginBinding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return loginBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBinding()
    }

    private fun initBinding() {
        // Login Button
        loginBinding!!.button.setOnClickListener {
            val userId: String = loginBinding!!.idLogin.text.toString()
            val userPassword: String = loginBinding!!.pwLogin.text.toString()

            Log.i(logTag, "$userId , $userPassword")

            userViewModel.login(
                userId = userId,
                userPassword = userPassword
            ) {
                if(userViewModel.liveErrorData.value == null){
                    // After login success, go MainActivity
                    parentActivity!!.switchActivity()
                } else {
                    // If login success, make Toast Message and clear livErrorData
                    val throwable: Throwable = userViewModel.liveErrorData.value!!
                    Log.e(logTag, "Error Message Observed")
                    Log.e(logTag, throwable.stackTraceToString())
                    Toast.makeText(context, "Login Error: ${throwable.message}", Toast.LENGTH_LONG).show()

                    userViewModel.liveErrorData.value = null

                    // Clear edit text
                    loginBinding!!.idLogin.setText("")
                    loginBinding!!.pwLogin.setText("")
                }
            }
        }

        // Join [Register] Button
        loginBinding!!.textView2.setOnClickListener {
            // fragment translation to Register Fragment
            parentActivity!!.replaceFragment(RegisterFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loginBinding = null
        parentActivity = null
    }
}