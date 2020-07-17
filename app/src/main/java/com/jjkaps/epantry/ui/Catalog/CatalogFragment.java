package com.jjkaps.epantry.ui.Catalog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ListView;

import android.widget.SearchView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.ui.Fridge.ItemAdapter;
import com.jjkaps.epantry.ui.ItemUI.ItemActivity;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CatalogFragment extends Fragment {

    private CatalogViewModel catalogViewModel;
    private static final String TAG = "CatalogFragment";
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView txt_empty;
    private SearchView searchView;
    private TextView noItemFound;
    private ArrayAdapter<String> arrayAdapter;
    private ListView listView_catalogItem;
    private CollectionReference catalogListRef = db.collection("users").document(uid).collection("catalogList");
    private CollectionReference shopListRef = db.collection("users").document(uid).collection("shoppingList");
    private CollectionReference fridgeListRef = db.collection("users").document(uid).collection("fridgeList");

    private Dialog myDialog;
    private Button sortBarcode;
    private ImageButton imgBtRemove;
    private Boolean sorted = false;
    Button btEdit;
    DocumentReference itemRef;

    //final String itemName;

    BarcodeProduct bp;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        catalogViewModel = new ViewModelProvider(this).get(CatalogViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_catalog, container, false);
        //final TextView textView = root.findViewById(R.id.text_catalog);
        //catalog banner
        if(getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() !=null){
            View view = Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).getCustomView();
            TextView name = view.findViewById(R.id.name);
            name.setText(R.string.title_catalog);
        }
        imgBtRemove = root.findViewById(R.id.ibt_remove);


        txt_empty = root.findViewById(R.id.txt_emptyList);
        searchView = root.findViewById(R.id.search_view);
        noItemFound = root.findViewById(R.id.txt_noItemFound);

        listView_catalogItem = root.findViewById(R.id.listView_catalogItem);

        arrayAdapter = new ArrayAdapter(root.getContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        listView_catalogItem.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        //retrieveCatalogList(root);
        catalogListRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<String> catalogItem = new ArrayList<>();
                        listView_catalogItem = root.findViewById(R.id.listView_catalogItem);

                        if (task.isSuccessful() && task.getResult() != null && task.getResult().size() != 0) {
                            txt_empty.setVisibility(View.INVISIBLE);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                BarcodeProduct bp = document.toObject(BarcodeProduct.class);// TODO use this for views
                                catalogItem.add(document.get("name").toString());
                            }
                            arrayAdapter = new ArrayAdapter(root.getContext(), android.R.layout.simple_list_item_1, catalogItem);
                            listView_catalogItem.setAdapter(arrayAdapter);

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());

                        }
                    }
                });
        //ON ITEM CLICK
        listView_catalogItem.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
//                Log.d(TAG, "onClick: Clicked!");
                String itemName = adapter.getItemAtPosition(position).toString();
//                Toast.makeText(getContext(), itemName, Toast.LENGTH_SHORT).show();
//                //popup displaying extra info and add to other list options
                //    bp = item.toObject(BarcodeProduct.class);
                // final FridgeItem fridgeItem =  item;
                // bp = fridgeItem.getBarcodeProduct();
                //  bp = <insert_document_snapshot>.toObject(BarcodeProduct.class);
                if(bp != null) {
                    Intent i = new Intent(v.getContext(), ItemActivity.class);
                    i.putExtra("barcodeProduct", bp);
                    if(itemRef != null) {
                        i.putExtra("currCollection", "catalogList");
                        i.putExtra("docID", itemRef.getPath());
                    }
                    v.getContext().startActivity(i);
                }

            }
        });



        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) { return false; }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.equals("")) {
                    Log.d(TAG, "onQueryTextChange: " );
                    noItemFound.setVisibility(View.INVISIBLE);
                    retrieveCatalogList(root);
                }else {
                    arrayAdapter.getFilter().filter(s);
                    if (arrayAdapter.isEmpty()){
//                    Log.d(TAG, "onQueryTextChange: "+s);
                        noItemFound.setVisibility(View.VISIBLE);
                    }
                }
                return false;
            }
        });

//SORT BARCODE
        sortBarcode = root.findViewById(R.id.sortBarcode);
        sortBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                arrayAdapter.clear();
                if (sorted) {
                    catalogListRef.get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    // List<String> catalogItem = new ArrayList<>();

                                    if (task.isSuccessful() && task.getResult() != null && task.getResult().size() != 0) {
                                        txt_empty.setVisibility(View.INVISIBLE);
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            //bp = document.toObject(BarcodeProduct.class);
                                            arrayAdapter.add(document.get("name").toString());
                                        }
                                        sortBarcode.setText("Scanned");
                                        sorted = false;
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task.getException());
                                    }
                                }
                            });

                } else {
                    catalogListRef.get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    // List<String> catalogItem = new ArrayList<>();

                                    if (task.isSuccessful() && task.getResult() != null && task.getResult().size() != 0) {
                                        txt_empty.setVisibility(View.INVISIBLE);
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            //bp = document.toObject(BarcodeProduct.class);
                                            // catalogItem.add(document.get("name").toString());
                                            if (document.get("barcode") != null) {
                                                arrayAdapter.add(document.get("name").toString());
                                            }
                                        }
                                        sortBarcode.setText("All");
                                        sorted = true;
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task.getException());

                                    }
                                }
                            });
                }
                arrayAdapter.notifyDataSetChanged();
            }
        });




        return root;
    }

    public void retrieveCatalogList(final View root) {
        //retrieve from db
        catalogListRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<String> catalogItem = new ArrayList<>();
                        final ListView listView_catalogItem = root.findViewById(R.id.listView_catalogItem);

                        if (task.isSuccessful() && task.getResult() != null && task.getResult().size() != 0) {
                            txt_empty.setVisibility(View.INVISIBLE);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                BarcodeProduct bp = document.toObject(BarcodeProduct.class);// TODO use this for views
                                catalogItem.add(document.get("name").toString());
                            }
                            arrayAdapter = new ArrayAdapter(root.getContext(), android.R.layout.simple_list_item_1, catalogItem);
                            listView_catalogItem.setAdapter(arrayAdapter);

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}