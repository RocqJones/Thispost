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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.intoverflown.pos.databinding.SupplierFragmentBinding;
import com.intoverflown.pos.patterns.MySingleton;
import com.intoverflown.pos.ui.inventory.addsupplier.AddSupplierActivity;
import com.intoverflown.pos.utils.Constants;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class SupplierFragment extends Fragment {

    SupplierFragmentBinding binding;

    String token;
    Integer merchantId;

    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;
    public String SHARED_PREF_NAME = "pos_pref";
    public String KEY_TOKEN = "Token";
    public String MERCHANT_ID = "merchantId";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = SupplierFragmentBinding.inflate(inflater, container, false);

        binding.fab.setOnClickListener(v -> {
            Intent i = new Intent(SupplierFragment.this.getContext(), AddSupplierActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        preferences = this.getContext().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        token = preferences.getString(KEY_TOKEN, "Token");
        merchantId = Integer.valueOf(preferences.getString(MERCHANT_ID, "merchantId"));

        String url = Constants.BASE_URL + "Supplier/GetSupplierList?MerchantId=";
        getSupplier(url);

        return binding.getRoot();
    }

    private void getSupplier(String url) {
        StringRequest stringRequest  = new StringRequest(Request.Method.GET,
                url + merchantId, response -> Log.d("response supplier", response), new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
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
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(this.getContext()).addToRequestQueue(stringRequest);
    }
}