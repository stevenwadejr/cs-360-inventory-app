package com.stevenwadejr.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryDatabase extends SQLiteOpenHelper {

	// Logcat tag
	private static final String LOG = "InventoryDatabase";

	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "inventoryApp.db";

	// Singleton of the database
	private static InventoryDatabase sInventoryDatabase;

	/**
	 * Factory method to get the singleton and create a new one if needed
	 *
	 * @param context The app's context
	 * @return Inventory database
	 */
	public static InventoryDatabase getInstance(Context context) {
		Log.i(LOG, "Get instance of database");
		if (sInventoryDatabase == null) {
			sInventoryDatabase = new InventoryDatabase(context);
		}
		return sInventoryDatabase;
	}

	/**
	 * Make this class a singleton by marking the constructor as private
	 *
	 * @param context The app's context
	 */
	private InventoryDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Class representation of the inventory table
	 */
	private static final class InventoryTable {
		private static final String TABLE = "inventory";
		private static final String COL_ID = "_id";
		private static final String COL_NAME = "name";
		private static final String COL_QUANTITY = "quantity";
	}

	/**
	 * Class representation of the user's table
	 */
	private static final class UsersTable {
		private static final String TABLE = "users";
		private static final String COL_ID = "_id";
		private static final String COL_USERNAME = "username";
		private static final String COL_PASSWORD = "password";
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(LOG, "Create database");
		db.execSQL("CREATE TABLE " + InventoryTable.TABLE + " (" +
				InventoryTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				InventoryTable.COL_NAME + " TEXT, " +
				InventoryTable.COL_QUANTITY + " INTEGER)");
		db.execSQL("CREATE TABLE " + UsersTable.TABLE + " (" +
				UsersTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				UsersTable.COL_USERNAME + " TEXT, " +
				UsersTable.COL_PASSWORD + " TEXT)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + UsersTable.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + InventoryTable.TABLE);
		onCreate(db);
	}

	/**
	 * Get all inventory items
	 *
	 * @return List of inventory items
	 */
	public List<Item> getItems() {
		List<Item> items = new ArrayList<Item>();
		SQLiteDatabase db = getReadableDatabase();

		String sql = "SELECT * FROM " + InventoryTable.TABLE;
		Cursor cursor = db.rawQuery(sql, new String[]{});
		if (cursor.moveToFirst()) {
			do {
				long id = cursor.getLong(0);
				String name = cursor.getString(1);
				int quantity = cursor.getInt(2);
				items.add(new Item(id, name, quantity));
			} while (cursor.moveToNext());
		}
		cursor.close();

		return items;
	}

	/**
	 * Create a new user - failing if the user already exists in the database.
	 *
	 * @param username The username of the user to create
	 * @param password The hashed password of the user to create
	 * @return `true` if user was created, `false` otherwise
	 */
	public boolean addUser(String username, String password) {
		// Guard against registering an existing user
		if (usernameExists(username)) {
			return false;
		}

		// Fetch an instance of database to write to
		SQLiteDatabase db = this.getWritableDatabase();

		// Set the username and password values to insert into their columns
		ContentValues values = new ContentValues();
		values.put(UsersTable.COL_USERNAME, username);
		values.put(UsersTable.COL_PASSWORD, password);

		// Insert row
		long userId = db.insert(UsersTable.TABLE, null, values);

		// Check if there was a user ID returned to indicate success.
		return userId != -1;
	}

	/**
	 * Check the given user's credentials against the database to see if they are correct.
	 *
	 * @param username The given username to check
	 * @param password The given hashed password to check
	 * @return `true` for a user found, `false` otherwise
	 */
	public boolean checkUser(String username, String password) {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "SELECT * FROM " + UsersTable.TABLE + " WHERE username = ? AND password = ?";

		Cursor cursor = db.rawQuery(sql, new String[]{username, password});

		return cursor.getCount() > 0;
	}

	/**
	 * Check to see if a user with the given username already exists in the database.
	 *
	 * @param username The given username to check
	 * @return `true` if a user with that username exists, `false` otherwise
	 */
	public boolean usernameExists(String username) {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "SELECT * FROM " + UsersTable.TABLE + " WHERE username = ?";

		Cursor cursor = db.rawQuery(sql, new String[]{username});

		return cursor.getCount() > 0;
	}

	/**
	 * Add an item to the database
	 *
	 * @param name     The name of the item
	 * @param quantity The quantity of the item
	 * @return Whether the item was successfully inserted into the database or not
	 */
	public boolean addItem(String name, int quantity) {
		// Get an instance of the writable database
		SQLiteDatabase db = this.getWritableDatabase();

		// Set the values according to their columns
		ContentValues values = new ContentValues();
		values.put(InventoryTable.COL_NAME, name);
		values.put(InventoryTable.COL_QUANTITY, quantity);

		// Insert row
		long itemId = db.insert(InventoryTable.TABLE, null, values);

		return itemId != -1;
	}

	/**
	 * Update an existing item
	 *
	 * @param item The item to update
	 * @return Whether the item was successfully updated or not
	 */
	public boolean updateItem(Item item) {
		// Get an instance of the writable database
		SQLiteDatabase db = this.getWritableDatabase();

		// Set the values according to their columns
		ContentValues values = new ContentValues();
		values.put(InventoryTable.COL_NAME, item.getName());
		values.put(InventoryTable.COL_QUANTITY, item.getQuantity());

		// Update the item and check that it successfully updated
		int rowsUpdated = db.update(InventoryTable.TABLE, values, InventoryTable.COL_ID + " = ?",
				new String[]{String.valueOf(item.getId())});

		return rowsUpdated > 0;
	}

	/**
	 * Delete an item from the database
	 *
	 * @param item The item to delete
	 * @return Whether the item was successfully deleted or not
	 */
	public boolean deleteItem(Item item) {
		// Get a writeable instance of the database
		SQLiteDatabase db = getWritableDatabase();

		// Delete the item matching the given item's ID
		int rowsDeleted = db.delete(InventoryTable.TABLE, InventoryTable.COL_ID + " = ?",
				new String[]{String.valueOf(item.getId())});

		// Check that the row was removed from the database
		return rowsDeleted > 0;
	}
}
