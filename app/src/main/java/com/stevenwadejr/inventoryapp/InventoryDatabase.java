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

	private static InventoryDatabase sInventoryDatabase;

	public static InventoryDatabase getInstance(Context context) {
		Log.i(LOG, "Get instance of database");
		if (sInventoryDatabase == null) {
			sInventoryDatabase = new InventoryDatabase(context);
		}
		return sInventoryDatabase;
	}

	private InventoryDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	private static final class InventoryTable {

		private static final String TABLE = "inventory";
		private static final String COL_ID = "_id";
		private static final String COL_NAME = "name";
		private static final String COL_QUANTITY = "quantity";
	}

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

	public boolean addUser(String username, String password) {
		// Guard against registering an existing user
		if (usernameExists(username)) {
			return false;
		}

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(UsersTable.COL_USERNAME, username);
		values.put(UsersTable.COL_PASSWORD, password);

		// insert row
		long userId = db.insert(UsersTable.TABLE, null, values);

		return userId == -1 ? false : true;
	}

	public boolean checkUser(String username, String password) {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "SELECT * FROM " + UsersTable.TABLE + " WHERE username = ? AND password = ?";

		Cursor cursor = db.rawQuery(sql, new String[]{username, password});

		return cursor.getCount() > 0;
	}

	public boolean usernameExists(String username) {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "SELECT * FROM " + UsersTable.TABLE + " WHERE username = ?";

		Cursor cursor = db.rawQuery(sql, new String[]{username});

		return cursor.getCount() > 0;
	}

	public boolean addItem(String name, int quantity) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(InventoryTable.COL_NAME, name);
		values.put(InventoryTable.COL_QUANTITY, quantity);

		// insert row
		long itemId = db.insert(InventoryTable.TABLE, null, values);

		return itemId == -1 ? false : true;
	}

	public boolean updateItem(Item item) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(InventoryTable.COL_NAME, item.getName());
		values.put(InventoryTable.COL_QUANTITY, item.getQuantity());

		int rowsUpdated = db.update(InventoryTable.TABLE, values, InventoryTable.COL_ID + " = ?",
				new String[]{String.valueOf(item.getId())});
		return rowsUpdated > 0;
	}

	public boolean deleteItem(Item item) {
		SQLiteDatabase db = getWritableDatabase();
		int rowsDeleted = db.delete(InventoryTable.TABLE, InventoryTable.COL_ID + " = ?",
				new String[]{String.valueOf(item.getId())});
		return rowsDeleted > 0;
	}
}
