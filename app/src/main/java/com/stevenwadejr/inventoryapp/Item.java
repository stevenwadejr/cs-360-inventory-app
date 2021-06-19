package com.stevenwadejr.inventoryapp;

import java.io.Serializable;

public class Item implements Serializable {
	private long mId;
	private String mName;
	private int mQuantity;

	/**
	 * Default constructor
	 */
	public Item() {
	}

	/**
	 * Constructor that takes an ID and a name - defaulting quantity to 0
	 *
	 * @param id   The ID of the item
	 * @param name The name of the item
	 */
	public Item(long id, String name) {
		mId = id;
		mName = name;
		mQuantity = 0;
	}

	/**
	 * Constructor that takes id, name, and quantity
	 *
	 * @param id       The ID of the item
	 * @param name     The name of the item
	 * @param quantity The quantity of the item
	 */
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

	/**
	 * Set the quantity, preventing a negative number
	 *
	 * @param quantity The desiired quantity to set
	 */
	public void setQuantity(int quantity) {
		this.mQuantity = Math.max(0, quantity);
	}

	/**
	 * Increase the item's quantity by one
	 */
	public void incrementQuantity() {
		this.mQuantity++;
	}

	/**
	 * Decrease the item's quantity by one, stopping at zero
	 */
	public void decrementQuantity() {
		this.mQuantity = Math.max(0, this.mQuantity - 1);
	}
}
