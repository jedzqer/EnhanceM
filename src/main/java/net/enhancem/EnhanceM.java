package net.enhancem;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnhanceM implements ModInitializer {
	public static final String MOD_ID = "enhancem";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("EnhanceM loaded!");
	}
}