package com.omgisa.the_forgotten_banana_of_rebirth.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class TombstoneBlockEntity extends RandomizableContainerBlockEntity implements Clearable {
    private static final int SIZE = 42; // 36 inv + 4 armor + 1 offhand, last slot reserved for owner marker
    private static final int META_SLOT = SIZE - 1; // reserved owner marker slot (not dropped)
    private static final String OWNER_UUID_KEY = "owner_uuid";
    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public TombstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOMBSTONE_BE.get(), pos, state);
    }

    public void depositFrom(@NotNull Player player) {
        // Set owner marker before storing items
        setOwnerMarker(player);
        deposit(player.getInventory(), true);
    }

    public void depositCopyOf(@NotNull Inventory inv, @NotNull Player owner) {
        setOwnerMarker(owner);
        deposit(inv, false);
    }

    private void setOwnerMarker(@NotNull Player player) {
        ItemStack marker = new ItemStack(Items.PAPER);
        // Optional cosmetic name
        marker.set(DataComponents.CUSTOM_NAME, Component.literal("Tomb of " + player.getGameProfile().getName()));
        // Persist owner UUID in CustomData
        CompoundTag ownerTag = new CompoundTag();
        ownerTag.putString(OWNER_UUID_KEY, player.getUUID().toString());
        marker.set(DataComponents.CUSTOM_DATA, CustomData.of(ownerTag));
        items.set(META_SLOT, marker);
        setChanged();
    }

    public boolean isOwner(@NotNull Player player) {
        Optional<UUID> uuid = getOwnerUuid();
        return uuid.map(u -> u.equals(player.getUUID())).orElse(false);
    }

    // Made public so blocks or other systems can identify the owner of this tombstone
    public Optional<UUID> getOwnerUuid() {
        ItemStack marker = items.get(META_SLOT);
        if (marker.isEmpty())
            return Optional.empty();
        CustomData data = marker.get(DataComponents.CUSTOM_DATA);
        if (data == null)
            return Optional.empty();
        CompoundTag tag = data.copyTag();
        String s = tag.getString(OWNER_UUID_KEY).orElse(null);
        if (s == null)
            return Optional.empty();
        try {
            return Optional.of(UUID.fromString(s));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void deposit(@NotNull Inventory inv, boolean move) {
        Level lvl = this.getLevel();
        if (!(lvl instanceof ServerLevel))
            return;
        // Fill our inventory with player's items; drop overflow
        SimpleContainer overflow = new SimpleContainer(inv.getContainerSize());
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty())
                continue;
            ItemStack toStore = stack.copy();
            // place into first free slot (skip meta slot)
            boolean placed = false;
            for (int slot = 0; slot < items.size(); slot++) {
                if (slot == META_SLOT)
                    continue;
                if (items.get(slot).isEmpty()) {
                    items.set(slot, toStore);
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                overflow.addItem(toStore);
            }
            if (move) {
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
        // Drop any overflow
        if (!overflow.isEmpty()) {
            Containers.dropContents(lvl, this.getBlockPos(), overflow);
        }
        setChanged();
    }

    public void dropAll(Level level, BlockPos pos) {
        if (level.isClientSide)
            return;
        boolean hasAny = false;
        SimpleContainer container = new SimpleContainer(items.size() - 1);
        int dst = 0;
        for (int i = 0; i < items.size(); i++) {
            if (i == META_SLOT)
                continue; // don't drop the meta marker
            ItemStack s = items.get(i);
            if (!s.isEmpty())
                hasAny = true;
            container.setItem(dst++, s);
        }
        if (hasAny) {
            Containers.dropContents(level, pos, container);
        }
        clearContent();
        setChanged();
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++)
            items.set(i, ItemStack.EMPTY);
    }

    // Container implementation
    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("block.the_forgotten_banana_of_rebirth.tombstone");
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> list) {
        this.items = list;
    }

    @Override
    protected @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory) {
        return null;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    // Networking: sync to client when needed
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider pRegistries) {
        return saveWithFullMetadata(pRegistries);
    }

    // Persistence: write/read using ValueOutput/ValueInput
    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        if (!this.trySaveLootTable(output)) {
            ContainerHelper.saveAllItems(output, this.items);
        }
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(input)) {
            ContainerHelper.loadAllItems(input, this.items);
        }
    }
}
