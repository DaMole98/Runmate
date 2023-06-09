package com.example.runmate

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class UserFragment : Fragment(R.layout.fragment_user), ChangeUNDialogFragment.OnUsernameChangedListener {

    private lateinit var logoutBtn: Button
    private lateinit var usernameBtn: Button
    private lateinit var targetBtn: Button
    private lateinit var deleteAccountBtn: Button
    private val currentUser = Firebase.auth.currentUser
    private val uid = currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        val user_view = view.findViewById<TextView>(R.id.user_view)
        logoutBtn = view.findViewById<Button>(R.id.btn_logout)
        usernameBtn = view.findViewById<Button>(R.id.btn_username)
        targetBtn = view.findViewById<Button>(R.id.btn_target)


        val sPref = requireContext().getSharedPreferences("${uid}UserPrefs", Context.MODE_PRIVATE)
        val username = sPref.getString("username", "")
        user_view.text = "Ciao $username"

        deleteAccountBtn = view.findViewById<Button>(R.id.btn_delete_account)
        deleteAccountBtn.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        targetBtn.setOnClickListener {
            val intent = Intent(activity, TargetActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        usernameBtn.setOnClickListener {
            val dialog = ChangeUNDialogFragment()
            dialog.setOnUsernameChangedListener(this) //il listener diventa questo fragment
            dialog.show(requireFragmentManager(), "change username dialog")
        }

        logoutBtn.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(activity, Login::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return view
    }

    override fun onUsernameChanged(newUsername: String) {
        val userView = view?.findViewById<TextView>(R.id.user_view)
        userView?.text = "Ciao $newUsername"
    }

    private fun showDeleteConfirmationDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Conferma cancellazione account")
        dialogBuilder.setMessage("Sei sicuro di voler cancellare il tuo account?")
        dialogBuilder.setPositiveButton("Conferma") { dialog, which ->
            reauthenticateUser()
        }
        dialogBuilder.setNegativeButton("Annulla", null)
        dialogBuilder.show()
    }

    private fun reauthenticateUser() {

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Reinserisci le credenziali")
        dialogBuilder.setMessage("Per cancellare l'account, reinserisci la tua password.")

        val rootView = requireActivity().layoutInflater.inflate(R.layout.delete_dialog, null)
        val passwordEditText = rootView.findViewById<EditText>(R.id.passwordEditText)
        dialogBuilder.setView(rootView)

        dialogBuilder.setPositiveButton("Conferma") { dialog, which ->
            val password = passwordEditText.text.toString()
            val credentials = EmailAuthProvider.getCredential(currentUser?.email!!, password)


            currentUser?.reauthenticate(credentials)
                ?.addOnSuccessListener {
                    deleteAccount()
                }
                ?.addOnFailureListener { exception ->
                    Log.e(TAG, "Errore durante la riautenticazione dell'utente", exception)
                    Toast.makeText(requireContext(), "Password errata", Toast.LENGTH_LONG).show()
                }
        }

        dialogBuilder.setNegativeButton("Annulla", null)
        dialogBuilder.show()
    }

    private fun deleteAccount() {
        currentUser?.delete()
        currentUser?.delete()
            ?.addOnSuccessListener {
                deleteUserDataFromDatabase(currentUser?.uid)
                navigateToLogin()
            }
            ?.addOnFailureListener { exception ->
                Log.e(TAG, "Errore durante la cancellazione dell'account", exception)
            }
    }

    private fun deleteUserDataFromDatabase(userId: String?) {
//
        // delete local data
        val sPref = requireContext().getSharedPreferences("${uid}UserPrefs", Context.MODE_PRIVATE)
        sPref.edit().apply(){
            clear()
            apply()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(activity, Login::class.java)
        startActivity(intent)
        activity?.finish()
    }
}



class ChangeUNDialogFragment : DialogFragment() {

    interface OnUsernameChangedListener { //interfaccia listener per il cambio username
        fun onUsernameChanged(newUsername: String)
    }

    private var usernameChangedListener: OnUsernameChangedListener? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Nuovo nome")

        val rootView = requireActivity().layoutInflater.inflate(R.layout.username_dialog, null)
        val usernameEditText = rootView.findViewById<EditText>(R.id.editText)
        builder.setView(rootView)

        builder.setPositiveButton("Conferma") { dialog, which ->
            val userId = Firebase.auth.currentUser?.uid
            val newUsername = usernameEditText.text.toString()
            val sPref = requireContext().getSharedPreferences("${userId}UserPrefs", Context.MODE_PRIVATE)
            val editor = sPref.edit()
            editor.putString("username", newUsername)
            editor.apply()

            usernameChangedListener?.onUsernameChanged(newUsername)

        }

        builder.setNegativeButton("Annulla", null)

        return builder.create()
    }

    fun setOnUsernameChangedListener(listener: OnUsernameChangedListener) {
        usernameChangedListener = listener
    }

}
