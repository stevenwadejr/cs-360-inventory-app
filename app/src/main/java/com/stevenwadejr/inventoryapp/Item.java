package com.stevenwadejr.inventoryapp;

import java.io.Serializable;

public class Item implements Serializable {
	private long mId;
	private String mName;
	private int mQuantity;

	public Item() {
	}

	public Item(long id, String name) {
		mId = id;
		mName = name;
		mQuantity = 0;
	}

	public Item(long id, String name, int quantity) {
		mId = id;
		mName = name;
		mQuantity = quantity;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public int getQuantity() {
		return mQuantity;
	}

	public void setQuantity(int mQuantity) {
		this.mQuantity = Math.max(0, mQuantity);
	}

	public void incrementQuantity() { this.mQuantity++; }

	public void decrementQuantity() {
		this.mQuantity = Math.max(0, this.mQuantity - 1);
	}
}
