package com.codepath.stegcaption

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.codepath.stegcaption.ui.main.DecodeFragment

class LoadDialog: DialogFragment() {

    interface LoadDialogListener {
        fun onFinishLoadDialog(path: String?, type: String?)
    }

    public lateinit var pathKey: String
    public lateinit var typeKey: String

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
        parentFragmentManager.setFragmentResult(
            "loadChoice", bundleOf("path" to "", "type" to "browse"
            )
        )
    }

    private fun loadUrl(view: View) {
        val urlText = view.findViewById<EditText>(R.id.et_url)
        parentFragmentManager.setFragmentResult(
            "loadChoice", bundleOf(
                "path" to urlText.text.toString(), "type" to "url"
            )
        )
    }

    private fun loadFile(view: View) {
        val fileText = view.findViewById<EditText>(R.id.et_file)
        parentFragmentManager.setFragmentResult(
            "loadChoice", bundleOf(
                "path" to fileText.text.toString(), "type" to "file"
            )
        )
    }
}