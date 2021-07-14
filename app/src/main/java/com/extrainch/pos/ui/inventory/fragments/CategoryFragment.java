package com.extrainch.pos.ui.inventory.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.extrainch.pos.databinding.FragmentProductCategoryBinding;
import com.extrainch.pos.patterns.MySingleton;
import com.extrainch.pos.ui.inventory.adapters.AdapterCategory;
import com.extrainch.pos.ui.inventory.postdata.AddCategoryActivity;
import com.extrainch.pos.ui.inventory.data.InventoryRemoteData;
import com.extrainch.pos.utils.Constants;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class CategoryFragment extends Fragment {

    FragmentProductCategoryBinding binding;

    String token;
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;
    public String SHARED_PREF_NAME = "pos_pref";
    public String KEY_TOKEN = "Token";

    List<InventoryRemoteData> categoryData;
    private AdapterCategory adapterCategory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductCategoryBinding.inflate(inflater, container, false);

        binding.fab.setOnClickListener(v -> intentToAddCategory());

        // prepare data to display on rv
        categoryData = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        binding.categoryRecycler.setLayoutManager(linearLayoutManager);

        preferences = this.getContext().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        token = preferences.getString(KEY_TOKEN, "Token");
        Log.d("category token", token);

        String url = Constants.BASE_URL + "ProductCategory";

        getCategoryData(url);

        return binding.getRoot();
    }

    private void getCategoryData(String url) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
            try {
                Log.d("response", response.toString());
//            Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                ArrayList<String> category = new ArrayList<String>();

                for (int i = 0; i < response.length(); i++) {
                    JSONObject jsonObject = (JSONObject) response.get(i);
                    InventoryRemoteData inventoryRemoteData = new InventoryRemoteData();
                    inventoryRemoteData.setName(jsonObject.optString("name"));
                    inventoryRemoteData.setRemarks(jsonObject.optString("remarks"));

                    String rawDate = jsonObject.optString("dateCreated");
                    String [] d = rawDate.split("T");

                    inventoryRemoteData.setDateCreated(d[0]);
                    categoryData.add(inventoryRemoteData);

                    // write names to arr. The '#' will be used to get sub-string
                    String temp_str = jsonObject.optString("id")+":"+jsonObject.optString("name");
                    category.add(temp_str);
                }

                editor = preferences.edit();
                editor.putString("categoryArr", category.toString());
                editor.apply();

                adapterCategory = new AdapterCategory(categoryData, this.getContext());
                binding.categoryRecycler.setAdapter(adapterCategory);
            } catch (Exception e){
                e.printStackTrace();
            }
        }, error -> {
            error.printStackTrace();
            Toast.makeText(getContext(), "Get failed", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(this.getContext()).addToRequestQueue(jsonArrayRequest);
    }

    private void intentToAddCategory() {
        Intent j = new Intent(CategoryFragment.this.getContext(), AddCategoryActivity.class);
        j.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(j);
    }
}