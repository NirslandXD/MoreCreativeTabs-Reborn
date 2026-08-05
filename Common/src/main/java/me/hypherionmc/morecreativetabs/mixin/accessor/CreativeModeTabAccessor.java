package me.hypherionmc.morecreativetabs.mixin.accessor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author HypherionSA
 * Helper Class to access some internal values and modify them as needed without
 * access wideners
 */
@Mixin(CreativeModeTab.class)
public interface CreativeModeTabAccessor {

    @Accessor("displayName")
    Component getInternalDisplayName();

    @Accessor("displayItemsGenerator")
    @Mutable
    void setDisplayItemsGenerator(CreativeModeTab.DisplayItemsGenerator itemsGenerator);

}