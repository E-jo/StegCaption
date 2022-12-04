package com.codepath.stegcaption

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import android.widget.EditText

class LoadDialog: DialogFragment() {


    interface LoadDialogListener {
        fun onFinishLoadDialog(path: String?, type: String?)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_corners)
        return inflater.inflate(R.layout.dialog_load, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val urlButton = view.findViewById<Button>(R.id.btn_loadUrl)
        urlButton.setOnClickListener {
            loadUrl(view)
            dismiss()
        }
        val fileButton = view.findViewById<Button>(R.id.btn_loadFile)
        fileButton.setOnClickListener {
            loadFile(view)
            dismiss()
        }
        val browseButton = view.findViewById<Button>(R.id.btn_browse)
        browseButton.setOnClickListener {
            browseFiles()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        //val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun browseFiles() {
        val listener: LoadDialogListener = activity as LoadDialogListener
        listener.onFinishLoadDialog("", "browse")
    }

    private fun loadUrl(view: View) {
        val urlText = view.findViewById<EditText>(R.id.et_url)
        val listener: LoadDialogListener = activity as LoadDialogListener
        listener.onFinishLoadDialog(urlText.text.toString(), "url")
    }

    private fun loadFile(view: View) {
        val fileText = view.findViewById<EditText>(R.id.et_file)
        val listener: LoadDialogListener = activity as LoadDialogListener
        listener.onFinishLoadDialog(fileText.text.toString(), "file")
    }
}