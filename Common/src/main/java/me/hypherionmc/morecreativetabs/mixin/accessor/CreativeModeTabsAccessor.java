package me.hypherionmc.morecreativetabs.mixin.accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 79b2a8a (initial commit)
/**
 * @author HypherionSA
 * Helper class to access private values from CreativeModeTabs
 */
<<<<<<< HEAD
=======
=======
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
@Mixin(CreativeModeTabs.class)
public interface CreativeModeTabsAccessor {

    @Accessor("INVENTORY")
    static ResourceKey<CreativeModeTab> getInventoryTab() {
        throw new AssertionError();
    }

    @Accessor("HOTBAR")
    static ResourceKey<CreativeModeTab> getHotbarTab() {
        throw new AssertionError();
    }

    @Accessor("SEARCH")
    static ResourceKey<CreativeModeTab> getSearchTab() {
        throw new AssertionError();
    }

    @Accessor("OP_BLOCKS")
    static ResourceKey<CreativeModeTab> getOpBlockTab() {
        throw new AssertionError();
    }

<<<<<<< HEAD
}
=======
<<<<<<< HEAD
}
=======
}
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
