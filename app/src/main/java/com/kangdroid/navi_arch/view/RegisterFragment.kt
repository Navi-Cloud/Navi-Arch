package com.kangdroid.navi_arch.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.databinding.FragmentRegisterBinding
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment @Inject constructor() : Fragment() {
    // Log Tag
    private val logTag: String = this::class.java.simpleName

    // View Binding
    var registerBinding: FragmentRegisterBinding? = null

    // View Model for Login/Register
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        registerBinding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        return registerBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()

        registerBinding!!.apply {
            //TODO 아이디 중복 체크
            button.setOnClickListener {
                //id = this.TextId.text.toString()
            }

            button2.setOnClickListener {
                // Check all args are OK
                val isRegisterArgsAllOk = checkRegisterArgs()

                // Then register!
                if(isRegisterArgsAllOk) {
                    userViewModel.register(
                        userName = this.Name.text.toString(),
                        userId = this.TextId.text.toString(),
                        userEmail = this.Email.text.toString(),
                        userPassword = this.Textpassword.text.toString()
                    )
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback {
            userViewModel.requestLoginPage()
        }
    }

    // Check all args OK
    // [return value] true: success, false: fail
    private fun checkRegisterArgs(): Boolean {
        registerBinding!!.apply {
            // First, check Privacy Policy is checked
            if(!this.checkbox.isChecked) {
                Toast.makeText(context, "약관 동의를 하지 않았습니다.", Toast.LENGTH_SHORT).show()
                return false
            }

            val userPassword: String = this.Textpassword.text.toString()
            val userPasswordForCheck: String = this.passwordRe.text.toString()
            val userEmail: String = this.Email.text.toString()
            val userId: String = this.TextId.text.toString()
            val userName: String = this.Name.text.toString()

            // step 1) Check all args are filled
            if(userId=="" || userName=="" || userEmail=="" || userPassword=="" || userPasswordForCheck==""){
                Toast.makeText(context,"양식을 모두 채우지 않았습니다.",Toast.LENGTH_SHORT).show()
                return false
            }

            // step 2) email check
            // (for now, check if email have '@')
            if(userEmail.contains('@')){
                this.textInputLayout.error = null
            }else{
                this.textInputLayout.error = "이메일 형식이 올바르지 않습니다."
                return false
            }

            // step 3) password check
            if(userPassword != userPasswordForCheck) {
                this.textInputLayout4.error = "비밀번호가 서로 맞지 않습니다."
                return false
            }
        }
        return true
    }

    private fun initObserver() {
        userViewModel.registerErrorData.observe(viewLifecycleOwner) {
            if (it != null) {
                // If login success, make Toast Message and clear livErrorData
                Log.e(logTag, "Error Message Observed")
                Log.e(logTag, it.stackTraceToString())
                Toast.makeText(context, "Register Error: ${it.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userViewModel.clearRegisterErrorData()
        registerBinding = null
    }
}