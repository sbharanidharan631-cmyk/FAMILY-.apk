package com.familymemories.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Family Memories - Native Android Main Activity
 * Black & Gold Luxury Design with device-local photo management
 */
public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FamilyMemoriesPrefs";
    private static final String KEY_PHOTOS = "saved_photos_json";
    private static final String KEY_CUSTOM_CATS = "custom_categories_json";

    private final List<String> categories = new ArrayList<>();
    private final List<PhotoItem> allPhotos = new ArrayList<>();
    private final List<PhotoItem> displayedPhotos = new ArrayList<>();

    private String selectedCategory = "All Memories";
    private PhotoAdapter photoAdapter;
    private ChipGroup categoryChipGroup;
    private TextView emptyStateText;

    // Modern Android Photo Picker (multi-select)
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(50), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {}
                        
                        PhotoItem item = new PhotoItem(
                                System.currentTimeMillis() + "_" + Math.random(),
                                uri.toString(),
                                selectedCategory.equals("All Memories") ? "Family" : selectedCategory,
                                "Cherished family moment"
                        );
                        allPhotos.add(0, item);
                    }
                    savePhotosToStorage();
                    filterPhotos();
                    Toast.makeText(this, uris.size() + " photos added to " + selectedCategory + "!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDefaultCategories();
        loadStoredData();
        setupUserInterface();
    }

    private void setupDefaultCategories() {
        categories.clear();
        categories.add("All Memories");
        categories.add("Family");
        categories.add("Father");
        categories.add("Mother");
        categories.add("Brother");
        categories.add("Sister");
        categories.add("Me");
        categories.add("Grandparents");
        categories.add("Friends");
    }

    private void setupUserInterface() {
        // Luxury Black-and-Gold programmatic UI layout
        android.widget.LinearLayout root = new android.widget.LinearLayout(this);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0C0A09"));
        root.setPadding(32, 48, 32, 16);

        // Header Title
        TextView title = new TextView(this);
        title.setText("FAMILY MEMORIES");
        title.setTextSize(22f);
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setLetterSpacing(0.15f);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Device-Local Private Photo Organiser");
        subtitle.setTextSize(13f);
        subtitle.setTextColor(Color.parseColor("#A8A29E"));
        subtitle.setPadding(0, 4, 0, 24);
        root.addView(subtitle);

        // Category Chips
        categoryChipGroup = new ChipGroup(this);
        categoryChipGroup.setSingleSelection(true);
        refreshCategoryChips();
        
        android.widget.HorizontalScrollView hScroll = new android.widget.HorizontalScrollView(this);
        hScroll.addView(categoryChipGroup);
        root.addView(hScroll);

        // Action Buttons Row (Add Photos + Add Category)
        android.widget.LinearLayout btnRow = new android.widget.LinearLayout(this);
        btnRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, 20, 0, 20);

        Button btnAddPhoto = new Button(this);
        btnAddPhoto.setText("+ Pick Photos");
        btnAddPhoto.setBackgroundColor(Color.parseColor("#D4AF37"));
        btnAddPhoto.setTextColor(Color.parseColor("#0C0A09"));
        btnAddPhoto.setOnClickListener(v -> pickMultipleMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        ));
        btnRow.addView(btnAddPhoto);

        Button btnAddCat = new Button(this);
        btnAddCat.setText("+ New Category");
        btnAddCat.setBackgroundColor(Color.parseColor("#1F1C18"));
        btnAddCat.setTextColor(Color.parseColor("#D4AF37"));
        btnAddCat.setOnClickListener(v -> promptAddCategory());
        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMarginStart(16);
        btnAddCat.setLayoutParams(lp);
        btnRow.addView(btnAddCat);

        root.addView(btnRow);

        // Empty state notice
        emptyStateText = new TextView(this);
        emptyStateText.setText("No photos added in this category yet.\nTap '+ Pick Photos' to select from your phone gallery.");
        emptyStateText.setTextColor(Color.parseColor("#78716C"));
        emptyStateText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        emptyStateText.setPadding(0, 60, 0, 60);
        root.addView(emptyStateText);

        // Photo Grid RecyclerView
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        photoAdapter = new PhotoAdapter(displayedPhotos, this::onPhotoClick, this::onPhotoDelete);
        recyclerView.setAdapter(photoAdapter);
        root.addView(recyclerView);

        setContentView(root);
        filterPhotos();
    }

    private void refreshCategoryChips() {
        categoryChipGroup.removeAllViews();
        for (String cat : categories) {
            Chip chip = new Chip(this);
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setChecked(cat.equals(selectedCategory));
            chip.setTextColor(Color.parseColor(cat.equals(selectedCategory) ? "#0C0A09" : "#FFEAA7"));
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setOnClickListener(v -> {
                selectedCategory = cat;
                refreshCategoryChips();
                filterPhotos();
            });
            categoryChipGroup.addView(chip);
        }
    }

    private void promptAddCategory() {
        EditText input = new EditText(this);
        input.setHint("Category name (e.g. Cousins, Pets, Trip)");
        new AlertDialog.Builder(this)
                .setTitle("Add Custom Category")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String catName = input.getText().toString().trim();
                    if (!catName.isEmpty() && !categories.contains(catName)) {
                        categories.add(catName);
                        selectedCategory = catName;
                        refreshCategoryChips();
                        filterPhotos();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void filterPhotos() {
        displayedPhotos.clear();
        for (PhotoItem p : allPhotos) {
            if (selectedCategory.equals("All Memories") || p.category.equalsIgnoreCase(selectedCategory)) {
                displayedPhotos.add(p);
            }
        }
        if (photoAdapter != null) photoAdapter.notifyDataSetChanged();
        if (emptyStateText != null) {
            emptyStateText.setVisibility(displayedPhotos.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void onPhotoClick(PhotoItem item) {
        new AlertDialog.Builder(this)
                .setTitle(item.category)
                .setMessage("Stored locally on this device.\n" + item.caption)
                .setPositiveButton("Close", null)
                .setNegativeButton("Remove Photo", (dialog, which) -> onPhotoDelete(item))
                .show();
    }

    private void onPhotoDelete(PhotoItem item) {
        allPhotos.remove(item);
        savePhotosToStorage();
        filterPhotos();
        Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show();
    }

    private void savePhotosToStorage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        for (PhotoItem item : allPhotos) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", item.id);
                obj.put("uri", item.uriString);
                obj.put("category", item.category);
                obj.put("caption", item.caption);
                arr.put(obj);
            } catch (JSONException ignored) {}
        }
        prefs.edit().putString(KEY_PHOTOS, arr.toString()).apply();
    }

    private void loadStoredData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String saved = prefs.getString(KEY_PHOTOS, null);
        if (saved != null) {
            try {
                JSONArray arr = new JSONArray(saved);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    allPhotos.add(new PhotoItem(
                            obj.getString("id"),
                            obj.getString("uri"),
                            obj.getString("category"),
                            obj.optString("caption", "")
                    ));
                }
            } catch (JSONException ignored) {}
        }
    }

    // Static Model Class
    public static class PhotoItem {
        public String id;
        public String uriString;
        public String category;
        public String caption;

        public PhotoItem(String id, String uriString, String category, String caption) {
            this.id = id;
            this.uriString = uriString;
            this.category = category;
            this.caption = caption;
        }
    }

    // RecyclerView Adapter
    public static class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
        private final List<PhotoItem> list;
        private final PhotoListener clickListener;
        private final PhotoListener deleteListener;

        public interface PhotoListener {
            void onAction(PhotoItem item);
        }

        public PhotoAdapter(List<PhotoItem> list, PhotoListener clickListener, PhotoListener deleteListener) {
            this.list = list;
            this.clickListener = clickListener;
            this.deleteListener = deleteListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            view.setBackgroundColor(Color.parseColor("#161412"));
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PhotoItem item = list.get(position);
            holder.text1.setText(item.category);
            holder.text1.setTextColor(Color.parseColor("#D4AF37"));
            holder.text2.setText(item.caption != null ? item.caption : "Family photo");
            holder.text2.setTextColor(Color.parseColor("#A8A29E"));
            holder.itemView.setOnClickListener(v -> clickListener.onAction(item));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView text1;
            public TextView text2;

            public ViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
