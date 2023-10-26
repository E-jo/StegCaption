package com.codepath.stegcaption.ui.main

import android.app.Activity
import android.app.ProgressDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.blue
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.fragment.app.Fragment
import com.codepath.stegcaption.*
import java.io.File
import java.io.FileOutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EncodeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EncodeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var btnEncode: Button
    lateinit var imgView: ImageView
    lateinit var btnShare: Button
    lateinit var btnLoad: Button
    lateinit var btnSave: Button
    lateinit var btnCopy: Button
    lateinit var btnPaste: Button
    lateinit var colorChoice: RadioGroup
    lateinit var secretMessage: EditText
    lateinit var password: EditText
    lateinit var progressBar: ProgressBar

    //private var _binding: FragmentTabbedMainBinding? = null
    //private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        childFragmentManager.setFragmentResultListener("loadChoice", this) { _, bundle ->
            val type = bundle.getString("type")
            val path = bundle.getString("path")
            Log.d("StegCap", "load result listener called")
            when (type) {
                "url" -> loadFromUrlOldVer(path)
                "file" -> loadFromFile(path)
                "browse" -> loadPicker()
            }
        }
        childFragmentManager.setFragmentResultListener("saveChoice", this) { _, bundle ->
            val path = bundle.getString("path")
            val format = bundle.getString("format")
            Log.d("StegCap", "save result listener called")
            val androidVersion = Build.VERSION.SDK_INT
            if (androidVersion < Build.VERSION_CODES.R && format == ".webp") {
                Toast.makeText(context,
                    "Saving as .webp requires a newer version of Android", Toast.LENGTH_LONG).show()
            } else {
                save(path, format)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_encode, container, false)

        imgView = view!!.findViewById(R.id.imageView)
        colorChoice = view.findViewById(R.id.radioGroupColor)
        btnEncode = view.findViewById(R.id.btnEncode)
        btnEncode.setOnClickListener{
            encode(colorChoice.checkedRadioButtonId)
        }
        btnCopy = view.findViewById(R.id.btnCopy)
        btnCopy.setOnClickListener{
            copy()
        }
        btnPaste = view.findViewById(R.id.btnPaste)
        btnPaste.setOnClickListener{
            paste()
        }
        btnSave = view.findViewById(R.id.btnSave)
        btnSave.setOnClickListener{
            saveAction()
        }
        btnLoad = view.findViewById(R.id.btnLoad)
        btnLoad.setOnClickListener{
            loadAction()
        }
        btnShare = view.findViewById(R.id.btnShare)
        btnShare.setOnClickListener{
            share(imgView)
        }
        progressBar = view.findViewById(R.id.progressBar2)

        secretMessage = view.findViewById(R.id.textView)
        password = view.findViewById(R.id.textView2)
        secretMessage = view.findViewById(R.id.textView)
        password = view.findViewById(R.id.textView2)
        return view
    }

    private fun encode(colorCode: Int) {
        progressBar.visibility = View.VISIBLE

        var checkMessage = byteArrayOf()

        val image: Bitmap = imgView.drawable.toBitmap()

        val message: String = secretMessage.text!!.toString()
        val password: String = password.text!!.toString()
        var messageByteArray = message.encodeToByteArray()
        val ending = byteArrayOf(0x0, 0x0, 0x3)
        val passwordByteArray = password.encodeToByteArray()

        if (messageByteArray.size * 8 > image.height * image.width - 3) {
            Log.d("StegCap","Image too small for message")
            return
        }

        // TODO
        // move encode() to another thread so the following will be possible
        val progressDialog: ProgressDialog = ProgressDialog(context)
        progressDialog.setMessage("Encoding")
        progressDialog.setCancelable(false)
        progressDialog.show()

        messageByteArray = encrypt(messageByteArray, passwordByteArray)
        checkMessage += messageByteArray
        messageByteArray += ending
        Log.d("StegCap", "Message byte array size: ${messageByteArray.size.toString()}")

        var bitIndex = 0
        var messageByteIndex = 0
        val newImageColors = mutableListOf<Int>()

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val color = image.getPixel(x, y)
                var rgb = 0
                when (colorCode) {
                    R.id.radioBlue -> {
                        val blue = if (messageByteIndex >= messageByteArray.size) {
                            color.blue
                        } else {
                            val messageByte = messageByteArray[messageByteIndex].toInt()
                            setLeastSignificantBit(color.blue, messageByte.toBinary(8)[bitIndex])
                        }
                        rgb = Color.rgb(
                            color.red,
                            color.green,
                            blue
                        )
                    }
                    R.id.radioRed -> {
                        val red = if (messageByteIndex >= messageByteArray.size) {
                            color.red
                        } else {
                            val messageByte = messageByteArray[messageByteIndex].toInt()
                            setLeastSignificantBit(color.red, messageByte.toBinary(8)[bitIndex])
                        }
                        rgb = Color.rgb(
                            red,
                            color.green,
                            color.blue
                        )
                    }
                    R.id.radioGreen -> {
                        val green = if (messageByteIndex >= messageByteArray.size) {
                            color.green
                        } else {
                            val messageByte = messageByteArray[messageByteIndex].toInt()
                            setLeastSignificantBit(color.green, messageByte.toBinary(8)[bitIndex])
                        }
                        rgb = Color.rgb(
                            color.red,
                            green,
                            color.blue
                        )
                    }
                }
                bitIndex++
                if (bitIndex > 7) {
                    messageByteIndex++
                    bitIndex = 0
                }
                newImageColors.add(rgb)
            }
        }

        progressDialog.dismiss()
        val newImage = Bitmap.createBitmap(newImageColors.toIntArray(),
            image.width, image.height, Bitmap.Config.ARGB_8888)
        imgView.setImageBitmap(newImage)
        progressBar.visibility = View.INVISIBLE
    }

    private fun encrypt(message: ByteArray, password: ByteArray) : ByteArray {
        var passwordByteCounter = 0
        var passwordBitCounter = 0
        val encrypted = mutableListOf<Byte>()
        var encryptedByte = ""
        for (byte in message) {
            for (bit in byte.toInt().toBinary(8)) {
                val passwordBit = password[passwordByteCounter].toInt().toBinary(8)[passwordBitCounter]
                encryptedByte += if (passwordBit != bit) {
                    "1"
                } else {
                    "0"
                }
                passwordBitCounter++
                if (passwordBitCounter > 7) {
                    passwordBitCounter = 0
                    passwordByteCounter++
                    encrypted.add(encryptedByte.toByte(2))
                    encryptedByte = ""
                    if (passwordByteCounter >= password.size) {
                        passwordByteCounter = 0
                    }
                }
            }
        }
        return encrypted.toByteArray()
    }

    // File I/O
    private fun loadAction() {
        LoadDialog().show(childFragmentManager, "Load")
    }

    private fun saveAction() {
        SaveDialog().show(childFragmentManager, "Save")
    }

    private fun loadFromFile(fileName: String?) {
        val file = File(activity?.getExternalFilesDir("images"), "$fileName")
        Log.d("StegCap",
            "Load file exists: ${file.exists()}\nTried to load from: ${file.absolutePath}")
        imgView.setImageDrawable(Drawable.createFromPath(file.toString()))
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val cr: ContentResolver = activity?.contentResolver!!
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                try {
                    val inputStream = cr.openInputStream(uri)
                    val image = BitmapFactory.decodeStream(inputStream)
                    imgView.setImageBitmap(image)
                } catch (e: Exception) {
                    e.message?.let {
                        Log.e("Error", it)
                    }
                    e.printStackTrace()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun save(fileName: String?, format: String?) {
        try {
            val image: Bitmap = imgView.drawable.toBitmap()
            val file = File(activity?.getExternalFilesDir("images"), "$fileName$format")
            val fileOutStream = FileOutputStream(file)
            when (format) {
                ".png" -> image.compress(Bitmap.CompressFormat.PNG, 100, fileOutStream)
                ".webp" -> image.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, fileOutStream)
            }
            fileOutStream.flush()
            fileOutStream.close()
            Log.d("StegCap", "Save file exists: ${file.exists()}\nTried to save to: ${file.absolutePath}")

        } catch (e: Exception) {
            e.message?.let { Log.d("Error", it) }
            e.printStackTrace()
        }
    }

    private fun share(imageView: ImageView) {
        val image: Bitmap = imageView.drawable.toBitmap()
        val file = File(activity?.getExternalFilesDir("images"), "temp.png")
        val fileOutStream = FileOutputStream(file)
        image.compress(Bitmap.CompressFormat.PNG, 100, fileOutStream)
        fileOutStream.flush()
        fileOutStream.close()
        val contentUri = activity?.applicationContext?.let {
            FileProvider.getUriForFile(
                it,
                "com.codepath.stegcaption.fileprovider",
                file
            )
        }
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "image/png"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(Intent.createChooser(shareIntent, "Share Image"))
        file.deleteOnExit()
    }

    private fun copy() {
        try {
            val clipboardManager = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val image: Bitmap = imgView.drawable!!.toBitmap()
            val file = File(activity?.getExternalFilesDir("images"), "temp.png")
            val fileOutStream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 100, fileOutStream)
            fileOutStream.flush()
            fileOutStream.close()
            val contentUri = activity?.applicationContext?.let {
                FileProvider.getUriForFile(
                    it,
                    "com.codepath.stegcaption.fileprovider",
                    file
                )
            }
            val clip: ClipData = ClipData.newUri(activity?.contentResolver, "URI", contentUri)
            clipboardManager.setPrimaryClip(clip)
            file.deleteOnExit()
        } catch (e: Exception) {
            e.message?.let {
                Log.e("Error", it)
            }
            e.printStackTrace()
        }
    }

    private fun paste() {
        try {
            val cr: ContentResolver? = activity?.contentResolver
            val clipboardManager = activity?.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
            val pasteData = clipboardManager.primaryClip
            val contentUri = pasteData!!.getItemAt(0).uri

            val inputStream = cr?.openInputStream(contentUri)
            val image = BitmapFactory.decodeStream(inputStream)
            imgView.setImageBitmap(image)
        } catch (e: Exception) {
            e.message?.let {
                Log.e("Error", it)
            }
            e.printStackTrace()
        }
    }

    private fun loadPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, 0)
    }

    private fun loadFromUrlOldVer(url: String?) {
        imgView.let { DownloadImageAsyncTask(it).execute(url) }
    }

    // helper functions
    private fun setLeastSignificantBit(x: Int, y: Char): Int {
        val xBinary = x.toBinary(8)
        val newX = (xBinary.subSequence(0, xBinary.length - 1)).toString() + y
        return newX.toInt(2)
    }

    private fun Int.toBinary(length: Int): String =
        String.format("%" + length + "s",
            this.toString(2)).replace(" ", "0")

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EncodeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EncodeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}