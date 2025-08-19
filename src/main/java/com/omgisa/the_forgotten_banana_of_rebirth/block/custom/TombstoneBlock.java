package com.omgisa.the_forgotten_banana_of_rebirth.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class TombstoneBlock extends HorizontalDirectionalBlock {
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
}
