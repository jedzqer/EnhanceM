package net.enhancem.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import java.util.UUID;

public class EnderSoldierEntity extends Zombie {

    private static final String LAST_ATTACKER_TAG = "EnhanceMLastAttacker";
    private static final String LAST_ATTACKER_TIME_TAG = "EnhanceMLastAttackerTime";
    private static final int ATTACKER_MEMORY_TICKS = 100;
    private static final int TARGET_REFRESH_INTERVAL = 10;
    private static final int TELEPORT_ATTEMPTS = 16;
    private static final double TELEPORT_HORIZONTAL_RANGE = 16.0D;
    private static final double TELEPORT_VERTICAL_RANGE = 8.0D;

    @Nullable
    private UUID enhancem$lastAttackerUuid;
    private long enhancem$lastAttackerGameTime = Long.MIN_VALUE;
    private int enhancem$targetRefreshCooldown;

    public EnderSoldierEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel) || !this.isAlive()) {
            return;
        }

        if (this.enhancem$targetRefreshCooldown > 0) {
            this.enhancem$targetRefreshCooldown--;
        } else {
            this.enhancem$targetRefreshCooldown = TARGET_REFRESH_INTERVAL;
            this.enhancem$updateTarget(serverLevel);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
        Entity directEntity = damageSource.getDirectEntity();
        if (directEntity instanceof Projectile) {
            return this.enhancem$tryRandomTeleport(serverLevel);
        }

        Entity attacker = damageSource.getEntity();
        if (attacker instanceof Player player) {
            this.enhancem$rememberAttacker(player);
            this.setTarget(player);
        }

        boolean teleported = this.enhancem$tryTeleportAwayFrom(attacker instanceof LivingEntity living ? living : null, serverLevel);
        if (!teleported) {
            teleported = this.enhancem$tryRandomTeleport(serverLevel);
        }
        if (teleported) {
            return false;
        }

        return super.hurtServer(serverLevel, damageSource, amount);
    }

    @Override
    protected void doPush(Entity entity) {
        if (entity instanceof Projectile) {
            return;
        }
        super.doPush(entity);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.enhancem$lastAttackerUuid != null) {
            output.putString(LAST_ATTACKER_TAG, this.enhancem$lastAttackerUuid.toString());
        }
        output.putLong(LAST_ATTACKER_TIME_TAG, this.enhancem$lastAttackerGameTime);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.enhancem$lastAttackerUuid = input.getString(LAST_ATTACKER_TAG).map(UUID::fromString).orElse(null);
        this.enhancem$lastAttackerGameTime = input.getLongOr(LAST_ATTACKER_TIME_TAG, Long.MIN_VALUE);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_AXE));
        this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        this.setCanPickUpLoot(false);
        return result;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource damageSource) {
        if (damageSource.is(DamageTypeTags.IS_PROJECTILE)) {
            return true;
        }
        return super.isInvulnerableTo(level, damageSource);
    }

    @Override
    public int getMaxHeadXRot() {
        return 50;
    }

    private void enhancem$updateTarget(ServerLevel serverLevel) {
        Player rememberedAttacker = this.enhancem$getRememberedAttacker(serverLevel);
        if (rememberedAttacker != null && rememberedAttacker.isAlive()) {
            this.setTarget(rememberedAttacker);
            return;
        }

        Player nearestPlayer = serverLevel.getNearestPlayer(this, this.getAttributeValue(Attributes.FOLLOW_RANGE));
        if (nearestPlayer != null && nearestPlayer.isAlive()) {
            this.setTarget(nearestPlayer);
        }
    }

    @Nullable
    private Player enhancem$getRememberedAttacker(ServerLevel serverLevel) {
        if (this.enhancem$lastAttackerUuid == null) {
            return null;
        }
        if (serverLevel.getGameTime() - this.enhancem$lastAttackerGameTime > ATTACKER_MEMORY_TICKS) {
            return null;
        }
        return serverLevel.getPlayerByUUID(this.enhancem$lastAttackerUuid);
    }

    private void enhancem$rememberAttacker(Player player) {
        this.enhancem$lastAttackerUuid = player.getUUID();
        this.enhancem$lastAttackerGameTime = this.level().getGameTime();
    }

    private boolean enhancem$tryTeleportAwayFrom(@Nullable LivingEntity attacker, ServerLevel serverLevel) {
        if (attacker == null) {
            return false;
        }

        for (int i = 0; i < TELEPORT_ATTEMPTS; i++) {
            double x = this.getX() + (this.getRandom().nextDouble() - 0.5D) * TELEPORT_HORIZONTAL_RANGE;
            double y = this.getY() + (this.getRandom().nextDouble() - 0.5D) * TELEPORT_VERTICAL_RANGE;
            double z = this.getZ() + (this.getRandom().nextDouble() - 0.5D) * TELEPORT_HORIZONTAL_RANGE;
            double oldDistance = this.distanceToSqr(attacker);
            double newDistance = attacker.distanceToSqr(x, y, z);
            if (newDistance <= oldDistance + 4.0D) {
                continue;
            }
            if (this.randomTeleport(x, y, z, true)) {
                this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                return true;
            }
        }

        return false;
    }

    private boolean enhancem$tryRandomTeleport(ServerLevel serverLevel) {
        for (int i = 0; i < TELEPORT_ATTEMPTS; i++) {
            double x = this.getX() + (this.getRandom().nextDouble() - 0.5D) * TELEPORT_HORIZONTAL_RANGE;
            double y = this.getY() + (this.getRandom().nextDouble() - 0.5D) * TELEPORT_VERTICAL_RANGE;
            double z = this.getZ() + (this.getRandom().nextDouble() - 0.5D) * TELEPORT_HORIZONTAL_RANGE;
            if (this.randomTeleport(x, y, z, true)) {
                this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }
}
