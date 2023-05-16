package com.example.runmate

import android.app.AlertDialog
import android.app.Dialog
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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class UserFragment: Fragment(R.layout.fragment_user), ChangeUNDialogFragment.OnUsernameChangedListener {


    private lateinit var logoutBtn: Button
    private lateinit var usernameBtn: Button
    //private lateinit var dialogView : View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        val user_view = view.findViewById<TextView>(R.id.user_view)
        logoutBtn = view.findViewById<Button>(R.id.btn_logout)
        usernameBtn = view.findViewById<Button>(R.id.username_btn)


        val username = arguments?.getString("USERNAME", "")
        user_view.text = "Ciao $username"



        usernameBtn.setOnClickListener{
            val dialog = ChangeUNDialogFragment()
            dialog.setOnUsernameChangedListener(this) //il listener diventa questo fragment
            dialog.show(requireFragmentManager(),"change username dialog")
        }


        logoutBtn.setOnClickListener {

            Firebase.auth.signOut()
            val intent = Intent(activity, Login::class.java)
            startActivity(intent)
            activity?.finish()
        }


        return view
    }

    //funzione listener che aggiorna lo username. l'interfaccia viene dalla classe ChangeUNDialogFragment
    override fun onUsernameChanged(newUsername: String) {
        val userView = view?.findViewById<TextView>(R.id.user_view)
        userView?.text = "Ciao $newUsername"
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
            val newUsername = usernameEditText.text.toString()
            val sPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sPref.edit()
            editor.putString("username", newUsername)
            editor.apply()

            val uid = Firebase.auth.currentUser?.uid
            val database = Firebase.database("https://runmate-b7137-default-rtdb.europe-west1.firebasedatabase.app/").reference
            val usernameRef = database.child("users/$uid/username")
            usernameRef.setValue(newUsername)


            usernameChangedListener?.onUsernameChanged(newUsername)

        }

        builder.setNegativeButton("Annulla", null)

        return builder.create()
    }

    fun setOnUsernameChangedListener(listener: OnUsernameChangedListener) {
        usernameChangedListener = listener
    }

}
