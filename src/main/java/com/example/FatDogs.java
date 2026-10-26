package com.example;

import com.example.entity.ChubbyDog;
import com.example.mixin.WolfVariantAccessor;
import com.example.network.ThrowWolfPayload;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FatDogs implements ModInitializer {
	public static final String MOD_ID = "chubby-dog";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final AttachmentType<Boolean> CHUBBY = AttachmentRegistry.create(
			id("chubby"),
			builder -> builder
					.persistent(Codec.BOOL)
					.syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.all())
	);
	public static final AttachmentType<String> CARRIED_BY = AttachmentRegistry.create(
			id("carried_by"),
			builder -> builder
					.persistent(Codec.STRING)
					.syncWith(ByteBufCodecs.STRING_UTF8, AttachmentSyncPredicate.all())
	);
	public static final AttachmentType<Integer> CARRIED_DOG_ID = AttachmentRegistry.create(
			id("carried_dog_id"),
			builder -> builder.syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.all())
	);
	public static final AttachmentType<Boolean> THROWN = AttachmentRegistry.create(
			id("thrown"),
			builder -> builder
					.persistent(Codec.BOOL)
					.syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.all())
	);
	public static final EntityType<ChubbyDog> CHUBBY_DOG = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			id("chubby_dog"),
			EntityType.Builder.of(ChubbyDog::new, MobCategory.CREATURE)
					.sized(0.6F, 0.85F)
					.build(ResourceKey.create(Registries.ENTITY_TYPE, id("chubby_dog")))
	);
	public static ChubbyDogConfig CONFIG;

	@Override
	public void onInitialize() {
		CONFIG = ChubbyDogConfig.load();
		FabricDefaultAttributeRegistry.register(CHUBBY_DOG, Wolf.createAttributes().build());
		PayloadTypeRegistry.serverboundPlay().register(ThrowWolfPayload.TYPE, ThrowWolfPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ThrowWolfPayload.TYPE, (payload, context) -> {
			Player player = context.player();
			ChubbyDog dog = carriedDog(player);
			LOGGER.info("[ChubbyDog Debug] Server received throw packet: player={}, chargeTicks={}, carriedDog={}",
					player.getName().getString(), payload.chargeTicks(), dog == null ? "none" : dog.getId());
			if (dog == null || !dog.getAttachedOrElse(CHUBBY, false)
					|| !dog.isOwnedBy(player)) {
				LOGGER.warn("[ChubbyDog Debug] Throw rejected: no carried dog, dog not chubby, or dog is not owned by player={}",
						player.getName().getString());
				return;
			}

			Vec3 velocity = WolfThrowing.velocity(player.getLookAngle(), payload.chargeTicks());
			player.setAttached(CARRIED_DOG_ID, 0);
			dog.setAttached(CARRIED_BY, "");
			dog.setNoAi(false);
			dog.setNoGravity(false);
			dog.setOnGround(false);
			dog.setAttached(THROWN, true);
			dog.setDeltaMovement(velocity);
			LOGGER.info("[ChubbyDog Debug] Throw launched: dog={}, velocity={}", dog.getId(), velocity);
		});
	}

	public static ChubbyDog carriedDog(Player player) {
		int entityId = player.getAttachedOrElse(CARRIED_DOG_ID, 0);
		return player.level().getEntity(entityId) instanceof ChubbyDog dog ? dog : null;
	}

	public static void carryDog(Player player, ChubbyDog dog) {
		if (carriedDog(player) != null) {
			LOGGER.warn("[ChubbyDog Debug] Pickup rejected: player={} is already carrying dogId={}",
					player.getName().getString(), carriedDog(player).getId());
			return;
		}

		dog.stopRiding();
		dog.setAttached(CARRIED_BY, player.getUUID().toString());
		dog.setNoAi(true);
		dog.setNoGravity(true);
		dog.setDeltaMovement(Vec3.ZERO);
		player.setAttached(CARRIED_DOG_ID, dog.getId());
		LOGGER.info("[ChubbyDog Debug] Dog marked as carried: dogId={}, player={}, playerEntityId={}",
				dog.getId(), player.getName().getString(), player.getId());
	}

	public static ChubbyDog promoteToChubbyDog(Wolf wolf) {
		if (wolf instanceof ChubbyDog chubbyDog) {
			return chubbyDog;
		}
		if (!wolf.getAttachedOrElse(CHUBBY, false)) {
			return null;
		}

		return wolf.convertTo(CHUBBY_DOG, ConversionParams.single(wolf, true, true),
				EntitySpawnReason.CONVERSION, chubbyDog -> {
					((WolfVariantAccessor) chubbyDog).chubbyDog$setVariant(
							((WolfVariantAccessor) wolf).chubbyDog$getVariant());
					chubbyDog.setAttached(CHUBBY, true);
					chubbyDog.applyChubbyAttributes();
				});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
