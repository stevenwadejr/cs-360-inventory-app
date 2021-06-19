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
import android.widget.Toast;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {

	// Instance of the database
	InventoryDatabase inventoryDatabase;

	// Cached view elements
	EditText usernameInput;
	EditText passwordInput;

	Button loginBtn;
	Button registerBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Get the singleton instance of the app database
		inventoryDatabase = InventoryDatabase.getInstance(this);

		// Cache the view elements
		usernameInput = findViewById(R.id.usernameInput);
		passwordInput = findViewById(R.id.passwordInput);

		loginBtn = findViewById(R.id.loginBtn);
		registerBtn = findViewById(R.id.registerBtn);

		// Disable both buttons by default
		loginBtn.setEnabled(false);
		registerBtn.setEnabled(false);

		// Listen to any text changes on these fields
		usernameInput.addTextChangedListener(textWatcher);
		passwordInput.addTextChangedListener(textWatcher);
	}

	/**
	 * Watch for text changes and enable the login and register buttons when the username
	 * and password fields have text in them, otherwise disable the buttons.
	 */
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

	/**
	 * Attempt to log the user in
	 *
	 * @param view The associated view for this controller
	 */
	public void login(View view) {
		// Validate the credentials and make sure they're not empty - otherwise show an error
		if (!validCredentials()) {
			showError(view.getContext().getResources().getString(R.string.invalid_login));
			return;
		}

		try {
			// Log the user in
			boolean isLoggedIn = inventoryDatabase.checkUser(getUsername(), hash(getPassword()));

			// Navigate to the inventory list if logged in, otherwise, show an error
			if (isLoggedIn) {
				handleLoggedInUser();
			} else {
				showError(view.getContext().getResources().getString(R.string.invalid_login));
			}
		} catch (Exception e) {
			showError(view.getContext().getResources().getString(R.string.invalid_login));
		}
	}

	/**
	 * Register a new user
	 *
	 * @param view The view for this controller
	 */
	public void register(View view) {
		// Make sure the given credentials are valid (not empty) first
		if (!validCredentials()) {
			showError(view.getContext().getResources().getString(R.string.registration_error));
		}

		try {
			// Try to create a user
			boolean userCreated = inventoryDatabase.addUser(getUsername(), hash(getPassword()));

			// If the user was created, navigate to the inventory list, otherwise, show an error.
			if (userCreated) {
				handleLoggedInUser();
			} else {
				showError(view.getContext().getResources().getString(R.string.registration_error));
			}
		} catch (Exception e) {
			showError(view.getContext().getResources().getString(R.string.registration_error));
		}
	}

	/**
	 * Navigate to the inventory list screen
	 */
	private void handleLoggedInUser() {
		Intent intent = new Intent(getApplicationContext(), InventoryListActivity.class);
		startActivity(intent);
	}

	/**
	 * Ensure the current credentials aren't invalid (empty)
	 *
	 * @return Whether the credentials are valid or not
	 */
	private boolean validCredentials() {
		return !getUsername().isEmpty() && !getPassword().isEmpty();
	}

	/**
	 * Get the username field input text
	 *
	 * @return The text from the username field
	 */
	private String getUsername() {
		Editable username = usernameInput.getText();
		return username != null ? username.toString().trim().toLowerCase() : "";
	}

	/**
	 * Get the password field input text
	 *
	 * @return The text from the password field
	 */
	private String getPassword() {
		Editable password = passwordInput.getText();
		return password != null ? password.toString().trim() : "";
	}

	/**
	 * Hash the given password string using MD5
	 *
	 * @param password The given plain text password to hash
	 * @return A hashed password as a string
	 * @throws Exception If something went wrong, bail
	 */
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

	/**
	 * Helper function to show a Toast error
	 *
	 * @param errorMessage The error message to show
	 */
	private void showError(String errorMessage) {
		Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, -200);
		toast.show();
	}
}
