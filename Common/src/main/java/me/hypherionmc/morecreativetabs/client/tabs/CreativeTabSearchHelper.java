package me.hypherionmc.morecreativetabs.client.tabs;

import me.hypherionmc.morecreativetabs.mixin.accessor.CreativeModeTabAccessor;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;

import java.lang.reflect.Proxy;
import java.util.Set;

final class CreativeTabSearchHelper {

    private CreativeTabSearchHelper() {
    }

    static void configure(CreativeModeTab searchTab) {
        ((CreativeModeTabAccessor) searchTab).setDisplayItemsGenerator(createGenerator());
    }

    private static CreativeModeTab.DisplayItemsGenerator createGenerator() {
        Object proxy = Proxy.newProxyInstance(
                CreativeModeTab.DisplayItemsGenerator.class.getClassLoader(),
                new Class<?>[]{CreativeModeTab.DisplayItemsGenerator.class},
                (instance, method, args) -> {
                    if (method.getDeclaringClass() == Object.class)
                        return switch (method.getName()) {
                            case "toString" -> "MoreCreativeTabs search display generator";
                            case "hashCode" -> System.identityHashCode(instance);
                            case "equals" -> instance == args[0];
                            default -> null;
                        };

                    if (args != null) {
                        for (Object arg : args) {
                            if (arg instanceof CreativeModeTab.Output output) {
                                output.acceptAll(collectSearchStacks());
                                break;
                            }
                        }
                    }
                    return null;
                });

        return (CreativeModeTab.DisplayItemsGenerator) proxy;
    }

    private static Set<ItemStack> collectSearchStacks() {
        Set<ItemStack> stacks = ItemStackLinkedSet.createTypeAndTagSet();

        for (CreativeModeTab tab : CustomCreativeTabRegistry.INSTANCE.getCurrentTabs()) {
            if (!CustomCreativeTabRegistry.INSTANCE.isVanillaSearch(tab))
                stacks.addAll(tab.getSearchTabDisplayItems());
        }

        return stacks;
    }
}
