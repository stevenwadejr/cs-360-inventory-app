package com.stevenwadejr.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This activity functions as the "create new" and "edit" inventory items screen.
 */
public class EditItemActivity extends AppCompatActivity {

	// The name of the key to use when sending an item to another view
	public static final String EXTRA_ITEM = "com.stevenwadejr.inventoryapp.item";

	// Instance of the inventory database
	InventoryDatabase inventoryDatabase;

	// Item name and quantity views
	EditText itemName;
	EditText itemQuantity;

	// Action buttons
	Button saveBtn;
	Button deleteItemBtn;

	// The current item being edited. `null` if this is a new item not saved yet.
	private Item mItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set up this view's transitions
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		setContentView(R.layout.activity_edit_item);

		// Set the instance of the database
		inventoryDatabase = InventoryDatabase.getInstance(this);

		// Cache the views
		itemName = findViewById(R.id.editItemName);
		itemQuantity = findViewById(R.id.editQuantity_edit);
		deleteItemBtn = findViewById(R.id.deleteItemBtn);
		saveBtn = findViewById(R.id.saveItem);

		// Set the initial state of the action buttons
		deleteItemBtn.setVisibility(View.GONE);
		saveBtn.setEnabled(false);

		int initialQuantity = 0;

		// See if there was an item passed as serialized data to this view. If so, open it, and set
		// the class item as well as any of the item's values (name, quantity).
		Item item = (Item) getIntent().getSerializableExtra(EXTRA_ITEM);
		if (item != null) {
			mItem = item;
			itemName.setText(item.getName());
			initialQuantity = item.getQuantity();
			deleteItemBtn.setVisibility(View.VISIBLE);
		}

		// Set the initial quantity value based
		itemQuantity.setText(String.valueOf(initialQuantity));

		// Listen to changes to item name or item quantity fields
		itemName.addTextChangedListener(textWatcher);
		itemQuantity.addTextChangedListener(textWatcher);
	}

	/**
	 * Listen for text changes on the item name and quantity fields and set the save button
	 * to enabled or disabled based on whether there is text in the item name field or not.
	 */
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

	/**
	 * Save an item to the database (insert or update)
	 *
	 * @param view Instance of the current view
	 */
	public void handleSaveItem(View view) {
		boolean saved;

		// If we're editing an existing item, update its values and update it in the database.
		if (mItem != null) {
			mItem.setName(getItemName());
			mItem.setQuantity(getItemQuantity());
			saved = inventoryDatabase.updateItem(mItem);
		} else {
			// Create a new item in the database.
			saved = inventoryDatabase.addItem(getItemName(), getItemQuantity());
		}

		// If the item saved in the database successfully, go back to the previous screen, otherwise,
		// show an error message.
		if (saved) {
			NavUtils.navigateUpFromSameTask(this);
		} else {
			Toast.makeText(EditItemActivity.this, R.string.save_error, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Handle deleting the current item if we're editing an existing one.
	 *
	 * @param view Instance of the current view
	 */
	public void handleDeleteItem(View view) {
		// Wrap the delete action in a confirmation dialog (only care about "yes" answers)
		new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.delete_confirmation_title).setMessage(R.string.delete_confirmation)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Delete the item from the database
						boolean deleted = inventoryDatabase.deleteItem(mItem);
						finish();

						// If successfully deleted, go back to the previous screen, otherwise, show
						// an error message.
						if (deleted) {
							NavUtils.navigateUpFromSameTask(EditItemActivity.this);
						} else {
							Toast.makeText(EditItemActivity.this, R.string.delete_error, Toast.LENGTH_SHORT).show();
						}
					}
				}).setNegativeButton("No", null).show();
	}

	/**
	 * Increase the item's quantity by one.
	 *
	 * @param view Instance of the current view
	 */
	public void incrementQuantity(View view) {
		itemQuantity.setText(String.valueOf(getItemQuantity() + 1));
	}

	/**
	 * Decrease the item's quantity by one - stopping at zero.
	 *
	 * @param view Instance of the current view
	 */
	public void decrementQuantity(View view) {
		itemQuantity.setText(String.valueOf(Math.max(0, getItemQuantity() - 1)));
	}

	/**
	 * Helper method to get current item's name from the text field.
	 *
	 * @return The item's name
	 */
	private String getItemName() {
		Editable name = itemName.getText();
		return name != null ? name.toString().trim() : "";
	}

	/**
	 * Helper method to get the current item's quantity by parsing the integer from the text field.
	 *
	 * @return The item's quantity
	 */
	private int getItemQuantity() {
		String rawValue = itemQuantity.getText().toString().trim();
		int quantity = rawValue.isEmpty() ? 0 : Integer.parseInt(rawValue);

		// Quantity cannot be less than 0
		return Math.max(quantity, 0);
	}
}
