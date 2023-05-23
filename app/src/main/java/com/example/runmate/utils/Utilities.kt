package com.example.runmate.utils

import com.google.firebase.auth.FirebaseAuth


fun checkCurrentUser(mAuth: FirebaseAuth): Boolean {
    val currentUser = mAuth.currentUser
    return if (currentUser != null) {
        currentUser.reload()
        true
    }
    else false
}

