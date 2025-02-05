package com.jjkaps.epantry.ui.Shopping;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.ProductModels.DietInfo;
import com.jjkaps.epantry.models.ProductModels.DietLabel;
import com.jjkaps.epantry.ui.ItemUI.ItemActivity;
import com.jjkaps.epantry.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class EditShoppingItem extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user;
    private CollectionReference shopListRef;

    private String docRef;
    private String shoppingItemName;
    private String shoppingItemQuantity;
    private String shoppingItemNotes;

    private TextView nameTV;
    private EditText quantityET, notesET;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_shopping_item);
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (user != null) {
            shopListRef = db.collection("users").document(user.getUid()).collection("shoppingList");
        }

        docRef = getIntent().getStringExtra("docID");
        shoppingItemName = getIntent().getStringExtra("name");
        shoppingItemQuantity = getIntent().getStringExtra("quantity");
        shoppingItemNotes = getIntent().getStringExtra("notes");

        //if(docRef != null){
            //Firebase
            //db = FirebaseFirestore.getInstance();
        //}

        //init
        nameTV = findViewById(R.id.item_name);
        nameTV.setText(shoppingItemName);
        quantityET = findViewById(R.id.item_quantity);
        quantityET.setText(shoppingItemQuantity);
        //quantityET.setText("2");
        notesET = findViewById(R.id.item_notes);
        notesET.setText(shoppingItemNotes);

        //set action bar
        if (this.getSupportActionBar() != null){
            this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            TextView name = findViewById(R.id.name);
            Button backButton = findViewById(R.id.txt_close);
            backButton.setVisibility(View.VISIBLE);

            /* close */
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            name.setText("Edit Item");
            ImageButton updateButton = findViewById(R.id.btn_update);
            updateButton.setImageResource(R.drawable.ic_update_check);
            updateButton.setVisibility(View.VISIBLE);

            /* Update item */
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (db != null && docRef != null && shopListRef != null) {
                        // notes changed
                        if(Utils.isNotNullOrEmpty(notesET.getText().toString().trim())){
                            db.collection("users").document(user.getUid()).collection("shoppingList")
                                    .document(docRef).update("notes", notesET.getText().toString().trim());
                        }

                        // if quantity changed
                        if (Utils.isNotNullOrEmpty(quantityET.getText().toString().trim())) {
                            // verify new quantity is valid
                            String quantity = quantityET.getText().toString().trim();
                            Pattern containsNum = Pattern.compile("^[0-9]+$");
                            Matcher isNum = containsNum.matcher(quantity);
                            if (!((quantity.equals("")) || !isNum.find() || (Integer.parseInt(quantity) <= 0) || (Integer.parseInt(quantity) > 99))) { // if it is valid, mark as changed
                                db.collection("users").document(user.getUid()).collection("shoppingList")
                                        .document(docRef).update("quantity", Integer.parseInt(quantity));
                            }
                        }
                        Utils.createToast(EditShoppingItem.this, "Item updated!", Toast.LENGTH_SHORT);
                    }
                }
            });
        }
    }
}
