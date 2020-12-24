package net.devtech.snt.v0.api.filter;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Iterables;
import net.devtech.snt.v0.api.participants.Insertable;
import org.jetbrains.annotations.Nullable;

public interface FilteredInsertable {
	/**
	 * If you want to quickly insert items into an Insertable, this method will help find the optimum set of objects to
	 * iterate through. Keep in mind the passed objects are not guaranteed to be present in `self`
	 * @param self the objects present in the current inventory
	 * @param insertable the insertable
	 * @return if the insertable implements ObjFilter, it will return its iterable instead
	 */
	static <T> Iterable<T> canInsert(Class<T> type, Collection<T> self, Insertable insertable) {
		if (insertable instanceof FilteredInsertable) {
			Set<Object> objects = ((FilteredInsertable) insertable).getSupported();
			if (objects != null) {
				if (self.size() > objects.size()) {
					return Iterables.filter(objects, type);
				} else {
					return Iterables.filter(self, objects::contains);
				}
			}
		}

		return self;
	}

	@Nullable Set<Object> getSupported();
}
