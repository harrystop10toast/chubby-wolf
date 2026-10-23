package com.example.mixin.client;

import com.example.FatDogs;
import com.example.client.ChubbyWolfState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WolfRenderer.class)
public abstract class WolfRendererMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void chubbyDog$extractState(Wolf wolf, WolfRenderState state, float partialTick, CallbackInfo ci) {
		((ChubbyWolfState) state).chubbyDog$setChubby(wolf.getAttachedOrElse(FatDogs.CHUBBY, false));
	}

	@Inject(method = "getTextureLocation", at = @At("RETURN"), cancellable = true)
	private void chubbyDog$useChubbyTexture(WolfRenderState state, CallbackInfoReturnable<Identifier> cir) {
		if (!((ChubbyWolfState) state).chubbyDog$isChubby()) {
			return;
		}

		Identifier texture = cir.getReturnValue();
		Identifier chubbyTexture = FatDogs.id(texture.getPath());
		if (Minecraft.getInstance().getResourceManager().getResource(chubbyTexture).isPresent()) {
			cir.setReturnValue(chubbyTexture);
		}
	}
}
