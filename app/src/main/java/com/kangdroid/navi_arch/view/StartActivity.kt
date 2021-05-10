package com.kangdroid.navi_arch.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.databinding.ActivityStartBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartActivity : AppCompatActivity(), FragmentCallBack {
    // View binding
    private val startBinding: ActivityStartBinding by lazy {
        ActivityStartBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(startBinding.root)

        replaceFragment(LoginFragment())
    }

    // If login success, switch to MainActivity
    override fun switchActivity(){
        val intent: Intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    // for fragment by fragment transaction
    // default: addToBackStack
    override fun replaceFragment(fragment: Fragment){
        val transaction: FragmentTransaction =
            supportFragmentManager.beginTransaction().replace(R.id.startActivityContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    // for fragment by fragment transaction
    override fun removeFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().remove(fragment).commit()
        supportFragmentManager.popBackStack()
    }

}

/*
 * [interface] FragmentCallBack
 * Manage fragment-Activity-fragment switching
 */
interface FragmentCallBack {
    fun replaceFragment(fragment: Fragment)
    fun removeFragment(fragment: Fragment)
    fun switchActivity()
}