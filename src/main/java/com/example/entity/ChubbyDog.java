package com.example.entity;

import com.example.FatDogs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ChubbyDog extends Wolf {
	private UUID lastLoggedCarrier;

	public ChubbyDog(EntityType<? extends Wolf> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageMultiplier,
			net.minecraft.world.damagesource.DamageSource damageSource) {
		resetFallDistance();
		return false;
	}

	@Override
	public void tick() {
		super.tick();

		String carrierId = getAttachedOrElse(FatDogs.CARRIED_BY, "");
		if (carrierId.isEmpty()) {
			explodeOnImpact();
			return;
		}

		UUID carrierUuid;
		try {
			carrierUuid = UUID.fromString(carrierId);
		} catch (IllegalArgumentException exception) {
			FatDogs.LOGGER.error("[ChubbyDog Debug] Invalid carrier UUID on dogId={}: {}", getId(), carrierId);
			if (!level().isClientSide()) {
				releaseFromCarrier();
			}
			return;
		}

		Player carrier = level().getPlayerByUUID(carrierUuid);
		if (carrier == null) {
			if (!level().isClientSide()) {
				FatDogs.LOGGER.warn("[ChubbyDog Debug] Carrier not found; releasing dogId={}, carrierUuid={}",
						getId(), carrierId);
				releaseFromCarrier();
			}
			return;
		}

		if (!level().isClientSide() && FatDogs.carriedDog(carrier) != this) {
			FatDogs.LOGGER.warn("[ChubbyDog Debug] Player carry reference missing; releasing dogId={}, player={}",
					getId(), carrier.getName().getString());
			releaseFromCarrier();
			return;
		}

		setNoGravity(true);
		setDeltaMovement(Vec3.ZERO);
		setPos(carrier.getX(), carrier.getY() + carrier.getBbHeight() + 0.2, carrier.getZ());
		setYRot(carrier.getYRot());
		setXRot(0.0F);
		if (!carrierUuid.equals(lastLoggedCarrier)) {
			FatDogs.LOGGER.info(
					"[ChubbyDog Debug] Carried dog positioned: side={}, dogId={}, carrier={}, dogPosition={}",
					level().isClientSide() ? "client" : "server", getId(),
					carrier.getName().getString(), position());
			lastLoggedCarrier = carrierUuid;
		}
		explodeOnImpact();
	}

	private void explodeOnImpact() {
		if (level().isClientSide() || !getAttachedOrElse(FatDogs.THROWN, false)
				|| !(onGround() || horizontalCollision || verticalCollision)) {
			return;
		}

		FatDogs.LOGGER.info("[ChubbyDog] Thrown dog impacted at {}; exploding with power {}",
				position(), FatDogs.CONFIG.explosionPower());
		setAttached(FatDogs.THROWN, false);
		level().explode(this, getX(), getY(), getZ(), FatDogs.CONFIG.explosionPower(),
				false, FatDogs.CONFIG.destroyBlocks()
						? Level.ExplosionInteraction.TNT
						: Level.ExplosionInteraction.MOB);
		setDeltaMovement(Vec3.ZERO);
		FatDogs.LOGGER.info("[ChubbyDog] Explosion complete; dogId={} survived.", getId());
	}

	private void releaseFromCarrier() {
		setAttached(FatDogs.CARRIED_BY, "");
		setNoAi(false);
		setNoGravity(false);
		setDeltaMovement(Vec3.ZERO);
		lastLoggedCarrier = null;
	}
}
