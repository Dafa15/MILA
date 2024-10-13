package com.example.mila.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mila.constant.Constant
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {

    private val _profileImage = MutableLiveData<Bitmap>()
    val profileImage: LiveData<Bitmap> get() = _profileImage
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    // Fungsi untuk memperbarui gambar
    fun setProfileImage(bitmap: Bitmap) {
        if (_profileImage.value == null || !_profileImage.value!!.sameAs(bitmap)) {
            _profileImage.value = bitmap
        }
    }
    fun setUserName(string: String) {
        if (_userName.value == null || _userName.value != string) {
            _userName.value = string
        }
    }

}