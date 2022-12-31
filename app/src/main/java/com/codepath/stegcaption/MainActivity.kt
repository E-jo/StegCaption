package com.codepath.stegcaption

import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.Image
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.graphics.blue
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.green
import androidx.core.graphics.red
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), SaveDialog.SaveDialogListener,
    LoadDialog.LoadDialogListener {

    var checkMessage = byteArrayOf()
    var imageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupColor)

        val imageOriginal = findViewById<ImageView>(R.id.imageView)
        val imageNew = findViewById<ImageView>(R.id.imageView2)

        val encodeButton = findViewById<Button>(R.id.btnEncode)
        encodeButton.setOnClickListener {
            encode(radioGroup.checkedRadioButtonId)
        }
        val decodeButton = findViewById<Button>(R.id.btnDecode)
        decodeButton.setOnClickListener {
            decode(radioGroup.checkedRadioButtonId)
        }
        val saveButton = findViewById<Button>(R.id.btnSave)
        saveButton.setOnClickListener {
            saveAction()
        }
        val loadButton = findViewById<Button>(R.id.btnLoad)
        loadButton.setOnClickListener {
            imageView = imageOriginal
            loadAction()
        }
        val loadButton2 = findViewById<Button>(R.id.btnLoad2)
        loadButton2.setOnClickListener {
            imageView = imageNew
            loadAction()
        }
        val shareButton = findViewById<Button>(R.id.btnShare)
        shareButton.setOnClickListener {
            share(imageNew)
        }
        val copyButton = findViewById<Button>(R.id.btnCopy)
        copyButton.setOnClickListener {
            imageView = imageOriginal
            copy()
        }
        val copyButton2 = findViewById<Button>(R.id.btnCopy2)
        copyButton2.setOnClickListener {
            imageView = imageNew
            copy()
        }
        val pasteButton = findViewById<Button>(R.id.btnPaste)
        pasteButton.setOnClickListener {
            imageView = imageNew
            paste()
        }
        val pasteButton2 = findViewById<Button>(R.id.btnPaste2)
        pasteButton2.setOnClickListener {
            imageView = imageOriginal
            paste()
        }
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("image/") == true) {
                    (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                        val cr = contentResolver
                        val inputStream = cr.openInputStream(it)
                        val image = BitmapFactory.decodeStream(inputStream)
                        imageNew.setImageBitmap(image)
                        Toast.makeText(this,
                            "Image is null: ${image == null}\nUri:${it.toString()}",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d("StegCap", "onCreateOptionsMenu() called")
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_tabbed_main, menu)
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_help -> Log.d("StegCap", "Help menu item clicked")
            R.id.menu_exit -> Log.d("StegCap", "Exit menu item clicked")
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun loadAction() {
        LoadDialog().show(supportFragmentManager, "Load")
    }

    private fun saveAction() {
        SaveDialog().show(supportFragmentManager, "Save")
    }

    override fun onFinishLoadDialog(path: String?, type: String?) {
        when (type) {
            "url" -> loadFromUrl(path)
            "file" -> loadFromFile(path)
            "browse" -> loadPicker()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onFinishSaveDialog(path: String?, format: String?) {
        val androidVersion = Build.VERSION.SDK_INT
        if (androidVersion < Build.VERSION_CODES.R && format == ".webp") {
            Toast.makeText(this,
                "Saving as .webp requires a newer version of Android", Toast.LENGTH_LONG).show()
        } else {
            save(path, format)
        }
    }

    private fun loadFromFile(fileName: String?) {
        val file = File(getExternalFilesDir("images"), "$fileName")
        val fileLog = "Load file exists: ${file.exists()}\nTried to load from: ${file.absolutePath}"
        Toast.makeText(this, fileLog, Toast.LENGTH_LONG).show()
        imageView?.setImageDrawable(Drawable.createFromPath(file.toString()))
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun save(fileName: String?, format: String?) {
        try {
            val imageNew = findViewById<ImageView>(R.id.imageView2)
            val image: Bitmap = imageNew.drawable.toBitmap()
            val file = File(getExternalFilesDir("images"), "$fileName$format")
            val fileOutStream = FileOutputStream(file)
            when (format) {
                ".png" -> image.compress(Bitmap.CompressFormat.PNG, 100, fileOutStream)
                ".webp" -> image.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, fileOutStream)
            }
            fileOutStream.flush()
            fileOutStream.close()
            val fileLog = "Save file exists: ${file.exists()}\nTried to save to: ${file.absolutePath}"
            Toast.makeText(this, fileLog, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.message?.let { Log.d("Error", it) }
            e.printStackTrace()
        }
    }

    private fun encode(colorCode: Int) {
        checkMessage = byteArrayOf()
        val imageOriginal = findViewById<ImageView>(R.id.imageView)
        val imageNew = findViewById<ImageView>(R.id.imageView2)
        val image: Bitmap = imageOriginal.drawable.toBitmap()
        val messageInput = findViewById<TextView>(R.id.textView)
        val passwordInput = findViewById<TextView>(R.id.textView2)

        val message: String = messageInput.text!!.toString()
        val password: String = passwordInput.text!!.toString()
        var messageByteArray = message.encodeToByteArray()
        val ending = byteArrayOf(0x0, 0x0, 0x3)
        val passwordByteArray = password.encodeToByteArray()

        if (messageByteArray.size * 8 > image.height * image.width) {
            Toast.makeText(this, "Image is not large enough to encode message." +
                    "\nTry using a shorter message or a larger image.", Toast.LENGTH_LONG).show()
            return
        }

        messageByteArray = encrypt(messageByteArray, passwordByteArray)
        checkMessage += messageByteArray
        messageByteArray += ending
        Log.d("Message byte array size: ", messageByteArray.size.toString())

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

        val newImage = Bitmap.createBitmap(newImageColors.toIntArray(),
            image.width, image.height, Bitmap.Config.ARGB_8888)
        imageNew.setImageBitmap(newImage)
    }

    private fun decode(colorCode: Int) {
        val imageNew = findViewById<ImageView>(R.id.imageView2)
        val image: Bitmap = imageNew.drawable.toBitmap()

        val lastThreeBytes = byteArrayOf(0x9, 0x9, 0x9)
        val endingBytes = byteArrayOf(0x0, 0x0, 0x3)
        var bitIndex = 0
        var bitString = ""
        val messageByteArray = mutableListOf<Byte>()

        try {
            loop@ for (y in 0 until image.height) {
                for (x in 0 until image.width) {

                    // get pixel rgb
                    val color = image.getPixel(x, y)

                    // keep reading until the three ending bytes are found
                    Log.d("Ending bytes found: ", "${lastThreeBytes.contentEquals(endingBytes)}")

                    if (!lastThreeBytes.contentEquals(endingBytes)) {
                        when (colorCode) {
                            // get the least significant bit from the chosen color
                            R.id.radioBlue -> bitString += color.blue.toBinary(8)[7]
                            R.id.radioGreen -> bitString += color.green.toBinary(8)[7]
                            R.id.radioRed -> bitString += color.red.toBinary(8)[7]
                        }
                        bitIndex++

                        // check to see if we are done with a byte
                        if (bitIndex > 7) {
                            // if so, update the last three bytes array
                            lastThreeBytes[0] = lastThreeBytes[lastThreeBytes.lastIndex - 1]
                            lastThreeBytes[lastThreeBytes.lastIndex - 1] =
                                lastThreeBytes[lastThreeBytes.lastIndex]

                            // new last byte is also the byte we add to the byte array
                            // holding our received message
                            lastThreeBytes[lastThreeBytes.lastIndex] = bitString.toByte(2)
                            messageByteArray.add(bitString.toByte(2))
                            Log.d("decoding:", bitString)

                            // reset
                            bitString = ""
                            bitIndex = 0
                        }
                    } else {
                        break@loop
                    }
                }
            }
            Log.d("Message byte array size: ", messageByteArray.size.toString())
            val passwordInput = findViewById<TextView>(R.id.textView2)
            val password = passwordInput.text!!.toString().toByteArray()
            val mesByteArray = messageByteArray.dropLast(3).toByteArray()
            Log.d("Message equals original encoded message: ",
                checkMessage.contentEquals(mesByteArray).toString()
            )
            val decryptedMessage = decrypt(mesByteArray, password)

            val outputTextView = findViewById<TextView>(R.id.textView3)
            outputTextView.text = decryptedMessage.toString(Charsets.UTF_8)
            Toast.makeText(this, decryptedMessage.toString(Charsets.UTF_8), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "No message found", Toast.LENGTH_LONG).show()
        }
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

    private fun decrypt(encryptedMessage: ByteArray, password: ByteArray) : ByteArray {
        var passwordByteCounter = 0
        var passwordBitCounter = 0
        val decrypted = mutableListOf<Byte>()
        var decryptedByte = ""
        for (byte in encryptedMessage) {
            for (bit in byte.toInt().toBinary(8)) {
                val passwordBit = password[passwordByteCounter].toInt().toBinary(8)[passwordBitCounter]
                decryptedByte += if (passwordBit != bit) {
                    "1"
                } else {
                    "0"
                }
                passwordBitCounter++
                if (passwordBitCounter > 7) {
                    passwordBitCounter = 0
                    passwordByteCounter++
                    decrypted.add(decryptedByte.toByte(2))
                    decryptedByte = ""
                    if (passwordByteCounter >= password.size) {
                        passwordByteCounter = 0
                    }
                }
            }
        }
        return decrypted.toByteArray()
    }

    private fun setLeastSignificantBit(x: Int, y: Char): Int {
        val xBinary = x.toBinary(8)
        val newX = (xBinary.subSequence(0, xBinary.length - 1)).toString() + y
        return newX.toInt(2)
    }

    private fun Int.toBinary(length: Int): String =
        String.format("%" + length + "s",
            this.toString(2)).replace(" ", "0")

    private fun loadFromUrl(url: String?) {
        val es = Executors.newSingleThreadExecutor()
        val downloadCallable = url?.let {
            DownloadImageCallable(it)
        }
        val resultImage = es.submit(downloadCallable)
        es.shutdown()
        imageView?.setImageBitmap(resultImage.get())
    }

    private fun loadFromUrlOldVer(url: String?) {
        imageView?.let { DownloadImageAsyncTask(it).execute(url) }
    }

    private fun copy() {
        try {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val image: Bitmap = imageView?.drawable!!.toBitmap()
            val file = File(getExternalFilesDir("images"), "temp.png")
            val fileOutStream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 100, fileOutStream)
            fileOutStream.flush()
            fileOutStream.close()
            val contentUri = getUriForFile(
                applicationContext,
                "com.codepath.stegcaption.fileprovider",
                file
            )
            val clip: ClipData = ClipData.newUri(contentResolver, "URI", contentUri)
            clipboardManager.setPrimaryClip(clip)
            file.deleteOnExit()
        } catch (e: Exception) {
            e.message?.let {
                Log.e("Error", it)
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
            e.printStackTrace()
        }
    }

    private fun paste() {
        try {
            val cr: ContentResolver = contentResolver
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val pasteData = clipboardManager.primaryClip
            val contentUri = pasteData!!.getItemAt(0).uri

            val inputStream = cr.openInputStream(contentUri)
            val image = BitmapFactory.decodeStream(inputStream)
            imageView?.setImageBitmap(image)
        } catch (e: Exception) {
            e.message?.let {
                Log.e("Error", it)
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val cr: ContentResolver = contentResolver
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                try {
                    val inputStream = cr.openInputStream(uri)
                    val image = BitmapFactory.decodeStream(inputStream)
                    imageView?.setImageBitmap(image)
                } catch (e: Exception) {
                    e.message?.let {
                        Log.e("Error", it)
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                    }
                    e.printStackTrace()
                }
            }
        }
    }

    private fun share(imageView: ImageView) {
        val image: Bitmap = imageView.drawable.toBitmap()
        val file = File(getExternalFilesDir("images"), "temp.png")
        val fileOutStream = FileOutputStream(file)
        image.compress(Bitmap.CompressFormat.PNG, 100, fileOutStream)
        fileOutStream.flush()
        fileOutStream.close()
        val contentUri = getUriForFile(applicationContext,
            "com.codepath.stegcaption.fileprovider", file)
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "image/png"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(Intent.createChooser(shareIntent, "Share Image"))
        file.deleteOnExit()
    }
}
