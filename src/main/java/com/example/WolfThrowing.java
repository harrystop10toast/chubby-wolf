package com.example;

import net.minecraft.world.phys.Vec3;

public final class WolfThrowing {
	public static final int MAX_CHARGE_TICKS = 80;

	private WolfThrowing() {
	}

	public static Vec3 velocity(Vec3 look, int chargeTicks) {
		float charge = Math.clamp(chargeTicks, 0, MAX_CHARGE_TICKS) / (float) MAX_CHARGE_TICKS;
		double horizontalSpeed = 0.7 + charge;
		double upwardSpeed = 0.35 + charge * 0.65;
		return look.scale(horizontalSpeed).add(0.0, upwardSpeed, 0.0);
	}
}
