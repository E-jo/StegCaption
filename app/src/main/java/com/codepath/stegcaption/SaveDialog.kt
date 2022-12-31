package com.codepath.stegcaption

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment

class SaveDialog: DialogFragment() {
    var format: String? = ".png"

    interface SaveDialogListener {
        fun onFinishSaveDialog(path: String?, format: String?)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_corners)
        return inflater.inflate(R.layout.dialog_save, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val saveButton = view.findViewById<Button>(R.id.btn_saveFile)
        saveButton.setOnClickListener {
            saveFile(view)
            dismiss()
        }
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioPng -> format = ".png"
                R.id.radioWebP -> format = ".webp"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        //val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun saveFile(view: View) {
        val filePath = view.findViewById<EditText>(R.id.et_file2)
        parentFragmentManager.setFragmentResult(
            "saveChoice", bundleOf(
                "path" to filePath.text.toString(), "format" to format
            )
        )
    }

}