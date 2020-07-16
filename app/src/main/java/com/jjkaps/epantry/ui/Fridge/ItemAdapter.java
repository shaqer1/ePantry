package com.jjkaps.epantry.ui.Fridge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjkaps.epantry.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference fridgeListRef = db.collection("users").document(uid).collection("fridgeList");

    private ArrayList<FridgeItem> itemList;
    //private final AdapterView.OnItemClickListener incListener;

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView tvItemName;
        public TextView tvItemQuantity;
        public TextView tvItemNotes;
        private Button incButton;
        private Button decButton;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tv_fridgeItem);
            tvItemQuantity = itemView.findViewById(R.id.tv_fridgeItemQuantity);
            tvItemNotes = itemView.findViewById(R.id.tv_notes);
            incButton = itemView.findViewById(R.id.btn_inc);
            decButton = itemView.findViewById(R.id.btn_dec);

        }
    }

    public ItemAdapter(ArrayList<FridgeItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fridge_item, parent, false); // assigns the fridge_item layout to rv
        ItemViewHolder ivh = new ItemViewHolder(v);
        return ivh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        final FridgeItem currentItem = itemList.get(position);

        holder.tvItemName.setText(currentItem.getTvFridgeItemName());
        holder.tvItemQuantity.setText(currentItem.getTvFridgeItemQuantity());
        holder.tvItemNotes.setText(currentItem.getTvFridgeItemNotes());

        // incrementing the quantity
        holder.incButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // increment on the UI
                currentItem.incTvFridgeItemQuantity();
                holder.tvItemQuantity.setText(currentItem.getTvFridgeItemQuantity());

                final String[] docId = new String[1];

                // find the item thats quantity is being updated
                fridgeListRef.whereEqualTo("name", currentItem.getTvFridgeItemName())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult() != null && task.getResult().size() != 0) {
                                        docId[0] = task.getResult().getDocuments().get(0).getId(); // this identifies the document we want to change

                                        // update this document's quantity
                                        db.collection("users").document(uid).collection("fridgeList").document(docId[0])
                                                .update("quantity", currentItem.getTvFridgeItemQuantity());
                                    }
                                } else {
                                    // todo: what should happen if this is not successful? - currently just doesn't update the database but shouldn't throw any errors
                                }
                            }
                        });
            }
        });
        holder.decButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // cannot decrement below 0
                if (!currentItem.getTvFridgeItemQuantity().contentEquals("0")) {
                    currentItem.decTvFridgeItemQuantity();
                    holder.tvItemQuantity.setText(currentItem.getTvFridgeItemQuantity());

                    final String[] docId = new String[1];

                    // find the item thats quantity is being updated
                    fridgeListRef.whereEqualTo("name", currentItem.getTvFridgeItemName())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult() != null && task.getResult().size() != 0) {
                                            docId[0] = task.getResult().getDocuments().get(0).getId(); // this identifies the document we want to change

                                            // update this document's quantity
                                            db.collection("users").document(uid).collection("fridgeList").document(docId[0])
                                                    .update("quantity", currentItem.getTvFridgeItemQuantity());
                                        }
                                    } else {
                                        // todo: what should happen if this is not successful? - currently just doesn't update the database but shouldn't throw any errors
                                    }
                                }
                            });

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
