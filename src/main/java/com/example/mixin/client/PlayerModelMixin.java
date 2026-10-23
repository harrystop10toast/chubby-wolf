package com.example.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import com.example.FatDogs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
	@Inject(method = "setupAnim", at = @At("TAIL"))
	private void chubbyDog$raiseArmsWhileCarrying(AvatarRenderState state, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			return;
		}

		Entity entity = client.level.getEntity(state.id);
		if (!(entity instanceof Player player) || FatDogs.carriedDog(player) == null) {
			return;
		}

		float sway = (float) Math.sin(state.ageInTicks * 0.12F) * 0.035F;
		PlayerModel model = (PlayerModel) (Object) this;
		model.rightArm.xRot = model.leftArm.xRot = -0.35F + sway;
		model.rightArm.yRot = model.leftArm.yRot = 0.0F;
		model.rightArm.zRot = 2.65F + sway;
		model.leftArm.zRot = -2.65F - sway;
		model.rightSleeve.xRot = model.rightArm.xRot;
		model.rightSleeve.yRot = model.rightArm.yRot;
		model.rightSleeve.zRot = model.rightArm.zRot;
		model.leftSleeve.xRot = model.leftArm.xRot;
		model.leftSleeve.yRot = model.leftArm.yRot;
		model.leftSleeve.zRot = model.leftArm.zRot;
	}
}
