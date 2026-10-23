package com.example;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public record ChubbyDogConfig(float explosionPower, boolean destroyBlocks) {
	private static final float DEFAULT_EXPLOSION_POWER = 4.0F;
	private static final float MIN_EXPLOSION_POWER = 0.1F;
	private static final float MAX_EXPLOSION_POWER = 100.0F;

	public static ChubbyDogConfig load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve("chubby-dog.properties");
		Properties properties = new Properties();
		try {
			Files.createDirectories(path.getParent());
			if (Files.exists(path)) {
				try (Reader reader = Files.newBufferedReader(path)) {
					properties.load(reader);
				}
			}
		} catch (IOException exception) {
			throw new IllegalStateException("Could not read Chubby Dog config at " + path, exception);
		}

		float power = DEFAULT_EXPLOSION_POWER;
		String rawPower = properties.getProperty("explosion_power");
		if (rawPower != null) {
			try {
				float parsedPower = Float.parseFloat(rawPower.trim());
				if (Float.isFinite(parsedPower)
						&& parsedPower >= MIN_EXPLOSION_POWER
						&& parsedPower <= MAX_EXPLOSION_POWER) {
					power = parsedPower;
				} else {
					FatDogs.LOGGER.warn("[ChubbyDog] explosion_power must be between {} and {}; using default {}",
							MIN_EXPLOSION_POWER, MAX_EXPLOSION_POWER, DEFAULT_EXPLOSION_POWER);
				}
			} catch (NumberFormatException exception) {
				FatDogs.LOGGER.warn("[ChubbyDog] Invalid explosion_power '{}'; using default {}",
						rawPower, DEFAULT_EXPLOSION_POWER);
			}
		}

		boolean destroyBlocks = true;
		String rawDestroyBlocks = properties.getProperty("destroy_blocks");
		if (rawDestroyBlocks != null) {
			if (rawDestroyBlocks.trim().equalsIgnoreCase("true")) {
				destroyBlocks = true;
			} else if (rawDestroyBlocks.trim().equalsIgnoreCase("false")) {
				destroyBlocks = false;
			} else {
				FatDogs.LOGGER.warn("[ChubbyDog] Invalid destroy_blocks '{}'; using default true",
						rawDestroyBlocks);
			}
		}

		properties.setProperty("explosion_power", Float.toString(power));
		properties.setProperty("destroy_blocks", Boolean.toString(destroyBlocks));
		try (Writer writer = Files.newBufferedWriter(path)) {
			properties.store(writer,
					"Thrown chubby dog explosion. Power 4.0 is approximately TNT strength; allowed range: 0.1-100.0.");
		} catch (IOException exception) {
			throw new IllegalStateException("Could not write Chubby Dog config at " + path, exception);
		}

		FatDogs.LOGGER.info("[ChubbyDog] Loaded config: explosion_power={}, destroy_blocks={}",
				power, destroyBlocks);
		return new ChubbyDogConfig(power, destroyBlocks);
	}
}
