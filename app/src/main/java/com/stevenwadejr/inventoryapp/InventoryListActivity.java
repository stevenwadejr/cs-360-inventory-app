package com.stevenwadejr.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class InventoryListActivity extends AppCompatActivity {

	// Logcat tag
	private static final String TAG = "InventoryList";

	// List of all inventory items
	private List<Item> mItemList;

	// Instance of the app database
	InventoryDatabase inventoryDatabase;

	// View elements
	RecyclerView itemListView;
	TextView emptyListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set this view's transitions
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		setContentView(R.layout.activity_inventory_list);

		// Initialize the database and fetch all inventory items
		inventoryDatabase = InventoryDatabase.getInstance(getApplicationContext());
		mItemList = inventoryDatabase.getItems();

		// Set up the Recycler View, adding dividers between each element
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		itemListView = findViewById(R.id.itemListView);
		itemListView.setLayoutManager(layoutManager);
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(itemListView.getContext(),
				layoutManager.getOrientation());
		itemListView.addItemDecoration(dividerItemDecoration);

		// Find the view when there are no items
		emptyListView = findViewById(R.id.emptyListView);

		// Send items to recycler view
		ItemAdapter adapter = new ItemAdapter(mItemList, this, inventoryDatabase);
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				checkListIsEmpty();
			}
		});

		itemListView.setAdapter(adapter);

		// Check to see if the list is empty - showing the appropriate child view
		checkListIsEmpty();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Show the app bar menu
		getMenuInflater().inflate(R.menu.appbar_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.action_add_item:
				// Switch to the "create new item" view
				Log.d(TAG, "New item view");
				intent = new Intent(getApplicationContext(), EditItemActivity.class);
				startActivity(intent);
				return true;

			case R.id.action_toggle_notifications:
				// Switch to the notifications setting screen
				Log.d(TAG, "SMS Notifications view");
				intent = new Intent(getApplicationContext(), SmsNotifcationsActivity.class);
				startActivity(intent);
				return true;

			case R.id.action_logout:
				// Log the user out by returning to the login screen
				Log.d(TAG, "Logging out");
				intent = new Intent(getApplicationContext(), LoginActivity.class);
				startActivity(intent);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Set the child views to visible or hidden depending on if there are items in the list or not
	 */
	public void checkListIsEmpty() {
		Log.d(TAG, "Inventory size: " + mItemList.size());
		if (mItemList.isEmpty()) {
			itemListView.setVisibility(View.GONE);
			emptyListView.setVisibility(View.VISIBLE);
		} else {
			itemListView.setVisibility(View.VISIBLE);
			emptyListView.setVisibility(View.GONE);
		}
	}
}
