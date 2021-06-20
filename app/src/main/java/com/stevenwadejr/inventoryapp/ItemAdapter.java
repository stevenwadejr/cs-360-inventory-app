package com.stevenwadejr.inventoryapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemHolder> {

	// Logcat tag
	private static final String TAG = "ItemAdapter";

	// Collection of inventory items in this list/adapter
	private List<Item> mItems;

	// A context that this item adapter is running in
	private Context mCtx;

	// An instance of the app's database
	InventoryDatabase inventoryDatabase;

	/**
	 * Constructor that takes in a list of inventory items, an app context, and an instance
	 * of the inventory database.
	 *
	 * @param items       Inventory items
	 * @param ctx         A given app context
	 * @param inventoryDb An instance of the inventory database
	 */
	public ItemAdapter(List<Item> items, Context ctx, InventoryDatabase inventoryDb) {
		mItems = items;
		mCtx = ctx;
		inventoryDatabase = inventoryDb;
	}

	@Override
	public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// Create an instance of the child view
		final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_item, parent, false);
		return new ItemHolder(view, inventoryDatabase);
	}

	@Override
	public void onBindViewHolder(ItemHolder holder, int position) {
		// Find the inventory item at the current position and bind its data to the item holder view
		Item item = mItems.get(position);
		holder.bind(item);

		// Listen to the click on the child view's "more actions" button
		holder.mItemActionsBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Show a popup menu for this child
				PopupMenu popup = new PopupMenu(mCtx, holder.mItemActionsBtn);
				// Inflating menu from xml resource
				popup.inflate(R.menu.inventory_item_actions_menu);
				// Adding click listener
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem menuItem) {
						switch (menuItem.getItemId()) {
							case R.id.menu_edit:
								// Navigate to the edit screen and pass in the current item
								// as extra data to the destination view.
								Log.i(TAG, "edit item at position " + position);

								Intent intent = new Intent(mCtx, EditItemActivity.class);
								intent.putExtra(EditItemActivity.EXTRA_ITEM, item);
								mCtx.startActivity(intent);

								return true;
							case R.id.menu_remove:
								// Delete the current item from the database and the list
								Log.i(TAG, "remove item at position " + position);

								// Wrap the delete action in a confirmation dialog
								new AlertDialog.Builder(mCtx).setIcon(android.R.drawable.ic_dialog_alert)
										.setTitle(R.string.delete_confirmation_title).setMessage(R.string.delete_confirmation)
										.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												// Delete the item from the database
												boolean deleted = inventoryDatabase.deleteItem(item);
												if (deleted) {
													// Remove the item from the list and broadcast the change
													mItems.remove(position);
													notifyItemRemoved(position);
													notifyDataSetChanged();
												} else {
													// Show an error message to the user
													Toast.makeText(mCtx, R.string.delete_error, Toast.LENGTH_SHORT).show();
												}
											}
										}).setNegativeButton("No", null).show();

								return true;
							default:
								return false;
						}
					}
				});
				//displaying the popup
				popup.show();
			}
		});
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}

	/**
	 * Child view for each inventory item
	 */
	class ItemHolder extends RecyclerView.ViewHolder {

		// Cached view references
		private Item mItem;
		private TextView mNameTextView;
		private EditText mQuantityView;

		// Instance of the database
		InventoryDatabase inventoryDatabase;

		ImageButton mDecreaseQuantityBtnInline;
		ImageButton mIncreaseQuantityBtnInline;
		ImageButton mItemActionsBtn;

		/**
		 * Constructor that requires the view and an instance of the database
		 *
		 * @param itemView    The view associated with this controller
		 * @param inventoryDb Instance of the app database
		 */
		public ItemHolder(View itemView, InventoryDatabase inventoryDb) {
			super(itemView);
			inventoryDatabase = inventoryDb;
			mNameTextView = itemView.findViewById(R.id.itemName);
			mQuantityView = itemView.findViewById(R.id.editQuantity);
			mDecreaseQuantityBtnInline = itemView.findViewById(R.id.decreaseQuantityBtnInline);
			mIncreaseQuantityBtnInline = itemView.findViewById(R.id.increaseQuantityBtnInline);
			mItemActionsBtn = itemView.findViewById(R.id.itemActionsBtn);

			// Update the item's quantity when the decrement button is clicked
			mDecreaseQuantityBtnInline.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mItem.decrementQuantity();
					boolean updated = inventoryDatabase.updateItem(mItem);
					if (updated) {
						mQuantityView.setText(String.valueOf(mItem.getQuantity()));
					}
				}
			});

			// Update the items' quantity when the increment button is clicked
			mIncreaseQuantityBtnInline.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mItem.incrementQuantity();
					boolean updated = inventoryDatabase.updateItem(mItem);
					if (updated) {
						mQuantityView.setText(String.valueOf(mItem.getQuantity()));
					}
				}
			});

			// Listen for changes on the quantity text field, updating the item and database
			mQuantityView.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
					mItem.setQuantity(getItemQuantity());
					boolean updated = inventoryDatabase.updateItem(mItem);
					Log.d(TAG, "Item quantity updated: " + updated);

				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});
		}

		/**
		 * Bind the model to the controller
		 *
		 * @param item Model for this view
		 */
		public void bind(Item item) {
			mItem = item;
			Log.d("ItemHolder", "Bind item: " + mItem.getName());
			mNameTextView.setText(mItem.getName());
			mQuantityView.setText(String.valueOf(mItem.getQuantity()));
		}

		/**
		 * Helper method to convert the quantity from the text input to an integer value
		 *
		 * @return Item's quantity
		 */
		private int getItemQuantity() {
			String rawValue = mQuantityView.getText().toString().replaceAll("[^\\d.]", "").trim();
			int quantity = rawValue.isEmpty() ? 0 : Integer.parseInt(rawValue);

			// Quantity cannot be less than 0
			return Math.max(quantity, 0);
		}
	}
}
