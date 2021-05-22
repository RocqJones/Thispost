package com.intoverflown.pos.ui.inventory.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.intoverflown.pos.databinding.FragmentProductBinding;
import com.intoverflown.pos.patterns.MySingleton;
import com.intoverflown.pos.ui.inventory.adapters.AdapterProducts;
import com.intoverflown.pos.ui.inventory.postdata.AddProductActivity;
import com.intoverflown.pos.ui.inventory.data.InventoryRemoteData;
import com.intoverflown.pos.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class ProductFragment extends Fragment {

    private FragmentProductBinding binding;

    String token;
    String productId;
    String merchantId;
    String categoryId;
    String supplierId;
    public SharedPreferences preferences;
    public String SHARED_PREF_NAME = "pos_pref";
    public String KEY_TOKEN = "Token";
    public String MERCHANT_ID = "merchantId";
    public String CATEGORY_ID = "categoryId";
    public String SUPPLIER_ID = "supplierId";
    public String PRODUCT_ID = "createProductId";

    String categoryName;
    String supplierName;

    List<InventoryRemoteData> productData;
    private AdapterProducts adapterProducts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProductBinding.inflate(inflater, container, false);

        binding.fab.setOnClickListener(v -> {
            Intent i = new Intent(ProductFragment.this.getContext(), AddProductActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        // prepare root rv layout manager
        productData = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        binding.productRecycler.setLayoutManager(linearLayoutManager);

        preferences = this.getContext().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        token = preferences.getString(KEY_TOKEN, "Token");
        productId = preferences.getString(PRODUCT_ID, "createProductId");
        categoryId = preferences.getString(CATEGORY_ID, "categoryId");
        supplierId = preferences.getString(SUPPLIER_ID, "supplierId");
        merchantId = preferences.getString(MERCHANT_ID, "merchantId");

        String url = Constants.BASE_URL + "Product?Id=";
        getProductName(url);

        return binding.getRoot();
    }

    private void getProductName(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url + productId + "&CategoryId=" + categoryId + "&SupplierId=" + supplierId + "&MerchantId=" + merchantId,
                null, response -> {
            Log.d("response products", response.toString());
            try {


                JSONArray jsonArray1 = response.getJSONArray("productCategory");
                for (int i = 0; i < jsonArray1.length(); i++) {
                    JSONObject jsonObject1 = (JSONObject) jsonArray1.get(i);
                    categoryName = jsonObject1.optString("name");
                }

                JSONArray jsonArray2 = response.getJSONArray("supplier");
                for (int j = 0; j < jsonArray2.length(); j++) {
                    JSONObject jsonObject2 = (JSONObject) jsonArray2.get(j);
                    supplierName = jsonObject2.optString("name");
                }

                JSONArray jsonArray3 = response.getJSONArray("product");
                for (int k = 0; k < jsonArray3.length(); k++) {
                    JSONObject jsonObject3 = (JSONObject) jsonArray3.get(k);
                    InventoryRemoteData inventoryRemoteData = new InventoryRemoteData();
                    inventoryRemoteData.setName(jsonObject3.optString("name"));
                    inventoryRemoteData.setUnits(jsonObject3.optString("unitOfMeasure"));
                    inventoryRemoteData.setStatusId(jsonObject3.optString("statusID"));
                    inventoryRemoteData.setReOrderLevel(jsonObject3.optString("reOrderLevel"));
                    inventoryRemoteData.setCategory(categoryName);
                    inventoryRemoteData.setSupplier(supplierName);
                    inventoryRemoteData.setRemarks(jsonObject3.optString("remarks"));
                    productData.add(inventoryRemoteData);
                }

                adapterProducts = new AdapterProducts(productData, this.getContext());
                binding.productRecycler.setAdapter(adapterProducts);
            } catch (Exception e){
                e.printStackTrace();
            }

        }, Throwable::printStackTrace) {
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
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(this.getContext()).addToRequestQueue(jsonObjectRequest);
    }
}