package me.hypherionmc.morecreativetabs.mixin.accessor;

import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeTab.class)
public interface ForceCreativeTabAccessor {

    @Accessor("column")
    @Mutable
    void setColumn(int column);

    @Accessor("row")
    @Mutable
    void setRow(CreativeModeTab.Row row);

}
