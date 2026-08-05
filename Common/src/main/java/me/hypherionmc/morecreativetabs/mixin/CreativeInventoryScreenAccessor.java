package me.hypherionmc.morecreativetabs.mixin;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeInventoryScreen.class)
public interface CreativeInventoryScreenAccessor {

    @Accessor("selectedTab")
    static void setSelectedTab(int selectedTab) {
        throw new AssertionError();
    }
}
