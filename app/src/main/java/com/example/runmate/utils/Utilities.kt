package com.example.runmate.utils

import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
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

fun setUserProperties(context: Context, analytics: FirebaseAnalytics){

    val uid = FirebaseAuth.getInstance().currentUser!!.uid
    val tPref = context.getSharedPreferences("${uid}UserPrefs", Context.MODE_PRIVATE)
    val weight = tPref.getInt("Weight", 0)
    val height = tPref.getInt("Height", 0)
    val gender = tPref.getString("Gender", "Male")


    analytics.setUserProperty("Weight", weight.toString())
    analytics.setUserProperty("Height", height.toString())
    analytics.setUserProperty("Gender", gender.toString())


}

/*fun isFirstLogin(): Boolean {
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser
    val isfirst = (currentUser?.metadata?.creationTimestamp == currentUser?.metadata?.lastSignInTimestamp)
    return isfirst
} */


/*fun syncDB(sPref : SharedPreferences, uid : String){
    val DB = CloudDBSingleton.getInstance()

    lateinit var email : String;
    lateinit var username : String;

    val userRef = DB.getDBref().getReference("users/${uid}")
    userRef.child("email").get().addOnSuccessListener { dataSnapshot ->
        email = dataSnapshot.getValue(String::class.java).toString()
    }

    userRef.child("username").get().addOnSuccessListener { dataSnapshot ->
        username = dataSnapshot.getValue(String::class.java).toString()
    }

    userRef.child("profile/calories").get().addOnSuccessListener { dataSnapshot ->
        email = dataSnapshot.getValue(String::class.java).toString()
    }

    userRef.child("profile/height").get().addOnSuccessListener { dataSnapshot ->
        email = dataSnapshot.getValue(String::class.java).toString()
    }

    userRef.child("profile/calories").get().addOnSuccessListener { dataSnapshot ->
        email = dataSnapshot.getValue(String::class.java).toString()
    }

    userRef.child("profile/meters").get().addOnSuccessListener { dataSnapshot ->
        email = dataSnapshot.getValue(String::class.java).toString()
    }

    userRef.child("profile/steps").get().addOnSuccessListener { dataSnapshot ->
        email = dataSnapshot.getValue(String::class.java).toString()
    }

    userRef.child("profile/weight").get().addOnSuccessListener { dataSnapshot ->
        email = dataSnapshot.getValue(String::class.java).toString()
    }

    val trainingRef = userRef.child("traininglist")

    val sevenDaysAgo = Calendar.getInstance()
    sevenDaysAgo.add(Calendar.DAY_OF_YEAR, -7)
    //val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(sevenDaysAgo.time)

    val query = trainingRef.orderByChild("data").startAt(date)

    query.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            for (childSnapshot in dataSnapshot.children) {
                val trainingData = childSnapshot.getValue(TrainingObject::class.java)

            }

            // Rimuovi il listener se necessario
            query.removeEventListener(this)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Gestisci gli errori
        }
    })


    val editor = sPref.edit()
    editor.putString("username", username)
    editor.putString("email", email)
    editor.putString("uid", uid)
    editor.apply()





} */