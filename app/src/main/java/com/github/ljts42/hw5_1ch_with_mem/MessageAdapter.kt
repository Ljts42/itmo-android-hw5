package com.github.ljts42.hw5_1ch_with_mem

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.concurrent.Executors

class MessageAdapter(
    private val messagesViewModel: MessagesViewModel, private val onClickImage: (Message) -> Unit
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(
        root: View, private val messagesViewModel: MessagesViewModel, private val cacheDir: File
    ) : RecyclerView.ViewHolder(root) {
        private val usernameView = root.findViewById<TextView>(R.id.username_view)
        private val msgIdView = root.findViewById<TextView>(R.id.msg_id_view)
        private val timeView = root.findViewById<TextView>(R.id.time_view)
        private val textView = root.findViewById<TextView>(R.id.text_view)
        val imageView: ImageView = root.findViewById(R.id.image_view)
        private val handler = Handler(Looper.getMainLooper())

        fun bind(message: Message) {
            if (message.type == MessageType.TEXT) {
                textView.visibility = View.VISIBLE
                imageView.visibility = View.GONE
                textView.text = message.data
            } else {
                textView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                imageView.setImageResource(R.drawable.ic_broken_image)

                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    val image = messagesViewModel.getImage(cacheDir, "thumb/${message.data}")
                    handler.post {
                        if (image != null) imageView.setImageBitmap(image)
                    }
                }
            }
            usernameView.text = message.from
            msgIdView.text = message.id
            timeView.text = message.time.asTime()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (messagesViewModel.getMessages()[position].type) {
            MessageType.TEXT -> 0
            MessageType.IMAGE -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val holder = MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false),
            messagesViewModel,
            parent.context.cacheDir
        )
        if (viewType == 1) {
            holder.imageView.setOnClickListener {
                onClickImage(messagesViewModel.getMessages()[holder.adapterPosition])
            }
        }
        return holder
    }

    override fun getItemCount(): Int = messagesViewModel.getMessages().size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) =
        holder.bind(messagesViewModel.getMessages()[position])
}
