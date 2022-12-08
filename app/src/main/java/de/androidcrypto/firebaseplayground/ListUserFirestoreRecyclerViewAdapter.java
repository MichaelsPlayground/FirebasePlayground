package de.androidcrypto.firebaseplayground;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListUserFirestoreRecyclerViewAdapter extends RecyclerView.Adapter<ListUserFirestoreRecyclerViewAdapter.MyView> {

    // List with String type
    private final List<String> arrayList;

    // View Holder class which
    // extends RecyclerView.ViewHolder
    public class MyView extends RecyclerView.ViewHolder {
        // Text View
        TextView textView;

        // parameterised constructor for View Holder class
        // which takes the view as a parameter
        public MyView(View view) {
            super(view);
            // initialise TextView with id
            textView = (TextView) view.findViewById(R.id.userRvName);
        }
    }

    // Constructor for adapter class
    // which takes a list of String type
    public ListUserFirestoreRecyclerViewAdapter(List<String> horizontalList) {
        this.arrayList = horizontalList;
    }

    @NonNull
    @Override
    public ListUserFirestoreRecyclerViewAdapter.MyView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item.xml using LayoutInflator
        View itemView
                = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.user_rv_item,
                        parent, false);

        // return itemView
        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListUserFirestoreRecyclerViewAdapter.MyView holder, int position) {
        // Set the text of each item of
        // Recycler view with the list items
        holder.textView.setText(arrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}
