package com.stevenwadejr.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {

	InventoryDatabase inventoryDatabase;

	EditText usernameInput;
	EditText passwordInput;

	Button loginBtn;
	Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

		inventoryDatabase = InventoryDatabase.getInstance(this);

		usernameInput = findViewById(R.id.usernameInput);
		passwordInput = findViewById(R.id.passwordInput);

		loginBtn = findViewById(R.id.loginBtn);
		registerBtn = findViewById(R.id.registerBtn);

		loginBtn.setEnabled(false);
		registerBtn.setEnabled(false);

		usernameInput.addTextChangedListener(textWatcher);
		passwordInput.addTextChangedListener(textWatcher);
    }

	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		@Override
		public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
			boolean fieldsAreEmpty = getUsername().isEmpty() || getPassword().isEmpty();
			loginBtn.setEnabled(!fieldsAreEmpty);
			registerBtn.setEnabled(!fieldsAreEmpty);
		}
		@Override
		public void afterTextChanged(Editable s) {
		}
	};

    public void login(View view) {
		if (!validCredentials()) {
			showError(view.getContext().getResources().getString(R.string.invalid_login));
			return;
		}

		try {
			boolean isLoggedIn = inventoryDatabase.checkUser(getUsername(), hash(getPassword()));
			if (isLoggedIn) {
				handleLoggedInUser();
			} else {
				showError(view.getContext().getResources().getString(R.string.invalid_login));
			}
		} catch (Exception e) {
			showError(view.getContext().getResources().getString(R.string.invalid_login));
		}
	}

	public void register(View view) {
		if (!validCredentials()) {
			showError(view.getContext().getResources().getString(R.string.registration_error));
		}

		try {
			boolean userCreated = inventoryDatabase.addUser(getUsername(), hash(getPassword()));
			if (userCreated) {
				handleLoggedInUser();
			} else {
				showError(view.getContext().getResources().getString(R.string.registration_error));
			}
		} catch (Exception e) {
			showError(view.getContext().getResources().getString(R.string.registration_error));
		}
	}

	private void handleLoggedInUser() {
		Intent intent = new Intent(getApplicationContext(), InventoryListActivity.class);
		startActivity(intent);
	}

	private boolean validCredentials() {
    	return !getUsername().isEmpty() && !getPassword().isEmpty();
	}

	private String getUsername() {
		Editable username = usernameInput.getText();
		return username != null ? username.toString().trim().toLowerCase() : "";
	}

	private String getPassword() {
		Editable password = passwordInput.getText();
		return password != null ? password.toString().trim() : "";
	}

	private String hash(String password) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(password.getBytes());
		byte[] digest = md.digest();
		StringBuffer sb = new StringBuffer();
		for (byte b : digest) {
			sb.append(String.format("%02x", b & 0xff));
		}

		return sb.toString();
	}

	private void showError(String errorMessage) {
		Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, -200);
		toast.show();
	}
}
