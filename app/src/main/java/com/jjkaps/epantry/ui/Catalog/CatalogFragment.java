package com.jjkaps.epantry.ui.Catalog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.WriteResult;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.models.ShoppingListItem;
import com.jjkaps.epantry.ui.Shopping.ShoppingItemAdapter;
import com.jjkaps.epantry.ui.scanCode.ScanItem;


import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CatalogFragment extends Fragment {

    private CatalogViewModel catalogViewModel;
    private static final String TAG = "CatalogFragment";
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView txt_empty;

    private SearchView searchView;

    private ArrayAdapter<String> arrayAdapter;
    private ListView listView_catalogItem;

    private Dialog myDialog;

    private ImageButton imgBtRemove;
    Button btEdit;
    DocumentReference itemRef;
    //final String itemName;

    BarcodeProduct bp;

    private CollectionReference catalogListRef = db.collection("users").document(uid).collection("catalogList");
    private CollectionReference shopListRef = db.collection("users").document(uid).collection("shoppingList");
    private CollectionReference fridgeListRef = db.collection("users").document(uid).collection("fridgeList");


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        catalogViewModel = new ViewModelProvider(this).get(CatalogViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_catalog, container, false);
        //final TextView textView = root.findViewById(R.id.text_catalog);
        myDialog = new Dialog(root.getContext());
        //catalog banner
        if(getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() !=null){
            View view = Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).getCustomView();
            TextView name = view.findViewById(R.id.name);
            name.setText(R.string.title_catalog);
        }

        imgBtRemove = root.findViewById(R.id.ibt_remove);

        txt_empty = root.findViewById(R.id.txt_emptyList);
        searchView = root.findViewById(R.id.search_view);
        listView_catalogItem = root.findViewById(R.id.listView_catalogItem);
        arrayAdapter = new ArrayAdapter(root.getContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        listView_catalogItem.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        //retrieve from db
        catalogListRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                       // List<String> catalogItem = new ArrayList<>();

                        if (task.isSuccessful() && task.getResult() != null && task.getResult().size() != 0) {
                            txt_empty.setVisibility(View.INVISIBLE);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                 bp = document.toObject(BarcodeProduct.class);
                               // catalogItem.add(document.get("name").toString());
                                arrayAdapter.add(document.get("name").toString());
                            }
                            arrayAdapter.notifyDataSetChanged();
//                            arrayAdapter = new ArrayAdapter(root.getContext(), android.R.layout.simple_list_item_1, catalogItem);
//                            listView_catalogItem.setAdapter(arrayAdapter);

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
                Log.d(TAG, "onClick: Clicked!");
                final String itemName = (String)adapter.getItemAtPosition(position);
                Toast.makeText(getContext(), itemName, Toast.LENGTH_SHORT).show();
                popupItem(itemName);
                //popup displaying extra info and add to other list options

            }
        });

        //SEARCH BAR
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                arrayAdapter.getFilter().filter(s);

                return false;
            }
        });

        //CLEAR CATALOG
        imgBtRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked!");
                PopupMenu popup = new PopupMenu(getContext(), imgBtRemove);
                popup.getMenuInflater().inflate(R.menu.popup_menu_clearcat, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getItemId() == R.id.item_removeAll) {
                           // case R.id.item_removeAll:
                                clearCatalog();
                                //Toast.makeText(getContext(), "hit remove all", Toast.LENGTH_SHORT).show();
                                return true;
                        }
                        return false;
                    }
                });
                popup.show();

            }
        });

        return root;
    }
    private void clearCatalog() {
        TextView txtClose;
        Button btRemoveAll;
        myDialog.setContentView(R.layout.popup_removeall);
        txtClose =  myDialog.findViewById(R.id.txt_removeAll_close);
        btRemoveAll = myDialog.findViewById(R.id.bt_removeAllYes);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        btRemoveAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                catalogListRef.get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    if (task.getResult().size() == 0) {
                                        Toast.makeText(getContext(), "No Items!", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            catalogListRef.document(document.getId()).delete();
                                        }
                                        Toast.makeText(getContext(), "Your catalog is now empty!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                myDialog.dismiss();
            }
        });
        myDialog.show();
    }
    private void popupItem(final String itemName){
        TextView txtClose;
        TextView txtName;
        TextView txtNotes;
       // final String itemRef;


        myDialog.setContentView(R.layout.popup_catalog_item);

        txtClose = myDialog.findViewById(R.id.txt_close);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

        txtName = myDialog.findViewById(R.id.txtName);
        txtName.setText(itemName);

        btEdit = myDialog.findViewById(R.id.bt_edit);
        catalogListRef.whereEqualTo("name", itemName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && task.getResult().size() != 0) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                     bp = document.toObject(BarcodeProduct.class);
                                    itemRef = document.getReference();

                                }
                            }
                        }
                    }
                });

        btEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getContext(), btEdit);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_editcat, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.moveToFridge:
                                moveToFridge();
                                return true;
                            case R.id.moveToShopping:
                                moveToShopping();
                                return true;
                            case R.id.removeItem:
                                Toast.makeText(getContext(), itemName + " removed", Toast.LENGTH_SHORT).show();
                                itemRef.delete();
                                return true;
                        }

                        return false;
                    }
                });
                popupMenu.show();

            }

        });


        myDialog.show();
    }

    private void moveToFridge(){
     //   Map<String, BarcodeProduct> docData = new HashMap<>();
       // catalogListRef.
        Toast.makeText(getContext(),  bp.getName() + " added to Fridge", Toast.LENGTH_SHORT).show();
        fridgeListRef.add(bp);

    }

    private void moveToShopping(){
        Toast.makeText(getContext(),  bp.getName() + " added to Shopping List", Toast.LENGTH_SHORT).show();
        shopListRef.add(bp);
    }
}