package com.navi.file.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.navi.file.model.intercommunication.DisplayScreen

class AccountViewModel : ViewModel() {
    val displayLiveData: MutableLiveData<DisplayScreen> = MutableLiveData()
}