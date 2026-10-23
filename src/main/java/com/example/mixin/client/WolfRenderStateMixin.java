package com.example.mixin.client;

import com.example.client.ChubbyWolfState;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WolfRenderState.class)
public abstract class WolfRenderStateMixin implements ChubbyWolfState {
	@Unique
	private boolean chubbyDog$isChubby;

	@Override
	@Unique
	public boolean chubbyDog$isChubby() {
		return chubbyDog$isChubby;
	}

	@Override
	@Unique
	public void chubbyDog$setChubby(boolean chubby) {
		chubbyDog$isChubby = chubby;
	}
}
