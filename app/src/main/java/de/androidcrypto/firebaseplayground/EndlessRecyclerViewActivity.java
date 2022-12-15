package de.androidcrypto.firebaseplayground;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EndlessRecyclerViewActivity extends AppCompatActivity {

    /**
     * This class is based on this tutorial:
     * https://www.geeksforgeeks.org/endless-recyclerview-in-android/
     * additional classes and views:
     * EndlessRecyclerViewAdapter.java
     * activity_endless_recycler_view.xml, endless_item_row.xml, endless_item_loading.xml
     */

    RecyclerView recyclerView;
    EndlessRecyclerViewAdapter recylerViewAdapter;
    ArrayList<String> rowsArrayList = new ArrayList<>();

    boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_endless_recycler_view);

        recyclerView = findViewById(R.id.endlessRecyclerView);

        // Following three methods have
        // been implemented in this class.
        populateData();
        initAdapter();
        initScrollListener();
    }

    // PopulateData() method shows after how many items load more option
    // should be made available. In our case, i have taken 20 items
    private void populateData() {
        System.out.println("** populateData");
        int i = 1;
        while (i < 21) {
            System.out.println("** rowsArrayList.add: " + i);
            rowsArrayList.add("ITEM " + i);
            i++;
        }
    }

    // initAdapter() method initiates the RecyclerViewAdapter
    private void initAdapter() {
        System.out.println("** initAdapter");
        recylerViewAdapter = new EndlessRecyclerViewAdapter(rowsArrayList);
        recyclerView.setAdapter(recylerViewAdapter);
    }

    // initScrollListener() method is the method where we are checking
    // the scrolled state of the RecyclerView and if bottom-most is visible
    // we are showing the loading view and populating the next list
    private void initScrollListener() {
        System.out.println("** initScrollListener");
        recyclerView
                .addOnScrollListener(new RecyclerView.OnScrollListener() {
                                         @Override
                                         public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                             super.onScrollStateChanged(recyclerView, newState);
                                             System.out.println("** onScrollStateChanged");
                                         }

                                         @Override
                                         public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                             super.onScrolled(recyclerView, dx, dy);
                                             System.out.println("** onScrolled");

                                             LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                                             if (!isLoading) {
                                                 if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == rowsArrayList.size() - 1) {
                                                     // bottom of list!
                                                     loadMore();
                                                     isLoading = true;
                                                 }
                                             }
                                         }
                                     }
                );
    }

    // LoadMore() method is used to implement
    // the functionality of load more
    private void loadMore() {
        System.out.println("** loadMore");
        rowsArrayList.add(null);
        recylerViewAdapter.notifyItemInserted(rowsArrayList.size() - 1);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                rowsArrayList.remove(rowsArrayList.size() - 1);
                int scrollPosition = rowsArrayList.size();
                recylerViewAdapter.notifyItemRemoved(scrollPosition);
                int currentSize = scrollPosition;

                // Next load more option is to be shown after every 10 items.
                int nextLimit = currentSize + 10;

                while (currentSize - 1 < nextLimit) {
                    rowsArrayList.add("Item " + currentSize);
                    currentSize++;
                }

                recylerViewAdapter.notifyDataSetChanged();
                isLoading = false;
            }
        }, 2000);
    }
}