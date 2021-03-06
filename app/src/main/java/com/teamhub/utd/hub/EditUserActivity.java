package com.teamhub.utd.hub;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class EditUserActivity extends AppCompatActivity {

    EditText username, password, role;
    Button save, delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        final User user;
        //Bundle b = this.getIntent().getExtras();
        Intent i = getIntent();
        user = (User) i.getSerializableExtra("User");

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        role = (EditText) findViewById(R.id.role);
        save = (Button) findViewById(R.id.saveButton);
        delete = (Button) findViewById(R.id.deleteButton);

        username.setText(user.getUsername());
        password.setText(user.getPassword());
        role.setText(String.valueOf(user.getRole()));
        final int userID = user.getId();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {
                                Toast.makeText(EditUserActivity.this, "User Updated",
                                        Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(EditUserActivity.this, AdminActivity.class);
                                EditUserActivity.this.startActivity(intent);
                            } else {
                                Toast.makeText(EditUserActivity.this, "User Update failure",
                                        Toast.LENGTH_LONG).show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(EditUserActivity.this);
                                builder.setMessage("User Update Failed")
                                        .setNegativeButton("Retry", null)
                                        .create()
                                        .show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                };

                UpdateUserRequest updateUserRequest = new UpdateUserRequest(userID, username.getText().toString(),
                        password.getText().toString(), Integer.parseInt(role.getText().toString()), responseListener);
                RequestQueue queue = Volley.newRequestQueue(EditUserActivity.this);
                queue.add(updateUserRequest);

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {
                                Toast.makeText(EditUserActivity.this, "User Deleted",
                                        Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(EditUserActivity.this, AdminActivity.class);
                                EditUserActivity.this.startActivity(intent);
                            } else {
                                Toast.makeText(EditUserActivity.this, "User Deleted failure",
                                        Toast.LENGTH_LONG).show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(EditUserActivity.this);
                                builder.setMessage("User Deleted Failed")
                                        .setNegativeButton("Retry", null)
                                        .create()
                                        .show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                };

                RemoveUserRequest removeUserRequest = new RemoveUserRequest(userID, responseListener);
                RequestQueue queue = Volley.newRequestQueue(EditUserActivity.this);
                queue.add(removeUserRequest);
            }
        });
    }
}
