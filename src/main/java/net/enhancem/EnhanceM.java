package net.enhancem;

import net.enhancem.item.RebirthPearl;
import net.enhancem.network.RebirthPearlChannelPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.UUID;

public class EnhanceM implements ModInitializer {
	public static final String MOD_ID = "enhancem";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final ResourceKey<Enchantment> DIVINE_BLESSING_KEY = ResourceKey.create(
			Registries.ENCHANTMENT,
			Identifier.fromNamespaceAndPath("enhancem", "divine_blessing")
	);

	private static final ResourceKey<Item> REBIRTH_PEARL_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MOD_ID, "rebirth_pearl")
	);

	private static final ResourceKey<Item> SPEED_BOOTS_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MOD_ID, "speed_boots")
	);

	public static final SoundEvent DIVINE_BLESSING_SOUND = Registry.register(
			BuiltInRegistries.SOUND_EVENT,
			Identifier.fromNamespaceAndPath(MOD_ID, "divine_blessing"),
			SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MOD_ID, "divine_blessing"))
	);

	public static final Item REBIRTH_PEARL = Registry.register(
			BuiltInRegistries.ITEM,
			REBIRTH_PEARL_KEY,
			new RebirthPearl(new Item.Properties().setId(REBIRTH_PEARL_KEY).stacksTo(16))
	);

	public static final Item SPEED_BOOTS = Registry.register(
			BuiltInRegistries.ITEM,
			SPEED_BOOTS_KEY,
			new Item(new Item.Properties().setId(SPEED_BOOTS_KEY).humanoidArmor(ArmorMaterials.LEATHER, ArmorType.BOOTS))
	);

	@Override
	public void onInitialize() {
		LOGGER.info("EnhanceM loaded!");
		PayloadTypeRegistry.clientboundPlay().register(RebirthPearlChannelPayload.TYPE, RebirthPearlChannelPayload.CODEC);
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> output.accept(REBIRTH_PEARL));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> output.accept(SPEED_BOOTS));
		registerLootModifiers();
		registerRebirthPearlTick();
	}

	private void registerRebirthPearlTick() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			var toRemove = new ArrayList<UUID>();

			RebirthPearl.CHANNELING.forEach((uuid, startTick) -> {
				ServerPlayer player = server.getPlayerList().getPlayer(uuid);
				if (player == null) {
					toRemove.add(uuid);
					return;
				}
				long elapsed = player.level().getGameTime() - startTick;
				if (elapsed >= 100) {
					teleportToSpawn(player, server);
					toRemove.add(uuid);
					ServerPlayNetworking.send(player, new RebirthPearlChannelPayload(false));
					player.sendOverlayMessage(
							Component.translatable("item.enhancem.rebirth_pearl.success"));
				}
			});

			toRemove.forEach(RebirthPearl.CHANNELING::remove);
		});
	}

	private static void teleportToSpawn(ServerPlayer player, MinecraftServer server) {
		ServerLevel overworld = server.overworld();
		var spawnPos = overworld.getRespawnData().pos();
		player.teleport(new TeleportTransition(
				overworld,
				Vec3.atBottomCenterOf(spawnPos),
				Vec3.ZERO,
				player.getYRot(),
				player.getXRot(),
				TeleportTransition.DO_NOTHING));
		player.removeAllEffects();
		player.setHealth(player.getMaxHealth());
	}

	private void registerLootModifiers() {
		var bastionTables = java.util.List.of(
				BuiltInLootTables.BASTION_TREASURE,
				BuiltInLootTables.BASTION_OTHER,
				BuiltInLootTables.BASTION_BRIDGE,
				BuiltInLootTables.BASTION_HOGLIN_STABLE
		);

		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (bastionTables.stream().noneMatch(k -> k.equals(key))) return;

			registries.lookupOrThrow(Registries.ENCHANTMENT).get(DIVINE_BLESSING_KEY).ifPresent(enchantmentHolder -> {
				var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
				mutable.set(enchantmentHolder, 1);

				tableBuilder.withPool(
						LootPool.lootPool()
								.setRolls(ConstantValue.exactly(1))
								.add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
										.apply(SetComponentsFunction.setComponent(
												DataComponents.STORED_ENCHANTMENTS,
												mutable.toImmutable()))
										.when(LootItemRandomChanceCondition.randomChance(0.15f)))
				);
			});
		});

		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (!BuiltInLootTables.NETHER_BRIDGE.equals(key)) return;

			tableBuilder.withPool(
					LootPool.lootPool()
							.setRolls(ConstantValue.exactly(1))
							.add(LootItem.lootTableItem(SPEED_BOOTS)
									.when(LootItemRandomChanceCondition.randomChance(0.2f)))
			);
		});
	}
}
