package com.example.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Wolf.class)
public interface WolfVariantAccessor {
	@Invoker("getVariant")
	Holder<WolfVariant> chubbyDog$getVariant();

	@Invoker("setVariant")
	void chubbyDog$setVariant(Holder<WolfVariant> variant);
}
