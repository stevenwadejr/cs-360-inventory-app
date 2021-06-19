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

	private static final String TAG = "ItemAdapter";

	private List<Item> mItems;
	private Context mCtx;

	InventoryDatabase inventoryDatabase;

	public ItemAdapter(List<Item> items, Context ctx, InventoryDatabase inventoryDb) {
		mItems = items;
		mCtx = ctx;
		inventoryDatabase = inventoryDb;
	}

	@Override
	public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_item, parent, false);
		return new ItemHolder(view, inventoryDatabase);
	}

	@Override
	public void onBindViewHolder(ItemHolder holder, int position) {
		Item item = mItems.get(position);
		holder.bind(item);

		holder.mItemActionsBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupMenu popup = new PopupMenu(mCtx, holder.mItemActionsBtn);
				//inflating menu from xml resource
				popup.inflate(R.menu.inventory_item_actions_menu);
				//adding click listener
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem menuItem) {
						switch (menuItem.getItemId()) {
							case R.id.menu_edit:
								Log.i(TAG, "edit item at position " + position);

								Intent intent = new Intent(mCtx, EditItemActivity.class);
								intent.putExtra(EditItemActivity.EXTRA_ITEM_ID, item);
								mCtx.startActivity(intent);

								return true;
							case R.id.menu_remove:
								Log.i(TAG, "remove item at position " + position);

								new AlertDialog.Builder(mCtx).setIcon(android.R.drawable.ic_dialog_alert)
										.setTitle(R.string.delete_confirmation_title).setMessage(R.string.delete_confirmation)
										.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												boolean deleted = inventoryDatabase.deleteItem(item);
												if (deleted) {
													mItems.remove(position);
													notifyItemRemoved(position);
													notifyDataSetChanged();
												} else {
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

	class ItemHolder extends RecyclerView.ViewHolder {

		private Item mItem;
		private TextView mNameTextView;
		private EditText mQuantityView;

		InventoryDatabase inventoryDatabase;

		ImageButton mDecreaseQuantityBtnInline;
		ImageButton mIncreaseQuantityBtnInline;
		ImageButton mItemActionsBtn;

		public ItemHolder(View itemView, InventoryDatabase inventoryDb) {
			super(itemView);
			inventoryDatabase = inventoryDb;
			mNameTextView = itemView.findViewById(R.id.itemName);
			mQuantityView = itemView.findViewById(R.id.editQuantity);
			mDecreaseQuantityBtnInline = itemView.findViewById(R.id.decreaseQuantityBtnInline);
			mIncreaseQuantityBtnInline = itemView.findViewById(R.id.increaseQuantityBtnInline);
			mItemActionsBtn = itemView.findViewById(R.id.itemActionsBtn);

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

		public void bind(Item item) {
			mItem = item;
			Log.d("ItemHolder", "Bind item: " + mItem.getName());
			mNameTextView.setText(mItem.getName());
			mQuantityView.setText(String.valueOf(mItem.getQuantity()));
		}

		private int getItemQuantity() {
			String rawValue = mQuantityView.getText().toString().trim();
			int quantity = rawValue.isEmpty() ? 0 : Integer.parseInt(rawValue);

			// Quantity cannot be less than 0
			return Math.max(quantity, 0);
		}
	}
}
