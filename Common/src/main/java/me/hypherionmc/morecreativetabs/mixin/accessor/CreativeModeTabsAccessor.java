package me.hypherionmc.morecreativetabs.mixin.accessor;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CreativeModeTabs.class)
public interface CreativeModeTabsAccessor {

    @Accessor("INVENTORY")
    static CreativeModeTab getInventoryTab() {
        throw new AssertionError();
    }

    @Accessor("HOTBAR")
    static CreativeModeTab getHotbarTab() {
        throw new AssertionError();
    }

    @Accessor("SEARCH")
    static CreativeModeTab getSearchTab() {
        throw new AssertionError();
    }

    @Accessor("OP_BLOCKS")
    static CreativeModeTab getOpBlockTab() {
        throw new AssertionError();
    }

    @Accessor("TABS")
    static List<CreativeModeTab> getTabs() {
        throw new AssertionError();
    }

}
