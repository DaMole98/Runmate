package com.example.runmate.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CloudDBSingleton private constructor() {
    private val firebaseDB: FirebaseDatabase = FirebaseDatabase.getInstance("https://runmate-b7137-default-rtdb.europe-west1.firebasedatabase.app/")
    companion object {
        @Volatile
        private var instance: CloudDBSingleton? = null

        fun getInstance(): CloudDBSingleton =
            instance ?: synchronized(this) { //sunchronized crea la sezione critica
                instance ?: CloudDBSingleton().also {
                    instance = it
                    it.firebaseDB.setPersistenceEnabled(true)}
            }
    }

    fun getDBref(): FirebaseDatabase { return firebaseDB }//.reference }

}




fun checkCurrentUser(mAuth: FirebaseAuth): Boolean {
    val currentUser = mAuth.currentUser
    return if (currentUser != null) {
        currentUser.reload()
        true
    }
    else false
}

