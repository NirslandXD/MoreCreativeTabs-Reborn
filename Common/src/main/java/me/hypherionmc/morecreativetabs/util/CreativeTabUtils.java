package me.hypherionmc.morecreativetabs.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.hypherionmc.morecreativetabs.ModConstants;
import me.hypherionmc.morecreativetabs.client.data.jsonhelpers.CustomCreativeTab;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabManager;
import me.hypherionmc.morecreativetabs.mixin.CreativeModeTabAccessor;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class CreativeTabUtils {

    public static ItemStack makeTabIcon(CustomCreativeTab json) {
        CustomCreativeTab.TabIcon tabIcon = new CustomCreativeTab.TabIcon();

        if (json.tabStack != null) {
            tabIcon = json.tabStack;
        } else if (json.tabIcon != null) {
            tabIcon = new CustomCreativeTab.TabIcon();
            tabIcon.name = json.tabIcon;
        }

        ItemStack icon = getItemStack(tabIcon.name);
        if (icon.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (tabIcon.nbt != null) {
            try {
                CompoundTag tag = TagParser.parseTag(tabIcon.nbt);
                icon.setTag(tag);
            } catch (CommandSyntaxException e) {
                ModConstants.logger.error("Failed to parse NBT for creative tab icon {}", tabIcon.name, e);
            }
        }
        return icon;
    }

    public static ItemStack getItemStack(String jsonItem) {
        if (jsonItem == null || jsonItem.isBlank())
            return ItemStack.EMPTY;

        ResourceLocation itemId = ResourceLocation.tryParse(jsonItem);
        if (itemId == null)
            return ItemStack.EMPTY;

        Optional<Item> itemOptional = Registry.ITEM.getOptional(itemId);
        return itemOptional.map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    public static String prefix(String tabName) {
        return String.format("%s.%s", "morecreativetabs", tabName);
    }

    public static CreativeModeTab defaultTabCreator(int index, CustomCreativeTab json, List<ItemStack> tabItems) {
        if (index < 0) {
            index = CreativeModeTab.TABS.length;
            CreativeModeTabAccessor.setTabs(java.util.Arrays.copyOf(CreativeModeTab.TABS, index + 1));
        }

        CreativeModeTab tab = new CreativeModeTab(index, prefix(json.tabName)) {
            @Override
            public ItemStack makeIcon() {
                return CreativeTabUtils.makeTabIcon(json);
            }

            @Override
            public void fillItemList(NonNullList<ItemStack> itemStacks) {
                itemStacks.clear();
                itemStacks.addAll(tabItems);
            }
        };

        if (Boolean.TRUE.equals(json.searchBar)) {
            ((CreativeModeTabAccessor) tab).mctSetBackgroundSuffix("item_search.png");
        } else if (json.tabBackground != null) {
            ((CreativeModeTabAccessor) tab).mctSetBackgroundSuffix(json.tabBackground);
        }

        return tab;
    }

    public static String fileToTab(String input) {
        String prefix = "morecreativetabs/";
        String suffix = ".json";
        if (input.startsWith(prefix))
            input = input.substring(prefix.length());
        if (input.endsWith(suffix))
            input = input.substring(0, input.length() - suffix.length());

        return input;
    }

    public static Optional<Pair<CustomCreativeTab, List<ItemStack>>> replacementTab(String tabName) {
        for (String alias : selectorAliases(tabName)) {
            Pair<CustomCreativeTab, List<ItemStack>> replacement = CustomCreativeTabManager.replacedTabs.get(alias);
            if (replacement != null)
                return Optional.of(replacement);
        }
        return Optional.empty();
    }

    public static String normalizeLegacyReplacementKey(String key) {
        if (key == null)
            return null;
        String normalized = key.toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "itemgroup_buildingblocks":
            case "itemgroup.buildingblocks":
            case "buildingblocks":
                normalized = "building_blocks";
                break;
            case "itemgroup_decorations":
            case "itemgroup.decorations":
                normalized = "decorations";
                break;
            case "itemgroup_redstone":
            case "itemgroup.redstone":
            case "itemgroup_redstone_blocks":
            case "itemgroup.redstone_blocks":
                normalized = "redstone";
                break;
            case "itemgroup_transportation":
            case "itemgroup.transportation":
                normalized = "transportation";
                break;
            case "itemgroup_misc":
            case "itemgroup.misc":
            case "itemgroup_spawn_eggs":
            case "itemgroup.spawn_eggs":
                normalized = "misc";
                break;
            case "itemgroup_tools":
            case "itemgroup.tools":
            case "itemgroup_tools_and_utilities":
            case "itemgroup.tools_and_utilities":
            case "itemgroup_toolsandutilities":
            case "itemgroup.toolsandutilities":
                normalized = "tools";
                break;
            case "itemgroup_combat":
            case "itemgroup.combat":
                normalized = "combat";
                break;
            case "itemgroup_brewing":
            case "itemgroup.brewing":
                normalized = "brewing";
                break;
            case "itemgroup_materials":
            case "itemgroup.materials":
            case "itemgroup_ingredients":
            case "itemgroup.ingredients":
            case "materials":
            case "ingredients":
                normalized = "misc";
                break;
            case "itemgroup_food":
            case "itemgroup.food":
            case "itemgroup_food_and_drinks":
            case "itemgroup.food_and_drinks":
            case "itemgroup_foodanddrink":
            case "itemgroup.foodanddrink":
                normalized = "food";
                break;
            case "itemgroup_colored_blocks":
            case "itemgroup.colored_blocks":
            case "itemgroup_coloredblocks":
            case "itemgroup.coloredblocks":
                normalized = "building_blocks";
                break;
            case "itemgroup_natural_blocks":
            case "itemgroup.natural_blocks":
            case "itemgroup_natural":
            case "itemgroup.natural":
            case "itemgroup_functional_blocks":
            case "itemgroup.functional_blocks":
            case "itemgroup_functional":
            case "itemgroup.functional":
                normalized = "decorations";
                break;
            case "itemgroup.kubejs.tab":
            case "itemgroup_kubejs_tab":
            case "kubejs.tab":
            case "kubejs_tab":
                normalized = "kubejs_tab";
                break;
            default:
                break;
        }
        return normalized.replace(".", "_");
    }

    public static Set<String> selectorAliases(String selector) {
        LinkedHashSet<String> aliases = new LinkedHashSet<>();
        if (selector == null || selector.isBlank())
            return aliases;

        String key = selector.trim().toLowerCase(Locale.ROOT);
        aliases.add(key);
        if (!key.contains(".") && !key.contains(":"))
            aliases.add("itemgroup." + key);
        if (key.startsWith("itemgroup."))
            aliases.add(key.substring("itemgroup.".length()));
        aliases.add(key.replace(".", "_"));
        aliases.add(normalizeLegacyReplacementKey(key));
        aliases.add(normalizeLegacyReplacementKey(key.replace(".", "_")));
        aliases.remove(null);
        return aliases;
    }

    public static Set<String> tabAliases(CreativeModeTab tab) {
        LinkedHashSet<String> aliases = new LinkedHashSet<>();
        if (tab == null)
            return aliases;
        String key = tab.getRecipeFolderName().toLowerCase(Locale.ROOT);
        aliases.addAll(selectorAliases(key));
        aliases.addAll(selectorAliases(key.replace(".", "_")));
        return aliases;
    }

    public static boolean matchesTab(Set<String> selectors, CreativeModeTab tab) {
        for (String alias : tabAliases(tab)) {
            if (selectors.contains(alias))
                return true;
        }
        return false;
    }
}
