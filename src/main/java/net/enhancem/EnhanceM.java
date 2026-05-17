package net.enhancem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnhanceM implements ModInitializer {
	public static final String MOD_ID = "enhancem";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final ResourceKey<Enchantment> DIVINE_BLESSING_KEY = ResourceKey.create(
			Registries.ENCHANTMENT,
			Identifier.fromNamespaceAndPath("enhancem", "divine_blessing")
	);

	@Override
	public void onInitialize() {
		LOGGER.info("EnhanceM loaded!");
		registerLootModifiers();
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
	}
}