package com.stevenwadejr.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditItemActivity extends AppCompatActivity {

	public static final String EXTRA_ITEM_ID = "com.stevenwadejr.inventoryapp.item_id";

	InventoryDatabase inventoryDatabase;

	EditText itemName;
	EditText itemQuantity;

	Button saveBtn;
	Button deleteItemBtn;

	Item mItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		setContentView(R.layout.activity_edit_item);

		inventoryDatabase = InventoryDatabase.getInstance(this);

		itemName = findViewById(R.id.editItemName);
		itemQuantity = findViewById(R.id.editQuantity_edit);
		deleteItemBtn = findViewById(R.id.deleteItemBtn);
		deleteItemBtn.setVisibility(View.GONE);
		saveBtn = findViewById(R.id.saveItem);
		saveBtn.setEnabled(false);

		int initialQuantity = 0;
		Item item = (Item) getIntent().getSerializableExtra(EXTRA_ITEM_ID);
		if (item != null) {
			mItem = item;
			itemName.setText(item.getName());
			initialQuantity = item.getQuantity();
			deleteItemBtn.setVisibility(View.VISIBLE);
		}

		itemQuantity.setText(String.valueOf(initialQuantity));
		itemName.addTextChangedListener(textWatcher);
		itemQuantity.addTextChangedListener(textWatcher);
	}

	private final TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		@Override
		public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
			saveBtn.setEnabled(!getItemName().isEmpty());
		}
		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	public void handleSaveItem(View view) {
		boolean saved;
		if (mItem != null) {
			mItem.setName(getItemName());
			mItem.setQuantity(getItemQuantity());
			saved = inventoryDatabase.updateItem(mItem);
		} else {
			saved = inventoryDatabase.addItem(getItemName(), getItemQuantity());
		}

		if (saved) {
			NavUtils.navigateUpFromSameTask(this);
		} else {
			Toast.makeText(EditItemActivity.this, R.string.save_error,Toast.LENGTH_SHORT).show();
		}
	}

	public void handleDeleteItem(View view) {
		new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.delete_confirmation_title).setMessage(R.string.delete_confirmation)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						boolean deleted = inventoryDatabase.deleteItem(mItem);
						finish();
						if (deleted) {
							NavUtils.navigateUpFromSameTask(EditItemActivity.this);
						} else {
							Toast.makeText(EditItemActivity.this, R.string.delete_error,Toast.LENGTH_SHORT).show();
						}
					}
				}).setNegativeButton("No", null).show();
	}

	public void incrementQuantity(View view) {
		itemQuantity.setText(String.valueOf(getItemQuantity() + 1));
	}

	public void decrementQuantity(View view) {
		itemQuantity.setText(String.valueOf(Math.max(0, getItemQuantity() - 1)));
	}

	private String getItemName() {
		Editable name = itemName.getText();
		return name != null ? name.toString().trim() : "";
	}

	private int getItemQuantity() {
		String rawValue = itemQuantity.getText().toString().trim();
		int quantity = rawValue.isEmpty() ? 0 : Integer.parseInt(rawValue);

		// Quantity cannot be less than 0
		return Math.max(quantity, 0);
	}
}
