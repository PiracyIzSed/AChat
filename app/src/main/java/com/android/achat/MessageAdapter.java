package com.android.achat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.achat.DataModels.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gaurav on 15-09-2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENDER = 1;
    private static final int VIEW_TYPE_RECEIVER = 2;
    private List<Message> mMessageList ;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Context mCtx;

    public MessageAdapter(List<Message> mMessageList, Context context){
        this.mMessageList=mMessageList;
        this.mCtx=context;
    }

    @Override
    public int getItemViewType(int position) {
        Message m =mMessageList.get(position);
        if (m.getFrom().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return VIEW_TYPE_SENDER;
        }else{
            return VIEW_TYPE_RECEIVER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        switch(viewType){
            case VIEW_TYPE_SENDER:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_sender_layout, parent, false);
                return new SenderMessageViewHolder(v);
            case VIEW_TYPE_RECEIVER:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_receiver_layout, parent, false);
                return new ReceiverMessageViewHolder(v);
            default:
                return null;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = mMessageList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_SENDER) {
            SenderMessageViewHolder senderHolder = (SenderMessageViewHolder) holder;
            senderHolder.messageText.setText(message.getMessage());
            senderHolder.messageTime.setText(message.getTime());
            senderHolder.setSeen(message.isSeen());
        } else if (holder.getItemViewType() == VIEW_TYPE_RECEIVER) {
            ReceiverMessageViewHolder receiverHolder = (ReceiverMessageViewHolder) holder;
            receiverHolder.messageText.setText(message.getMessage());
            receiverHolder.messageTime.setText(message.getTime());
        }
    }

    public class SenderMessageViewHolder extends RecyclerView.ViewHolder {

        TextView messageText, messageTime;
        CircleImageView messageSeen;

        public SenderMessageViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message);
            messageTime = (TextView) itemView.findViewById(R.id.text_message_time);
            messageSeen = itemView.findViewById(R.id.message_seen);

        }

        public void setSeen(String seen) {
            if (seen.equals("true")) {
                messageSeen.setImageResource(R.color.md_green_500);
            } else {
                messageSeen.setImageResource(R.color.md_white_1000);
            }
        }
    }

    public class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {

        TextView messageText, messageTime;

        public ReceiverMessageViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message);
            messageTime = (TextView) itemView.findViewById(R.id.text_message_time);
        }


    }
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
