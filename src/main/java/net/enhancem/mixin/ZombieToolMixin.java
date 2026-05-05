package net.enhancem.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Zombie.class)
public class ZombieToolMixin {

    @Unique
    private static final Random RANDOM = new Random();

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("TAIL"))
    private void enhancem$addRandomTool(CallbackInfo ci) {
        Zombie zombie = (Zombie) (Object) this;
        ItemStack tool = getRandomTool(zombie);
        zombie.setItemSlot(EquipmentSlot.MAINHAND, tool);
    }

    @Unique
    private ItemStack getRandomTool(Zombie zombie) {
        int roll = RANDOM.nextInt(100);

        if (roll < 25) {
            return getRandomCopperTool(zombie);
        } else if (roll < 50) {
            return getRandomIronTool(zombie);
        } else if (roll < 80) {
            return getRandomGoldTool(zombie);
        } else if (roll < 95) {
            return getRandomDiamondTool(zombie);
        } else {
            return getRandomNetheriteTool(zombie);
        }
    }

    @Unique
    private ItemStack getRandomCopperTool(Zombie zombie) {
        int toolType = RANDOM.nextInt(5);
        return switch (toolType) {
            case 0 -> enchantSword(zombie, new ItemStack(Items.COPPER_SWORD));
            case 1 -> new ItemStack(Items.COPPER_PICKAXE);
            case 2 -> new ItemStack(Items.COPPER_AXE);
            case 3 -> new ItemStack(Items.COPPER_SHOVEL);
            case 4 -> new ItemStack(Items.COPPER_HOE);
            default -> enchantSword(zombie, new ItemStack(Items.COPPER_SWORD));
        };
    }

    @Unique
    private ItemStack getRandomIronTool(Zombie zombie) {
        int toolType = RANDOM.nextInt(5);
        return switch (toolType) {
            case 0 -> enchantSword(zombie, new ItemStack(Items.IRON_SWORD));
            case 1 -> new ItemStack(Items.IRON_PICKAXE);
            case 2 -> new ItemStack(Items.IRON_AXE);
            case 3 -> new ItemStack(Items.IRON_SHOVEL);
            case 4 -> new ItemStack(Items.IRON_HOE);
            default -> enchantSword(zombie, new ItemStack(Items.IRON_SWORD));
        };
    }

    @Unique
    private ItemStack getRandomGoldTool(Zombie zombie) {
        int toolType = RANDOM.nextInt(5);
        return switch (toolType) {
            case 0 -> enchantSword(zombie, new ItemStack(Items.GOLDEN_SWORD));
            case 1 -> new ItemStack(Items.GOLDEN_PICKAXE);
            case 2 -> new ItemStack(Items.GOLDEN_AXE);
            case 3 -> new ItemStack(Items.GOLDEN_SHOVEL);
            case 4 -> new ItemStack(Items.GOLDEN_HOE);
            default -> enchantSword(zombie, new ItemStack(Items.GOLDEN_SWORD));
        };
    }

    @Unique
    private ItemStack getRandomDiamondTool(Zombie zombie) {
        int toolType = RANDOM.nextInt(5);
        return switch (toolType) {
            case 0 -> enchantSword(zombie, new ItemStack(Items.DIAMOND_SWORD));
            case 1 -> new ItemStack(Items.DIAMOND_PICKAXE);
            case 2 -> new ItemStack(Items.DIAMOND_AXE);
            case 3 -> new ItemStack(Items.DIAMOND_SHOVEL);
            case 4 -> new ItemStack(Items.DIAMOND_HOE);
            default -> enchantSword(zombie, new ItemStack(Items.DIAMOND_SWORD));
        };
    }

    @Unique
    private ItemStack getRandomNetheriteTool(Zombie zombie) {
        int toolType = RANDOM.nextInt(5);
        return switch (toolType) {
            case 0 -> enchantSword(zombie, new ItemStack(Items.NETHERITE_SWORD));
            case 1 -> new ItemStack(Items.NETHERITE_PICKAXE);
            case 2 -> new ItemStack(Items.NETHERITE_AXE);
            case 3 -> new ItemStack(Items.NETHERITE_SHOVEL);
            case 4 -> new ItemStack(Items.NETHERITE_HOE);
            default -> enchantSword(zombie, new ItemStack(Items.NETHERITE_SWORD));
        };
    }

    @Unique
    private ItemStack enchantSword(Zombie zombie, ItemStack sword) {
        if (RANDOM.nextInt(100) < 25) {
            Holder.Reference<Enchantment> enchantment;
            if (RANDOM.nextBoolean()) {
                enchantment = zombie.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FIRE_ASPECT);
                sword.enchant(enchantment, 2);
            } else {
                enchantment = zombie.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS);
                sword.enchant(enchantment, 3);
            }
        }
        return sword;
    }
}
