package net.devtech.snt.v0.api.participants;

import net.devtech.snt.v0.api.transactions.Transaction;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;

/**
 * @see Fluids
 * @see Items
 */
public interface Extractable extends Participant {
	/**
	 * @return true if the extractable interface accepts objects of that type
	 */
	boolean canExtract(Class<?> type);

	/**
	 * take an amount of something from the extractable interface
	 * @param object the thing to extract
	 * @param amount the amount to extract
	 * @return the amount actually extracted
	 */
	int take(Transaction transaction, Object object, int amount);


	interface Fluids extends Extractable {
		@Override
		default boolean canExtract(Class<?> type) {
			return Fluid.class.isAssignableFrom(type);
		}

		@Override
		default int take(Transaction transaction, Object object, int amount) {
			if(object instanceof Fluid) {
				return this.take(transaction, (Fluid) object, amount);
			}
			return 0;
		}

		int take(Transaction transaction, Fluid fluid, int amount);
	}

	interface Items extends Extractable {
		@Override
		default boolean canExtract(Class<?> type) {
			return Item.class.isAssignableFrom(type);
		}

		@Override
		default int take(Transaction transaction, Object object, int amount) {
			if(object instanceof Item) {
				return this.take(transaction, (Item) object, amount);
			}
			return 0;
		}

		int take(Transaction transaction, Item fluid, int amount);
	}
}

