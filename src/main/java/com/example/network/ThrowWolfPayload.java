package com.example.network;

import com.example.FatDogs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ThrowWolfPayload(int chargeTicks) implements CustomPacketPayload {
	public static final Type<ThrowWolfPayload> TYPE = new Type<>(FatDogs.id("throw_wolf"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ThrowWolfPayload> CODEC =
			StreamCodec.composite(ByteBufCodecs.VAR_INT, ThrowWolfPayload::chargeTicks, ThrowWolfPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
