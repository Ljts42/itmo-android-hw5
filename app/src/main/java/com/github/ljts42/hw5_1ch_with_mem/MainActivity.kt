package com.github.ljts42.hw5_1ch_with_mem

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ljts42.hw5_1ch_with_mem.databinding.ActivityMainBinding
import java.io.File
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var messagesViewModel: MessagesViewModel
    private lateinit var dbHelper: MessagesDbHelper
    private lateinit var cacheDir: File

    private val loadExecutor = Executors.newSingleThreadExecutor()
    private var isLoading = false

    private val username = "Test"
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = MessagesDbHelper(this)
        messagesViewModel = ViewModelProvider(this)[MessagesViewModel::class.java]
        cacheDir = applicationContext.cacheDir

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val viewManager = LinearLayoutManager(this)
        binding.chatRecycleView.apply {
            layoutManager = viewManager
            adapter = MessageAdapter(messagesViewModel, onClickImage = {
                val intent = Intent(context, FullImage::class.java)
                intent.putExtra("imageUrl", it.data)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            })
        }
        getMessages(20, true)

        binding.chatRecycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = binding.chatRecycleView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastVisibleItemPosition() == messagesViewModel.getMessages().size - 1) {
                    getMessages()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = binding.chatRecycleView.layoutManager as LinearLayoutManager
                if (newState == RecyclerView.SCROLL_STATE_IDLE && layoutManager.findLastVisibleItemPosition() == messagesViewModel.getMessages().size - 1) {
                    getMessages()
                }
            }
        })
    }

    private fun getMessages(count: Int = 10, init: Boolean = false) {
        if (isLoading) return
        isLoading = true
        loadExecutor.execute {
            var newElementsList = if (init) dbHelper.getMessages("1@channel")
            else MyInternetUtility.getMessages(messagesViewModel.getLastId(), count)

            if (init && newElementsList.isEmpty()) {
                newElementsList =
                    MyInternetUtility.getMessages(messagesViewModel.getLastId(), count)
                newElementsList.forEach {
                    dbHelper.addMessage(it)
                }
            }

            if (!init) {
                newElementsList.forEach {
                    dbHelper.addMessage(it)
                }
            }

            runOnUiThread {
                if (newElementsList.isNotEmpty()) {
                    val start = messagesViewModel.getMessages().size
                    messagesViewModel.addMessages(newElementsList)
                    binding.chatRecycleView.adapter?.notifyItemRangeInserted(
                        start, newElementsList.size
                    )
                    messagesViewModel.updateLastId(newElementsList.last().id.toInt())
                }
                isLoading = false
            }
        }
    }

    fun sendMessage(view: View) {
        val messageSendExecutor = Executors.newSingleThreadExecutor()
        messageSendExecutor.execute {
            MyInternetUtility.sendMessage(username, binding.inputField.text.toString())
        }
        binding.inputField.setText("")
    }

    fun choosePhoto(view: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && intent != null && intent.data != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, intent.data)
                if (bitmap != null) {
                    MyInternetUtility.sendImage(username, bitmap)
                }
            }
        }
    }
}