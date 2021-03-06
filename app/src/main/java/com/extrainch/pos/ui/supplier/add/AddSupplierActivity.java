package com.extrainch.pos.ui.supplier.add;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.extrainch.pos.R;
import com.extrainch.pos.databinding.ActivityAddSupplierBinding;
import com.extrainch.pos.ui.inventory.InventoryActivity;
import com.extrainch.pos.ui.profile.addmerchant.AddMerchantActivity;
import com.extrainch.pos.ui.supplier.SupplierActivity;
import com.extrainch.pos.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddSupplierActivity extends AppCompatActivity {

    private ActivityAddSupplierBinding binding;

    String uid;
    String token;
    String merchantUid;
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;
    public String SHARED_PREF_NAME = "pos_pref";
    public String KEY_ID = "Id";
    public String KEY_TOKEN = "Token";
    public String MERCHANT_ID = "merchantId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddSupplierBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.addBackBtn.setOnClickListener(v -> {
            Intent i = new Intent(AddSupplierActivity.this, SupplierActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        /* Get uid and token */
        preferences = this.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        uid = preferences.getString(KEY_ID, "Id");
        token = preferences.getString(KEY_TOKEN, "Token");
        if (preferences.contains("merchantId")) {
            merchantUid = preferences.getString(MERCHANT_ID, "merchantId");
        } else {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_warning);
            dialog.setCancelable(false);

            TextView warnMessage = (TextView) dialog.findViewById(R.id.warnMessage);
            Button okBtn = (Button) dialog.findViewById(R.id.okBtn);

            warnMessage.setText("To create supplier you must maintain merchant!");

            okBtn.setOnClickListener(v -> {
                dialog.dismiss();
                Intent mD = new Intent(this, AddMerchantActivity.class);
                startActivity(mD);
            });

            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_1;
            dialog.show();
            dialog.setCanceledOnTouchOutside(true);
        }

        Log.d("uid supplier", uid);
        Log.d("token supplier", token);
        Log.d("uid merchant", merchantUid);

        binding.supplierSaveBtn.setOnClickListener(v -> {
            String url = Constants.BASE_URL + "Supplier/Create";
            createSupplier(url);
        });
    }

    private void createSupplier(String url) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JSONArray array = new JSONArray();
        JSONObject jsonObjects = new JSONObject();

        String supplierName = binding.supplierName.getText().toString().trim();
        String supplierAddress = binding.supplierAddress.getText().toString().trim();
        String supplierPhone = binding.supplierPhone.getText().toString().trim();
        String supplierEmail = binding.supplierEmail.getText().toString().trim();
        String supplierRemarks = binding.supplierRemarks.getText().toString().trim();

        if (supplierName.isEmpty() || supplierAddress.isEmpty() || supplierPhone.isEmpty()
                || supplierEmail.isEmpty() || supplierRemarks.isEmpty()) {
            String empError = "Sorry, Please fill all the required fields!";
            warnDialog(empError);
        } else {
            ProgressDialog progressDialog = new ProgressDialog(AddSupplierActivity.this);
            progressDialog.setMessage("Creating new supplier...");
            progressDialog.show();

            try {
                jsonObjects.put("merchantId", merchantUid);
                jsonObjects.put("name", supplierName);
                jsonObjects.put("address", supplierAddress);
                jsonObjects.put("phone", supplierPhone);
                jsonObjects.put("email", supplierEmail);
                jsonObjects.put("remarks", supplierRemarks);
                jsonObjects.put("createdById", uid);

                array = new JSONArray("["+jsonObjects.toString()+"]");
                Log.d("Post", array.toString());

            } catch (final JSONException e) {
                e.printStackTrace();
            }

            JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(com.android.volley.Request.Method.POST,
                    url, array, response -> {
                try {
                    Log.d("response", response.toString());

                    for(int i=0; i < response.length(); i++) {
                        JSONObject jsonObj = response.getJSONObject(i);
                        Log.d("uid supplier", jsonObj.optString("uniqueId"));

                        // store id
                        editor = preferences.edit();
                        editor.putString("supplierId", jsonObj.optString("uniqueId"));
                        editor.apply();
                    }

                    progressDialog.dismiss();
                    String mg = "Supplier created successfully!";
                    successDialog(mg);
                    proceedToCreateProduct();

                } catch (Exception e) {
                    String err = "Error occurred while sending request\nCheck logs!";
                    warnDialog(err);
                    Log.i("new branch", Log.getStackTraceString(e));
                }
            }, error -> {
                progressDialog.dismiss();
                Log.e("error", error.toString());
                String failed = "Failed because the server was unreachable, check your internet connection!";
                warnDialog(failed);
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    params.put("Authorization", "Bearer " + token);
                    return params;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(jsonObjectRequest);
        }
    }

    private void proceedToCreateProduct() {
        Intent j = new Intent(AddSupplierActivity.this, SupplierActivity.class);
        j.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(j);
        finish();
    }

    private void successDialog(String successM) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_success);
        dialog.setCancelable(false);

        TextView successMessage = (TextView) dialog.findViewById(R.id.successMessage);
        Button okBtn = (Button) dialog.findViewById(R.id.okBtn);

        successMessage.setText(successM);

        okBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_1;
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
    }

    private void warnDialog(String message) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_warning);
        dialog.setCancelable(false);

        TextView warnMessage = (TextView) dialog.findViewById(R.id.warnMessage);
        Button okBtn = (Button) dialog.findViewById(R.id.okBtn);

        warnMessage.setText(message);

        okBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_1;
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
    }
}