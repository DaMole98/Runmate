package com.example.runmate.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.auth.FirebaseUser


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



//fun isOnline(context: Context): Boolean {
//    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//    val network = connectivityManager.activeNetwork
//    val capabilities = connectivityManager.getNetworkCapabilities(network)
//    return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
//}


fun checkCurrentUser(mAuth: FirebaseAuth): Boolean {
    val currentUser = mAuth.currentUser
    return if (currentUser != null) {
        currentUser.reload()
        true
    }
    else false
}


//fun getUser() : FirebaseUser? {
//
//    val firebaseAuth = FirebaseAuth.getInstance()
//    val currentUser = firebaseAuth.currentUser
//
//}
//val firebaseAuth = FirebaseAuth.getInstance()
//val currentUser = firebaseAuth.currentUser
//
//if (currentUser != null) {
//    // Utente autenticato
//    val isFirstLoginOnDevice = currentUser.metadata.creationTimestamp == currentUser.metadata.lastSignInTimestamp
//
//    if (isFirstLoginOnDevice) {
//        // È il primo accesso dell'utente su questo dispositivo
//        // Scarica i dati dal cloud per la sincronizzazione con il database
//    } else {
//        // Utente già ha effettuato l'accesso su questo dispositivo in precedenza
//    }
//} else {
//    // Utente non autenticato
//}

