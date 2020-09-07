import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;
import net.devtech.fixedfluids.api.Participant;
import net.devtech.fixedfluids.api.util.Transaction;
import net.devtech.fixedfluids.api.util.Util;
import net.devtech.fixedfluids.impl.AbstractFilledBucketItemParticipant;

import net.minecraft.Bootstrap;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class TestImpl {
	public static void main(String[] args) {
		Bootstrap.initialize();
		Recording recording = new Recording();
		AbstractFilledBucketItemParticipant filled = new AbstractFilledBucketItemParticipant.Bucket(recording, new ItemStack(Items.WATER_BUCKET, 10), Fluids.WATER);
		long amount = filled.interact(Fluids.WATER, -Util.ONE_BUCKET - 1, false);
		System.out.println(Fluids.WATER + ": " + amount);
		System.out.println(recording.record);

	}

	static final class Recording implements Participant<Object> {
		private final List<Pair<Object, Long>> record = new ArrayList<>();

		@Override
		public long interact(Transaction transaction, Object type, long amount) {
			this.record.add(new Pair<>(type, amount));
			return amount > 0 ? 0 : amount;
		}

		@Override
		public void onAbort(Object data) {}

		@Override
		public void onCommit(Object data) {}
	}

}
