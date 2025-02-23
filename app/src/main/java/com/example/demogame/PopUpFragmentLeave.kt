package com.example.demogame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment

class PopUpFragmentLeave : DialogFragment() {

    //keep track if we should leave
    public var exit: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.popup_leavegame, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonLeave = view.findViewById<Button>(R.id.buttonLeaveYes)
        val buttonStay = view.findViewById<Button>(R.id.buttonStayNo)

        buttonLeave.setOnClickListener {
            //Toast.makeText(context, "This is a button!", Toast.LENGTH_LONG).show()
            exit=true
            dismiss()
        }

        buttonStay.setOnClickListener {
            dismiss()
        }
    }

}