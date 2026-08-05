package me.hypherionmc.morecreativetabs.mixin;

import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CreativeModeTab.class)
public interface CreativeModeTabAccessor {

    @Accessor
    void setId(int id);

    @Accessor("TABS")
    static void setTabs(CreativeModeTab[] tabs) {
        throw new AssertionError();
    }

    @Invoker("setBackgroundSuffix")
    CreativeModeTab mctSetBackgroundSuffix(String backgroundSuffix);
}
