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

	private static final int PERMISSIONS_REQUEST_SEND_SMS = 0;

	List<Item> mItemList;

	InventoryDatabase inventoryDatabase;

	RecyclerView itemListView;
	TextView emptyListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		setContentView(R.layout.activity_inventory_list);

		inventoryDatabase = InventoryDatabase.getInstance(getApplicationContext());
		mItemList = inventoryDatabase.getItems();

		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		itemListView = findViewById(R.id.itemListView);
		itemListView.setLayoutManager(layoutManager);
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(itemListView.getContext(),
				layoutManager.getOrientation());
		itemListView.addItemDecoration(dividerItemDecoration);

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
		checkListIsEmpty();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.appbar_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.action_add_item:
				Log.d(TAG, "New item view");
				intent = new Intent(getApplicationContext(), EditItemActivity.class);
				startActivity(intent);
				return true;

			case R.id.action_toggle_notifications:
				Log.d(TAG, "SMS Notifications view");
				intent = new Intent(getApplicationContext(), SmsNotifcationsActivity.class);
				startActivity(intent);
				return true;

			case R.id.action_logout:
				Log.d(TAG, "Logging out");
				intent = new Intent(getApplicationContext(), LoginActivity.class);
				startActivity(intent);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

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
