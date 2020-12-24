package net.devtech.snt.v0.api.participants;

import net.devtech.snt.v0.api.transactions.Transaction;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;

/**
 * @see Fluids
 * @see Items
 */
public interface Insertable extends Participant {
	/**
	 * @return true if the extractable interface accepts objects of that type
	 */
	boolean canInsert(Class<?> type);

	/**
	 * put an amount of something from the insertable interface
	 * @param object the thing to insert
	 * @param amount the amount to insert
	 * @return the amount leftover
	 */
	int put(Transaction transaction, Object object, int amount);

	interface Fluids extends Insertable {
		@Override
		default boolean canInsert(Class<?> type) {
			return Fluid.class.isAssignableFrom(type);
		}

		@Override
		default int put(Transaction transaction, Object object, int amount) {
			if(object instanceof Fluid) {
				return this.put(transaction, (Fluid) object, amount);
			}
			return amount;
		}

		int put(Transaction transaction, Fluid fluid, int amount);
	}

	interface Items extends Insertable {
		@Override
		default boolean canInsert(Class<?> type) {
			return Item.class.isAssignableFrom(type);
		}

		@Override
		default int put(Transaction transaction, Object object, int amount) {
			if(object instanceof Item) {
				return this.put(transaction, (Item) object, amount);
			}
			return amount;
		}

		int put(Transaction transaction, Item fluid, int amount);
	}
}
