package com.kangdroid.navi_arch.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.databinding.FragmentRegisterBinding
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    // Log Tag
    private val logTag: String = this::class.java.simpleName

    // View Binding
    var registerBinding: FragmentRegisterBinding? = null

    // View Model for Login/Register
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        registerBinding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        return registerBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerBinding!!.apply {
            button2.isEnabled = false

            checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked) this.button2.isEnabled = true
            }

            //TODO 아이디 중복 체크
            button.setOnClickListener {
                //id = this.TextId.text.toString()
            }

            button2.setOnClickListener {
                val password: String = this.Textpassword.text.toString()
                val passwordForCheck: String = this.passwordRe.text.toString()

                // 이메일 @ 체크
                if(this.Email.toString().contains('@')){
                    this.textInputLayout.error = null
                }else{
                    this.textInputLayout.error = "이메일 형식이 올바르지 않습니다."
                }

                //재입력한 비밀번호 == 비밀번호 확인
                if(password == passwordForCheck) {
                    this.button2.isEnabled = true
                }else{
                    this.textInputLayout4.error = "비밀번호가 서로 맞지 않습니다."
                }

                userViewModel.register(
                    userName = this.Name.text.toString(),
                    userId = this.TextId.text.toString(),
                    userEmail = this.Email.text.toString(),
                    userPassword = password
                )

                // After register, finish this fragment
                val parentActivity = activity as StartActivity
                parentActivity.removeFragment(this@RegisterFragment)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        registerBinding = null
    }
}