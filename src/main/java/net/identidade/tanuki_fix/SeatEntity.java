package net.identidade.tanuki_fix;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SeatEntity extends Entity {

    public SeatEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public SeatEntity(BlockPos pos, double ridingOffset, Level level) {
        super(TanukiFix.SEAT.get(), level);

        this.setPos(pos.getX() + 0.5D, pos.getY() + ridingOffset, pos.getZ() + 0.5D);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            BlockPos pos = this.blockPosition();
            if (this.getPassengers().isEmpty() || this.level().isEmptyBlock(pos)) {
                this.discard();

                this.level().updateNeighbourForOutputSignal(pos, this.level().getBlockState(pos).getBlock());
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.DISCARDED) {
            this.getPassengers().forEach(Entity::stopRiding);
        }
        super.remove(reason);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        BlockPos pos = this.blockPosition();

        int[][] offsets = DismountHelper.offsetsForDirection(Direction.NORTH);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (Pose pose : passenger.getDismountPoses()) {
            AABB aabb = passenger.getLocalBoundsForPose(pose);

            for (int[] offset : offsets) {
                mutableBlockPos.set(pos.getX() + offset[0], pos.getY(), pos.getZ() + offset[1]);
                double floorHeight = this.level().getBlockFloorHeight(mutableBlockPos);

                if (DismountHelper.isBlockFloorValid(floorHeight)) {
                    Vec3 vec3 = Vec3.upFromBottomCenterOf(mutableBlockPos, floorHeight);

                    if (DismountHelper.canDismountTo(this.level(), passenger, aabb.move(vec3))) {
                        passenger.setPose(pose);

                        return vec3;
                    }
                }
            }
        }

        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }
}
