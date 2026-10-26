package com.example.mixin;

import com.example.FatDogs;
import com.example.entity.ChubbyDog;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfFoodMixin {
	@Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
	private void chubbyDog$onFeed(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		Wolf wolf = (Wolf) (Object) this;
		boolean sneaking = player.isShiftKeyDown();
		boolean chubby = wolf.getAttachedOrElse(FatDogs.CHUBBY, false);
		boolean owned = wolf.isOwnedBy(player);
		FatDogs.LOGGER.info(
				"[ChubbyDog Debug] mobInteract reached: side={}, entityType={}, entityId={}, player={}, sneaking={}, chubby={}, owned={}, hand={}",
				wolf.level().isClientSide() ? "client" : "server",
				net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(wolf.getType()),
				wolf.getId(), player.getName().getString(), sneaking, chubby, owned, hand);

		if (sneaking && chubby && owned) {
			if (!wolf.level().isClientSide()) {
				ChubbyDog chubbyDog = FatDogs.promoteToChubbyDog(wolf);
				if (chubbyDog == null) {
					FatDogs.LOGGER.error("[ChubbyDog Debug] Pickup conversion failed: sourceEntityId={}", wolf.getId());
				} else {
					FatDogs.carryDog(player, chubbyDog);
				}
			} else {
				FatDogs.LOGGER.info("[ChubbyDog Debug] Client accepted pickup interaction; server should now process it.");
			}
			cir.setReturnValue(InteractionResult.SUCCESS);
			return;
		}

		if (sneaking) {
			FatDogs.LOGGER.warn("[ChubbyDog Debug] Pickup conditions failed: sneaking={}, chubby={}, owned={}",
					sneaking, chubby, owned);
		}

		ItemStack stack = player.getItemInHand(hand);
		boolean steak = stack.is(Items.COOKED_BEEF) && wolf.isFood(stack);
		boolean canEatNow = !wolf.isTame() || wolf.getHealth() < wolf.getMaxHealth();
		if (!player.isSpectator() && canEatNow && steak) {
			if (!wolf.level().isClientSide() && !chubby && wolf.getRandom().nextFloat() < 0.05F) {
				wolf.setAttached(FatDogs.CHUBBY, true);
			}

			if (!wolf.isTame()) {
				if (!wolf.level().isClientSide()) {
					stack.consume(1, player);
					wolf.heal(2.0F);
					if (wolf.getAttachedOrElse(FatDogs.CHUBBY, false)) {
						FatDogs.promoteToChubbyDog(wolf);
					}
				}
				cir.setReturnValue(InteractionResult.SUCCESS);
			}
		}
	}

	@Inject(method = "mobInteract", at = @At("TAIL"))
	private void chubbyDog$promoteToDistinctEntity(Player player, InteractionHand hand,
			CallbackInfoReturnable<InteractionResult> cir) {
		Wolf wolf = (Wolf) (Object) this;
		if (!wolf.level().isClientSide()
				&& wolf.getAttachedOrElse(FatDogs.CHUBBY, false)) {
			FatDogs.promoteToChubbyDog(wolf);
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void chubbyDog$migrateOldChubbyWolf(CallbackInfo ci) {
		Wolf wolf = (Wolf) (Object) this;
		if (!wolf.level().isClientSide()) {
			FatDogs.promoteToChubbyDog(wolf);
		}
	}
}
