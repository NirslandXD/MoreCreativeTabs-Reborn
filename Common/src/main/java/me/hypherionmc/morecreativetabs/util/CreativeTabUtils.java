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

        ItemStack stack = getItemStack(tabIcon.name);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (tabIcon.nbt != null) {
            try {
                CompoundTag tag = TagParser.parseTag(tabIcon.nbt);
                stack.setTag(tag);
            } catch (CommandSyntaxException e) {
                ModConstants.logger.warn("Ignoring invalid NBT for creative tab icon {}", tabIcon.name, e);
            }
        }
        return stack;
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
        String normalized = key.trim().toLowerCase(Locale.ROOT).replace(".", "_");
        switch (normalized) {
            case "buildingblocks":
            case "building_blocks":
            case "itemgroup_buildingblocks":
            case "itemgroup_building_blocks":
                normalized = "building_blocks";
                break;
            case "coloredblocks":
            case "colored_blocks":
            case "itemgroup_coloredblocks":
            case "itemgroup_colored_blocks":
                normalized = "building_blocks";
                break;
            case "natural":
            case "natural_blocks":
            case "itemgroup_natural":
            case "itemgroup_natural_blocks":
                normalized = "decorations";
                break;
            case "functional":
            case "functional_blocks":
            case "itemgroup_functional":
            case "itemgroup_functional_blocks":
                normalized = "decorations";
                break;
            case "redstone_blocks":
            case "itemgroup_redstone":
            case "itemgroup_redstone_blocks":
                normalized = "redstone";
                break;
            case "tools_and_utilities":
            case "toolsandutilities":
            case "itemgroup_tools":
            case "itemgroup_toolsandutilities":
            case "itemgroup_tools_and_utilities":
                normalized = "tools";
                break;
            case "food_and_drinks":
            case "foodanddrink":
            case "itemgroup_foodanddrink":
            case "itemgroup_food_and_drinks":
                normalized = "food";
                break;
            case "ingredients":
            case "materials":
            case "spawn_eggs":
            case "spawneggs":
            case "operator_utilities":
            case "operatorutilities":
            case "operator_blocks":
            case "op_blocks":
            case "opblocks":
            case "itemgroup_ingredients":
            case "itemgroup_materials":
            case "itemgroup_spawneggs":
            case "itemgroup_spawn_eggs":
            case "itemgroup_operator_utilities":
            case "itemgroup_operatorutilities":
            case "itemgroup_operator_blocks":
            case "itemgroup_op_blocks":
            case "itemgroup_opblocks":
                normalized = "misc";
                break;
            case "itemgroup_decorations":
                normalized = "decorations";
                break;
            case "itemgroup_transportation":
                normalized = "transportation";
                break;
            case "itemgroup_misc":
                normalized = "misc";
                break;
            case "itemgroup_food":
                normalized = "food";
                break;
            case "itemgroup_combat":
                normalized = "combat";
                break;
            case "itemgroup_brewing":
                normalized = "brewing";
                break;
            case "itemgroup_kubejs_tab":
            case "kubejs_tab":
                normalized = "itemgroup_kubejs_tab";
                break;
            default:
                break;
        }
        return normalized;
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
        aliases.addAll(selectorAliases("itemgroup." + key));
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
