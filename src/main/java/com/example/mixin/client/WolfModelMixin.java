package com.example.mixin.client;

import com.example.client.ChubbyWolfState;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfModel.class)
public abstract class WolfModelMixin {
	@Shadow
	@Final
	protected ModelPart body;

	@Inject(method = "setupAnim", at = @At("TAIL"))
	private void chubbyDog$makeBodyChubby(WolfRenderState state, CallbackInfo ci) {
		float bodyScale = ((ChubbyWolfState) state).chubbyDog$isChubby() ? 1.25F : 1.0F;
		body.xScale = bodyScale;
		body.yScale = bodyScale;
		body.zScale = bodyScale;
	}
}
