package com.omgisa.the_forgotten_banana_of_rebirth.block.custom;

import com.mojang.serialization.MapCodec;
import com.omgisa.the_forgotten_banana_of_rebirth.block.entity.TombstoneBlockEntity;
import com.omgisa.the_forgotten_banana_of_rebirth.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

public class TombstoneBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 2);
    public static final MapCodec<TombstoneBlock> CODEC = simpleCodec(TombstoneBlock::new);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public TombstoneBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, 0));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int variant = new Random().nextInt(3); // 0, 1, or 2
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(VARIANT, variant);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, VARIANT);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction(@NotNull BlockState state) {
        return PushReaction.BLOCK; // Immovable by pistons, not destroyed or pushed
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TombstoneBlockEntity(pos, state);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TombstoneBlockEntity tombstone) {
            boolean allowed = player.isCreative() || tombstone.isOwner(player);
            if (!allowed)
                return InteractionResult.PASS;
            tombstone.dropAll(level, pos);
            // Remove the tombstone after retrieval
            level.removeBlock(pos, false);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        // Only act on server, with a banana, and when the clicker is NOT the owner
        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        if (stack.isEmpty() || stack.getItem() != ModItems.BANANA.get())
            return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TombstoneBlockEntity tombstone))
            return InteractionResult.PASS;

        if (tombstone.isOwner(player))
            return InteractionResult.PASS; // owner uses default behavior

        // Non-owner with banana: try to revive the owner
        if (!(level instanceof ServerLevel serverLevel))
            return InteractionResult.PASS;
        var server = serverLevel.getServer();
        var optOwner = tombstone.getOwnerUuid();
        if (optOwner.isEmpty())
            return InteractionResult.CONSUME; // nothing to do, consume to avoid spam
        UUID ownerId = optOwner.get();
        ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
        if (owner == null) {
            // Owner not online; do nothing but consume to prevent repeated attempts
            return InteractionResult.CONSUME;
        }

        // If owner is dead or in spectator, force proper respawn to bed/world spawn
        if (!owner.isAlive() || owner.isSpectator()) {
            ServerPlayer respawned = server.getPlayerList().respawn(owner, false, Entity.RemovalReason.KILLED);
            respawned.setGameMode(GameType.SURVIVAL);
            float targetHealth = Math.max(8.0f, respawned.getHealth());
            respawned.setHealth(targetHealth);
        } else {
            // Otherwise just ensure Survival and minimum health
            owner.setGameMode(GameType.SURVIVAL);
            float targetHealth = Math.max(8.0f, owner.getHealth());
            owner.setHealth(targetHealth);
        }

        // Consume one banana from the user unless creative
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }
}
