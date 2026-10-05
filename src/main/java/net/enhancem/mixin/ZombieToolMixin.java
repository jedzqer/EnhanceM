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

    @Unique
    private static final int TOOL_SWORD = 0;

    @Unique
    private static final int TOOL_SPEAR = 1;

    @Unique
    private static final int TOOL_PICKAXE = 2;

    @Unique
    private static final int TOOL_AXE = 3;

    @Unique
    private static final int TOOL_SHOVEL = 4;

    @Unique
    private static final int TOOL_HOE = 5;

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

    /**
     * 每档材质内的工具类型分布：剑 18%、长矛 10%、镐 18%、斧 18%、锹 18%、锄 18%。
     */
    @Unique
    private static int rollToolType() {
        int roll = RANDOM.nextInt(100);
        if (roll < 18) {
            return TOOL_SWORD;
        } else if (roll < 28) {
            return TOOL_SPEAR;
        } else if (roll < 46) {
            return TOOL_PICKAXE;
        } else if (roll < 64) {
            return TOOL_AXE;
        } else if (roll < 82) {
            return TOOL_SHOVEL;
        } else {
            return TOOL_HOE;
        }
    }

    @Unique
    private ItemStack getRandomCopperTool(Zombie zombie) {
        return switch (rollToolType()) {
            case TOOL_SWORD -> enchantSword(zombie, new ItemStack(Items.COPPER_SWORD));
            case TOOL_SPEAR -> enchantSpear(zombie, new ItemStack(Items.COPPER_SPEAR));
            case TOOL_PICKAXE -> new ItemStack(Items.COPPER_PICKAXE);
            case TOOL_AXE -> new ItemStack(Items.COPPER_AXE);
            case TOOL_SHOVEL -> new ItemStack(Items.COPPER_SHOVEL);
            default -> new ItemStack(Items.COPPER_HOE);
        };
    }

    @Unique
    private ItemStack getRandomIronTool(Zombie zombie) {
        return switch (rollToolType()) {
            case TOOL_SWORD -> enchantSword(zombie, new ItemStack(Items.IRON_SWORD));
            case TOOL_SPEAR -> enchantSpear(zombie, new ItemStack(Items.IRON_SPEAR));
            case TOOL_PICKAXE -> new ItemStack(Items.IRON_PICKAXE);
            case TOOL_AXE -> new ItemStack(Items.IRON_AXE);
            case TOOL_SHOVEL -> new ItemStack(Items.IRON_SHOVEL);
            default -> new ItemStack(Items.IRON_HOE);
        };
    }

    @Unique
    private ItemStack getRandomGoldTool(Zombie zombie) {
        return switch (rollToolType()) {
            case TOOL_SWORD -> enchantSword(zombie, new ItemStack(Items.GOLDEN_SWORD));
            case TOOL_SPEAR -> enchantSpear(zombie, new ItemStack(Items.GOLDEN_SPEAR));
            case TOOL_PICKAXE -> new ItemStack(Items.GOLDEN_PICKAXE);
            case TOOL_AXE -> new ItemStack(Items.GOLDEN_AXE);
            case TOOL_SHOVEL -> new ItemStack(Items.GOLDEN_SHOVEL);
            default -> new ItemStack(Items.GOLDEN_HOE);
        };
    }

    @Unique
    private ItemStack getRandomDiamondTool(Zombie zombie) {
        return switch (rollToolType()) {
            case TOOL_SWORD -> enchantSword(zombie, new ItemStack(Items.DIAMOND_SWORD));
            case TOOL_SPEAR -> enchantSpear(zombie, new ItemStack(Items.DIAMOND_SPEAR));
            case TOOL_PICKAXE -> new ItemStack(Items.DIAMOND_PICKAXE);
            case TOOL_AXE -> new ItemStack(Items.DIAMOND_AXE);
            case TOOL_SHOVEL -> new ItemStack(Items.DIAMOND_SHOVEL);
            default -> new ItemStack(Items.DIAMOND_HOE);
        };
    }

    @Unique
    private ItemStack getRandomNetheriteTool(Zombie zombie) {
        return switch (rollToolType()) {
            case TOOL_SWORD -> enchantSword(zombie, new ItemStack(Items.NETHERITE_SWORD));
            case TOOL_SPEAR -> enchantSpear(zombie, new ItemStack(Items.NETHERITE_SPEAR));
            case TOOL_PICKAXE -> new ItemStack(Items.NETHERITE_PICKAXE);
            case TOOL_AXE -> new ItemStack(Items.NETHERITE_AXE);
            case TOOL_SHOVEL -> new ItemStack(Items.NETHERITE_SHOVEL);
            default -> new ItemStack(Items.NETHERITE_HOE);
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

    @Unique
    private ItemStack enchantSpear(Zombie zombie, ItemStack spear) {
        if (RANDOM.nextInt(100) < 25) {
            Holder.Reference<Enchantment> enchantment = zombie.level().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.KNOCKBACK);
            spear.enchant(enchantment, 2);
        }
        return spear;
    }
}
