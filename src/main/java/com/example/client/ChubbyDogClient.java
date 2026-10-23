package com.example.client;

import com.example.FatDogs;
import com.example.WolfThrowing;
import com.example.network.ThrowWolfPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.world.phys.Vec3;

public class ChubbyDogClient implements ClientModInitializer {
	private static final KeyMapping THROW_WOLF = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.chubby-dog.throw_wolf",
			InputConstants.Type.KEYBOARD,
			InputConstants.KEY_V,
			KeyMapping.Category.GAMEPLAY
	));

	private static int chargeTicks = -1;
	private static boolean wasCarryingChubbyDog;

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(FatDogs.CHUBBY_DOG, WolfRenderer::new);
		LevelRenderEvents.BEFORE_GIZMOS.register(context -> renderTrajectory());
		ClientTickEvents.END_CLIENT_TICK.register(client -> tickCharge(client));
	}

	private static void tickCharge(Minecraft client) {
		if (client.player == null || client.level == null) {
			chargeTicks = -1;
			wasCarryingChubbyDog = false;
			return;
		}

		var carriedDog = FatDogs.carriedDog(client.player);
		boolean carryingChubbyDog = carriedDog != null;
		if (carryingChubbyDog != wasCarryingChubbyDog) {
			FatDogs.LOGGER.info("[ChubbyDog Debug] Client carry state changed: carrying={}, dogId={}",
					carryingChubbyDog, carriedDog == null ? "none" : carriedDog.getId());
			wasCarryingChubbyDog = carryingChubbyDog;
		}
		if (THROW_WOLF.isDown() && carryingChubbyDog) {
			if (chargeTicks < 0) {
				chargeTicks = 0;
				FatDogs.LOGGER.info("[ChubbyDog Debug] V throw charge started: dogId={}", carriedDog.getId());
			} else {
				chargeTicks = Math.min(chargeTicks + 1, WolfThrowing.MAX_CHARGE_TICKS);
			}
			return;
		}

		if (chargeTicks >= 0) {
			if (carryingChubbyDog && ClientPlayNetworking.canSend(ThrowWolfPayload.TYPE)) {
				ClientPlayNetworking.send(new ThrowWolfPayload(chargeTicks));
				FatDogs.LOGGER.info("[ChubbyDog Debug] V released; throw packet sent: dogId={}, chargeTicks={}",
						carriedDog.getId(), chargeTicks);
			} else {
				FatDogs.LOGGER.warn("[ChubbyDog Debug] V released but no throw packet sent: carryingDog={}, canSend={}",
						carryingChubbyDog, ClientPlayNetworking.canSend(ThrowWolfPayload.TYPE));
			}
			chargeTicks = -1;
		}
	}

	private static void renderTrajectory() {
		Minecraft client = Minecraft.getInstance();
		if (chargeTicks < 0 || client.player == null || client.level == null) {
			return;
		}
		var dog = FatDogs.carriedDog(client.player);
		if (dog == null) {
			return;
		}

		Vec3 velocity = WolfThrowing.velocity(client.player.getLookAngle(), chargeTicks);
		Vec3 origin = dog.position().add(0.0, dog.getBbHeight() * 0.5, 0.0);
		SimpleGizmoCollector collector = new SimpleGizmoCollector();
		try (Gizmos.TemporaryCollection ignored = Gizmos.withCollector(collector)) {
			Gizmos.point(origin, 0xFFFFFFFF, 0.10F);
			for (int tick = 2; tick <= 50; tick += 2) {
				Vec3 point = origin.add(velocity.scale(tick)).add(0.0, -0.04 * tick * tick, 0.0);
				if (client.level.getBlockState(BlockPos.containing(point)).isSolid()) {
					break;
				}
				Gizmos.point(point, 0xFFFFFFFF, 0.10F);
			}
		}
		client.levelRenderer.addMainThreadGizmos(collector.drainGizmos());
	}
}
