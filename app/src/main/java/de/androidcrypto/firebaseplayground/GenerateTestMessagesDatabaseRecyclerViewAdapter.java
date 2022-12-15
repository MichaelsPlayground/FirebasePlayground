package de.androidcrypto.firebaseplayground;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GenerateTestMessagesDatabaseRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private List<String> mItemList;

    public GenerateTestMessagesDatabaseRecyclerViewAdapter(List<String> itemList) {
        mItemList = itemList;
    }

    // todo fill this with live data from FirebaseDatabase

    // Based on the View type we are instantiating the
    // ViewHolder in the onCreateViewHolder() method
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
            // the R.layout is the single item xml filename
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
            return new ItemViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingviewHolder(view);
        }
    }

    // Inside the onBindViewHolder() method we
    // are checking the type of ViewHolder
    // instance and populating the row accordingly
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            populateItemRows((ItemViewHolder) holder, position);
        } else if (holder instanceof LoadingviewHolder) {
            showLoadingView((LoadingviewHolder) holder, position);
        }
    }

    // getItemCount() method returns the size of the list
    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

    // getItemViewType() method is the method where we check each element
    // of the list. If the element is NULL we set the view type as 1 else 0
    public int getItemViewType(int position) {
        return mItemList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemMessageText;
        TextView tvItemNameText;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemMessageText = itemView.findViewById(R.id.message_text);
            tvItemNameText = itemView.findViewById(R.id.name_text);
        }
    }

    private class LoadingviewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        public LoadingviewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressbar);
        }
    }

    private void showLoadingView(LoadingviewHolder viewHolder, int position) {
        // Progressbar would be displayed
    }

    private void populateItemRows(ItemViewHolder viewHolder, int position) {
        String item = mItemList.get(position);
        viewHolder.tvItemMessageText.setText(item);
        // todo fix this
        viewHolder.tvItemNameText.setText("fixed date");

    }
}
