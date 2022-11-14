package com.cst3104.androidlab5_listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class MyAdapter extends ArrayAdapter<ChatMessage> {
    List<ChatMessage> messages ;

    public MyAdapter(@NonNull Context context, @NonNull List<ChatMessage> objects) {
            super(context, 0, objects);
        this.messages = objects;
    }

        @Override
        public int getCount() {
            return messages.size();
    }

        @Nullable
        @Override
        public ChatMessage getItem(int position) {
        return messages.get(position);
    }

        @Override
        public long getItemId(int position) {
        return position;
    }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ChatMessage actualMessage = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_layout, parent, false);

        }
            TextView testMessage = convertView.findViewById(R.id.textMessage);
            ImageView receivedImage = convertView.findViewById(R.id.imageReceived);
            ImageView sentImage = convertView.findViewById(R.id.imageSent);
            testMessage.setText(actualMessage.getMessageText());
            if (actualMessage.getStatus()) {
                testMessage.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                receivedImage.setVisibility(View.VISIBLE);
                sentImage.setVisibility(View.INVISIBLE);
            }
            else {
                testMessage.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                sentImage.setVisibility(View.VISIBLE);
                receivedImage.setVisibility(View.INVISIBLE);
            }

        return convertView;
    }


}
