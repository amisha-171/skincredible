package com.example.softeng306project1team22.Activities;


import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.softeng306project1team22.Adapters.CategoryAdapter;
import com.example.softeng306project1team22.Adapters.CompactItemAdapter;
import com.example.softeng306project1team22.Models.Category;
import com.example.softeng306project1team22.Models.Cleanser;
import com.example.softeng306project1team22.Models.Item;
import com.example.softeng306project1team22.Models.Moisturiser;
import com.example.softeng306project1team22.Models.Sunscreen;
import com.example.softeng306project1team22.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Category> categoryList = new ArrayList<>();
    List<Item> recentlyViewed = new ArrayList<>();
    CategoryAdapter adapter;
    CompactItemAdapter itemAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SearchView searchBar = findViewById(R.id.searchB);
        searchBar.setOnQueryTextListener(null);
        searchBar.setQueryHint("Search Items");
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchItem();
            }
        });
        //Create recyclerView instances for layout
        RecyclerView recyclerView = findViewById(R.id.category);
        RecyclerView historyView = findViewById(R.id.carousel_recycler_view);

        //Fetch All data required
        fetchCategoryData();
        fetchRecentlyViewed();

        //Create Adapters for different views
        itemAdapter = new CompactItemAdapter(recentlyViewed, getApplicationContext(), new CategoryAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                viewItem(position);
            }
        });
        adapter = new CategoryAdapter(categoryList, getApplicationContext(), new CategoryAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                System.out.println("Made it to onItemClick : " + position);
                viewCategory(position);
            }
        });

        //Set adapters that recyclerViews will use
        historyView.setAdapter(itemAdapter);
        recyclerView.setAdapter(adapter);


        //Set layout managers!
        historyView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }

    private void fetchCategoryData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("category");

        collectionRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d("Firestore", "Data retrieved successfully");
            categoryList.addAll(queryDocumentSnapshots.toObjects(Category.class));
            adapter.notifyDataSetChanged();

        }).addOnFailureListener(e -> {
            System.out.println("Category Data Retrieval Failure");
        });
    }

    private void fetchRecentlyViewed() {

        //DISCLAIMER! TEST DATA
        FirebaseFirestore dbs = FirebaseFirestore.getInstance();
        CollectionReference colRef = dbs.collection("recently-viewed");

        colRef.orderBy("timeAdded", Query.Direction.ASCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d("Firestore", "Recently viewed retrieved successfully");
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                String id = documentSnapshot.getString("itemId");
                CollectionReference itemRef;
                if (id.contains("sun")) {
                    itemRef = dbs.collection("sunscreen");
                } else if (id.contains("mos")) {
                    itemRef = dbs.collection("moisturiser");
                } else {
                    itemRef = dbs.collection("cleanser");
                }

                itemRef.document(id).get().addOnSuccessListener(referencedDocSnapshot -> {
                    if (referencedDocSnapshot.getString("categoryName").equals("Sunscreen")) {
                        Sunscreen sunscreen = referencedDocSnapshot.toObject(Sunscreen.class);
                        recentlyViewed.add(sunscreen);

                    } else if (referencedDocSnapshot.getString("categoryName").equals("Cleanser")) {
                        Cleanser cleanser = referencedDocSnapshot.toObject(Cleanser.class);
                        System.out.println("this is a real class: " + cleanser.getName());
                        recentlyViewed.add(cleanser);
                    } else if (referencedDocSnapshot.getString("categoryName").equals("Moisturiser")) {
                        Moisturiser moisturiser = referencedDocSnapshot.toObject(Moisturiser.class);
                        recentlyViewed.add(moisturiser);
                    }
                    itemAdapter.notifyDataSetChanged();
                });
            }
            itemAdapter.notifyDataSetChanged();
        });
    }

    public void viewCategory(int position) {
        Category clickedCategory = categoryList.get(position);
        // Create intent and pass data here
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("categoryName", clickedCategory.getName());
        // Add any other relevant data to the intent
        this.startActivity(intent);
    }

    public void viewItem(int position) {
        Item clickedItem = recentlyViewed.get(position);
        // Create intent and pass data here
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("name", clickedItem.getName());
        // Add any other relevant data to the intent
        this.startActivity(intent);
    }

    public void searchItem() {
        Intent intent = new Intent(this, ListActivity.class);
        this.startActivity(intent);
    }

}