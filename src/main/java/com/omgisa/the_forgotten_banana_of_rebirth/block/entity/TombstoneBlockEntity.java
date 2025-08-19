package com.omgisa.the_forgotten_banana_of_rebirth.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TombstoneBlockEntity extends BlockEntity implements Clearable {
    private NonNullList<ItemStack> items = NonNullList.create();
    private UUID ownerUuid = null;
    private String ownerName = null;

    public TombstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOMBSTONE_BE.get(), pos, state);
    }

    public void setOwner(@NotNull Player player) {
        this.ownerUuid = player.getUUID();
        this.ownerName = player.getGameProfile().getName();
        setChanged();
    }

    public boolean isOwner(@NotNull Player player) {
        return ownerUuid != null && ownerUuid.equals(player.getUUID());
    }

    public void depositFrom(@NotNull Inventory inv) {
        // Collect all non-empty items from the player's inventory, including armor and offhand.
        List<ItemStack> collected = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                collected.add(stack.copy());
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
        // Resize internal storage to fit exactly the collected items
        this.items = NonNullList.withSize(collected.size(), ItemStack.EMPTY);
        for (int i = 0; i < collected.size(); i++) {
            this.items.set(i, collected.get(i));
        }
        setChanged();
    }

    public void dropAll(Level level, BlockPos pos) {
        if (level.isClientSide)
            return;
        if (this.items.isEmpty())
            return;
        SimpleContainer container = new SimpleContainer(this.items.size());
        for (int i = 0; i < this.items.size(); i++) {
            container.setItem(i, this.items.get(i));
        }
        Containers.dropContents(level, pos, container);
        clearContent();
        setChanged();
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
    }
}
