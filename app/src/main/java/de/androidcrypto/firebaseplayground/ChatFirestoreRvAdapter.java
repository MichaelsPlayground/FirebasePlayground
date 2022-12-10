package de.androidcrypto.firebaseplayground;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.androidcrypto.firebaseplayground.models.MessageModel;

public class ChatFirestoreRvAdapter extends RecyclerView.Adapter<ChatFirestoreRvAdapter.ViewHolder> {

    // creating variables for our ArrayList and context
    private ArrayList<MessageModel> chatArrayList;
    private Context context;

    // creating constructor for our adapter class
    public ChatFirestoreRvAdapter(ArrayList<MessageModel> chatArrayList, Context context) {
        this.chatArrayList = chatArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatFirestoreRvAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // passing our layout file for displaying our card item
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.firestore_chat, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatFirestoreRvAdapter.ViewHolder holder, int position) {
        // setting data to our text views from our modal class.
        MessageModel messageModel = chatArrayList.get(position);
        holder.messageTextTV.setText(messageModel.getMessage());
        holder.messageSenderIdTV.setText(messageModel.getSenderId());
        holder.messageTimeTV.setText(String.valueOf(messageModel.getMessageTime()));
    }

    @Override
    public int getItemCount() {
        // returning the size of our array list.
        return chatArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        // creating variables for our text views.
        private final TextView messageTextTV;
        private final TextView messageSenderIdTV;
        private final TextView messageTimeTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our text views.
            messageTextTV = itemView.findViewById(R.id.messageText);
            messageSenderIdTV = itemView.findViewById(R.id.messageSenderId);
            messageTimeTV = itemView.findViewById(R.id.messageTime);
        }
    }
}
