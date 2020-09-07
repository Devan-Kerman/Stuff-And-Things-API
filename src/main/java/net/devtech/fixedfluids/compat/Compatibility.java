package net.devtech.fixedfluids.compat;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

// todo add item compatibility with Inventories
public class Compatibility implements IMixinConfigPlugin {
	public static final boolean LBA = FabricLoader.getInstance()
	                                              .isModLoaded("libblockattributes");
	public static final boolean FLUIDITY = FabricLoader.getInstance()
	                                                   .isModLoaded("fluidity");

	@Override
	public void onLoad(String mixinPackage) {}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return mixinClassName.contains("lba_compat") && LBA || !mixinClassName.contains("lba_compat") && (!mixinClassName.contains("fluidity_compat") || FLUIDITY);
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

	@Override
	public List<String> getMixins() {return null;}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
