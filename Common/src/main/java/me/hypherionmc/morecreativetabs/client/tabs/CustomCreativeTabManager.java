package me.hypherionmc.morecreativetabs.client.tabs;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.hypherionmc.morecreativetabs.ModConstants;
import me.hypherionmc.morecreativetabs.client.data.jsonhelpers.CustomCreativeTab;
import me.hypherionmc.morecreativetabs.client.data.jsonhelpers.DisabledTabsJsonHelper;
import me.hypherionmc.morecreativetabs.client.data.jsonhelpers.OrderedTabs;
import me.hypherionmc.morecreativetabs.mixin.CreativeInventoryScreenAccessor;
import me.hypherionmc.morecreativetabs.mixin.CreativeModeTabAccessor;
import me.hypherionmc.morecreativetabs.platform.PlatformServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static me.hypherionmc.morecreativetabs.util.CreativeTabUtils.*;

public class CustomCreativeTabManager {

    public static final Set<Item> hiddenStacks = new HashSet<>();
    public static final Map<Item, String> remappedItems = new HashMap<>();
    public static final Set<CreativeModeTab> customTabs = new LinkedHashSet<>();
    public static final Set<String> disabledTabs = new HashSet<>();
    public static final Set<String> reorderedTabs = new LinkedHashSet<>();
    public static boolean showNames = false;
    public static CreativeModeTab[] tabsBefore;
    public static final Map<String, Pair<CustomCreativeTab, List<ItemStack>>> replacedTabs = new HashMap<>();
    public static final Set<String> searchableTabs = new HashSet<>();
    public static final Set<String> searchDisabledTabs = new HashSet<>();
    public static final List<CreativeModeTab> currentTabs = new ArrayList<>();

    public static final int DISABLED_POLICY = 1;
    public static final int ORDERED_POLICY = 2;

    private static final Gson GSON = new Gson();
    private static final Set<String> PROTECTED_TABS = new HashSet<>(Arrays.asList(
            "itemgroup.search", "search", "itemgroup_search",
            "itemgroup.hotbar", "hotbar", "itemgroup_hotbar",
            "itemgroup.inventory", "inventory", "itemgroup_inventory"
    ));
    private static int loadedTabs;
    private static int skippedTabs;
    private static int failedTabs;
    private static CreativeModeTab selectedTabBeforeReload;

    public static void loadEntries(Map<ResourceLocation, Resource> entries, TabCreator creator) {
        entries.entrySet().stream().sorted(Comparator.comparing(entry -> entry.getKey().toString())).forEach(entry -> {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();
            ModConstants.logger.info("Processing {}", location.toString());

            try (InputStream stream = resource.open()) {
                CustomCreativeTab json = GSON.fromJson(new InputStreamReader(stream), CustomCreativeTab.class);
                if (json == null)
                    throw new IllegalArgumentException("JSON is empty");

                boolean replacementTab = hasTabReplace(json) || json.replace;
                if (json.tabEnabled == null)
                    throw new IllegalArgumentException("Missing tab_enabled");
                if (!json.tabEnabled) {
                    skippedTabs++;
                    return;
                }
                if (!replacementTab) {
                    if (json.tabName == null || json.tabName.isBlank())
                        throw new IllegalArgumentException("Missing tab_name");
                    if (json.tabItems == null)
                        throw new IllegalArgumentException("Missing tab_items");
                    if ((json.tabStack == null || json.tabStack.name == null || json.tabStack.name.isBlank())
                            && (json.tabIcon == null || json.tabIcon.isBlank()))
                        throw new IllegalArgumentException("Missing tab_stack.name");
                }

                List<ItemStack> tabItems = parseTabItems(location, json);

                if (replacementTab) {
                    registerReplacement(location, json, tabItems);
                    if (!isHotbarReplacement(location, json)) {
                        for (ItemStack stack : tabItems) {
                            if (!isExistingSentinel(stack)) {
                                remappedItems.put(stack.getItem(), firstSelectorForReplacement(location, json));
                            }
                        }
                    }
                } else {
                    CreativeModeTab tab = creator.createTab(json, tabItems);
                    for (ItemStack stack : tabItems) {
                        if (!isExistingSentinel(stack)) {
                            remappedItems.put(stack.getItem(), tab.getRecipeFolderName());
                        }
                    }
                    customTabs.add(tab);
                    if (Boolean.TRUE.equals(json.searchBar))
                        searchableTabs.addAll(tabAliases(tab));
                }
                loadedTabs++;
            } catch (Exception e) {
                failedTabs++;
                ModConstants.logger.error("Failed to process creative tab {}: {}", location, e.getMessage(), e);
            }
        });

        reOrderTabs();
    }

    private static List<ItemStack> parseTabItems(ResourceLocation location, CustomCreativeTab json) {
        final List<ItemStack> tabItems = new ArrayList<>();
        List<CustomCreativeTab.TabItem> jsonItems = json.tabItems == null ? Collections.emptyList() : json.tabItems;
        for (CustomCreativeTab.TabItem item : jsonItems) {
            if (item == null)
                continue;

            if (item.tag != null && !item.tag.isBlank()) {
                ResourceLocation tagId = ResourceLocation.tryParse(item.tag);
                if (tagId == null) {
                    ModConstants.logger.warn("Skipping invalid tag {} in {}", item.tag, location);
                    continue;
                }
                net.minecraft.tags.TagKey<Item> tagKey = net.minecraft.tags.TagKey.create(Registry.ITEM_REGISTRY, tagId);
                for (net.minecraft.core.Holder<Item> holder : Registry.ITEM.getTagOrEmpty(tagKey)) {
                    Item stackItem = holder.value();
                    ItemStack stack = new ItemStack(stackItem);
                    if (item.hideOldTab)
                        hiddenStacks.add(stack.getItem());
                    applyItemNbt(item, stack);
                    tabItems.add(stack);
                }
                continue;
            }

            if (item.name == null || item.name.isBlank()) {
                ModConstants.logger.warn("Skipping item without a name or tag in {}", location);
                continue;
            }

            if (item.name.equalsIgnoreCase("existing")) {
                json.keepExisting = true;
                addExistingExceptions(location, json, item);
                ItemStack sentinel = new ItemStack(net.minecraft.world.item.Items.COMMAND_BLOCK);
                CompoundTag sentinelTag = new CompoundTag();
                sentinelTag.putBoolean("MCT_EXISTING", true);
                sentinel.setTag(sentinelTag);
                sentinel.setHoverName(Component.literal("MCT_EXISTING"));
                tabItems.add(sentinel);
                continue;
            }

            ItemStack stack = getItemStack(item.name);
            if (stack.isEmpty())
                continue;

            if (item.hideOldTab)
                hiddenStacks.add(stack.getItem());

            applyItemNbt(item, stack);
            tabItems.add(stack);
        }
        return tabItems;
    }

    private static void applyItemNbt(CustomCreativeTab.TabItem item, ItemStack stack) {
        if (item.nbt == null || item.nbt.isEmpty())
            return;
        try {
            CompoundTag tag = TagParser.parseTag(item.nbt);
            stack.setTag(tag);
            if (tag.contains("customName"))
                stack.setHoverName(Component.literal(tag.getString("customName")));
        } catch (CommandSyntaxException e) {
            ModConstants.logger.error("Failed to Process NBT for Item {}", item.name, e);
        }
    }

    private static void addExistingExceptions(ResourceLocation location, CustomCreativeTab json, CustomCreativeTab.TabItem item) {
        JsonElement exceptions = item.except;
        if (exceptions == null || exceptions.isJsonNull())
            return;
        if (exceptions.isJsonArray()) {
            for (JsonElement exception : exceptions.getAsJsonArray())
                addExistingException(location, json, exception);
            return;
        }
        addExistingException(location, json, exceptions);
    }

    private static void addExistingException(ResourceLocation location, CustomCreativeTab json, JsonElement exception) {
        if (exception == null || exception.isJsonNull())
            return;
        if (!exception.isJsonPrimitive() || !exception.getAsJsonPrimitive().isString()) {
            ModConstants.logger.warn("Skipping invalid existing exception in {}", location);
            return;
        }
        ItemStack stack = getItemStack(exception.getAsString());
        if (stack.isEmpty()) {
            ModConstants.logger.warn("Unknown item {} in existing exception from {}", exception.getAsString(), location);
            return;
        }
        ResourceLocation itemId = Registry.ITEM.getKey(stack.getItem());
        json.existingExceptions.add(itemId.toString());
    }

    public static void loadDisabledTabs(Map<ResourceLocation, Resource> resourceMap) {
       resourceMap.entrySet().stream().sorted(Comparator.comparing(entry -> entry.getKey().toString())).forEach(entry -> {
           ResourceLocation location = entry.getKey();
           Resource resource = entry.getValue();
           ModConstants.logger.info("Processing {}", location.toString());
           try (InputStream stream = resource.open()) {
               DisabledTabsJsonHelper json = GSON.fromJson(new InputStreamReader(stream), DisabledTabsJsonHelper.class);
               applyDisabledTabs(location, json);
           } catch (Exception e) {
               ModConstants.logger.error("Failed to process disabled tabs for {}", location, e);
           }
       });
    }

    public static int processPolicyEntry(ResourceLocation location, Resource resource) {
        try (InputStream stream = resource.open()) {
            JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            int policies = 0;
            if (json.has("disabled_tabs")) {
                ModConstants.logger.info("Processing {}", location.toString());
                applyDisabledTabs(location, GSON.fromJson(json, DisabledTabsJsonHelper.class));
                policies |= DISABLED_POLICY;
            }
            if (json.has("tabs")) {
                ModConstants.logger.info("Processing {}", location.toString());
                OrderedTabs tabs = GSON.fromJson(json, OrderedTabs.class);
                if (tabs != null && tabs.tabs != null) {
                    for (String selector : tabs.tabs)
                        addOrderedSelector(selector);
                }
                policies |= ORDERED_POLICY;
            }
            return policies;
        } catch (Exception e) {
            ModConstants.logger.error("Failed to inspect creative tab policy {}", location, e);
            return 0;
        }
    }

    private static void applyDisabledTabs(ResourceLocation location, DisabledTabsJsonHelper json) {
        if (json == null)
            throw new IllegalArgumentException("JSON is empty");
        if (json.disabledTabs == null)
            return;
        for (String selector : json.disabledTabs) {
            if (selector != null && selector.equalsIgnoreCase("existing")) {
                for (CreativeModeTab tab : baseTabs()) {
                    for (String alias : tabAliases(tab)) {
                        if (!PROTECTED_TABS.contains(alias))
                            disabledTabs.add(alias);
                    }
                }
                continue;
            }
            Set<String> aliases = selectorAliases(selector);
            if (aliases.isEmpty())
                continue;
            if (aliases.stream().anyMatch(PROTECTED_TABS::contains)) {
                ModConstants.logger.warn("Ignoring attempt to disable protected creative tab {}", selector);
                continue;
            }
            disabledTabs.addAll(aliases);
        }
    }

    public static void loadOrderedTabs(Map<ResourceLocation, Resource> resourceMap) {
        resourceMap.entrySet().stream().sorted(Comparator.comparing(entry -> entry.getKey().toString())).forEach(entry -> {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();
            ModConstants.logger.info("Processing {}", location.toString());
            try (InputStream stream = resource.open()) {
                OrderedTabs tabs = GSON.fromJson(new InputStreamReader(stream), OrderedTabs.class);
                if (tabs == null || tabs.tabs == null)
                    throw new IllegalArgumentException("Missing tabs array");
                for (String selector : tabs.tabs)
                    addOrderedSelector(selector);
            } catch (Exception e) {
                ModConstants.logger.error("Failed to process ordered tabs for {}", location, e);
            }
        });
    }

    private static void addOrderedSelector(String selector) {
        if (selector != null && selector.equalsIgnoreCase("existing")) {
            reorderedTabs.add("existing");
            return;
        }
        reorderedTabs.addAll(selectorAliases(selector));
    }

    private static void reOrderTabs() {
        List<CreativeModeTab> oldTabs = new ArrayList<>();
        oldTabs.addAll(Arrays.asList(baseTabs()));
        oldTabs.addAll(customTabs);

        for (Pair<CustomCreativeTab, List<ItemStack>> replacement : replacementEntries()) {
            boolean matched = oldTabs.stream().anyMatch(tab -> replacementTab(tab.getRecipeFolderName())
                    .map(candidate -> candidate.equals(replacement))
                    .orElse(false));
            if (!matched)
                ModConstants.logger.warn("Creative tab replacement target {} did not match any loaded tab", replacement.getLeft().tabReplace);
        }

        Set<String> knownTabs = new HashSet<>();
        for (CreativeModeTab tab : oldTabs)
            knownTabs.addAll(tabAliases(tab));
        for (String selector : disabledTabs) {
            if (!knownTabs.contains(selector)) {
                ModConstants.logger.warn("Disabled tab selector {} did not match any creative tab", selector);
            }
        }

        LinkedHashSet<CreativeModeTab> filteredTabs = new LinkedHashSet<>();
        int nextTabId = 0;
        if (!reorderedTabs.isEmpty()) {
            for (String orderedTab : reorderedTabs) {
                if (!orderedTab.equalsIgnoreCase("existing")) {
                    Optional<CreativeModeTab> tab = oldTabs.stream()
                            .filter(candidate -> tabAliases(candidate).contains(orderedTab))
                            .findFirst();
                    if (tab.isPresent()) {
                        nextTabId = processTab(tab.get(), nextTabId, filteredTabs);
                    } else {
                        ModConstants.logger.warn("Ordered tab selector {} did not match any creative tab", orderedTab);
                    }
                } else {
                    for (CreativeModeTab tab : oldTabs) {
                        if (!isExplicitlyOrdered(tab))
                            nextTabId = processTab(tab, nextTabId, filteredTabs);
                    }
                }
            }
        } else {
            for (CreativeModeTab tab : oldTabs) {
                nextTabId = processTab(tab, nextTabId, filteredTabs);
            }
        }

        for (CreativeModeTab tab : customTabs) {
            if (!filteredTabs.contains(tab) && !isDisabled(tab)) {
                ((CreativeModeTabAccessor) tab).setId(nextTabId++);
                filteredTabs.add(tab);
            }
        }

        lockProtectedTabPositions(filteredTabs);

        CreativeModeTab[] newTabs = filteredTabs.toArray(new CreativeModeTab[0]);
        currentTabs.clear();
        currentTabs.addAll(Arrays.asList(newTabs));
        PlatformServices.helper.setNewTabs(newTabs);
        restoreSelectedTab(newTabs);
    }

    private static void lockProtectedTabPositions(LinkedHashSet<CreativeModeTab> filteredTabs) {
        List<CreativeModeTab> ordered = new ArrayList<>(filteredTabs);
        insertProtectedTab(ordered, CreativeModeTab.TAB_HOTBAR, 4);
        insertProtectedTab(ordered, CreativeModeTab.TAB_SEARCH, 5);
        insertProtectedTab(ordered, CreativeModeTab.TAB_INVENTORY, 11);
        filteredTabs.clear();
        filteredTabs.addAll(ordered);

        for (int i = 0; i < ordered.size(); i++) {
            ((CreativeModeTabAccessor) ordered.get(i)).setId(i);
        }
    }

    private static void insertProtectedTab(List<CreativeModeTab> ordered, CreativeModeTab tab, int index) {
        if (tab == null || isDisabled(tab))
            return;
        ordered.remove(tab);
        ordered.add(Math.min(index, ordered.size()), tab);
    }

    private static void restoreSelectedTab(CreativeModeTab[] tabs) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof CreativeModeInventoryScreen)) {
            selectedTabBeforeReload = null;
            return;
        }

        CreativeModeTab selection = selectedTabBeforeReload;
        if (selection == null || !currentTabs.contains(selection))
            selection = tabs.length == 0 ? null : tabs[0];
        CreativeInventoryScreenAccessor.setSelectedTab(selection == null ? -1 : selection.getId());
        selectedTabBeforeReload = null;
        refreshOpenScreen();
    }

    private static CreativeModeTab[] baseTabs() {
        return tabsBefore != null ? tabsBefore : CreativeModeTab.TABS;
    }

    private static int processTab(CreativeModeTab tab, int id, LinkedHashSet<CreativeModeTab> filteredTabs) {
        if (!isProtectedTab(tab) && !isDisabled(tab) && !filteredTabs.contains(tab)) {
            ((CreativeModeTabAccessor) tab).setId(id++);
            filteredTabs.add(tab);
        }
        return id;
    }

    private static boolean isExplicitlyOrdered(CreativeModeTab tab) {
        return tabAliases(tab).stream().anyMatch(reorderedTabs::contains);
    }

    private static boolean isProtectedTab(CreativeModeTab tab) {
        return tab == CreativeModeTab.TAB_SEARCH
                || tab == CreativeModeTab.TAB_HOTBAR
                || tab == CreativeModeTab.TAB_INVENTORY;
    }

    private static boolean hasTabReplace(CustomCreativeTab json) {
        return json.tabReplace != null && !json.tabReplace.isBlank();
    }

    private static String firstSelectorForReplacement(ResourceLocation location, CustomCreativeTab json) {
        if (hasTabReplace(json))
            return selectorAliases(json.tabReplace).stream().findFirst().orElse(json.tabReplace);
        return fileToTab(location.getPath()).toLowerCase(Locale.ROOT);
    }

    private static String legacyFileSelector(ResourceLocation location) {
        String selector = fileToTab(location.getPath()).toLowerCase(Locale.ROOT);
        if (!selector.startsWith("itemgroup_") && !selector.startsWith("itemgroup."))
            throw new IllegalArgumentException("Legacy replacement filenames must use a translation key, such as itemgroup_buildingblocks.json");
        return selector;
    }

    private static void registerReplacement(ResourceLocation location, CustomCreativeTab json, List<ItemStack> stacks) {
        String selector = hasTabReplace(json) ? json.tabReplace : legacyFileSelector(location);
        Set<String> aliases = selectorAliases(selector);
        if (aliases.isEmpty())
            throw new IllegalArgumentException("Missing replacement target");
        Pair<CustomCreativeTab, List<ItemStack>> replacement = Pair.of(json, stacks);
        aliases.forEach(alias -> replacedTabs.put(alias, replacement));
        if (json.searchBar != null && !aliases.contains("hotbar")
                && !aliases.contains("itemgroup.hotbar")
                && !aliases.contains("itemgroup_hotbar")) {
            if (json.searchBar)
                searchableTabs.addAll(aliases);
            else
                searchDisabledTabs.addAll(aliases);
        }
    }

    public static boolean isDisabled(CreativeModeTab tab) {
        return matchesTab(disabledTabs, tab);
    }

    public static boolean isSearchable(CreativeModeTab tab) {
        return tab == CreativeModeTab.TAB_SEARCH || matchesTab(searchableTabs, tab);
    }

    public static boolean isSearchBarDisabled(CreativeModeTab tab) {
        return tab != null && matchesTab(searchDisabledTabs, tab);
    }

    public static boolean shouldHandleSearch(CreativeModeTab tab) {
        return tab != null && !isProtectedSearchTab(tab)
                && !isSearchBarDisabled(tab)
                && matchesTab(searchableTabs, tab);
    }

    private static boolean isHotbarReplacement(ResourceLocation location, CustomCreativeTab json) {
        String selector = hasTabReplace(json) ? json.tabReplace : legacyFileSelector(location);
        return selectorAliases(selector).contains("hotbar");
    }

    private static boolean isProtectedSearchName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        return normalized.equals("search") || normalized.equals("hotbar") || normalized.equals("inventory");
    }

    public static boolean isProtectedSearchTab(CreativeModeTab tab) {
        return tab == CreativeModeTab.TAB_SEARCH
                || tab == CreativeModeTab.TAB_HOTBAR
                || tab == CreativeModeTab.TAB_INVENTORY
                || isProtectedSearchName(tab.getRecipeFolderName());
    }

    public static boolean isHidden(ItemStack stack) {
        return hiddenStacks.contains(stack.getItem());
    }

    public static boolean shouldKeepExistingItem(CustomCreativeTab json, ItemStack stack) {
        ResourceLocation itemId = Registry.ITEM.getKey(stack.getItem());
        return itemId == null || !json.existingExceptions.contains(itemId.toString());
    }

    public static boolean isExistingSentinel(ItemStack stack) {
        return stack.getItem() == Items.COMMAND_BLOCK
                && stack.hasTag() && stack.getTag().getBoolean("MCT_EXISTING");
    }

    public static List<CreativeModeTab> orderedVisibleTabs() {
        if (currentTabs.isEmpty())
            return Arrays.asList(CreativeModeTab.TABS);
        return currentTabs;
    }

    public static Collection<Pair<CustomCreativeTab, List<ItemStack>>> replacementEntries() {
        return new LinkedHashSet<>(replacedTabs.values());
    }

    public static void refreshOpenScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof CreativeModeInventoryScreen)
            minecraft.screen.resize(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    }

    public static TabReloadResult createReloadResult(int discoveredTabs, int disabledFiles, int orderedFiles) {
        return new TabReloadResult(discoveredTabs, loadedTabs, skippedTabs, failedTabs, disabledFiles, orderedFiles, CreativeModeTab.TABS.length);
    }

    public static void clearTabs() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof CreativeModeInventoryScreen screen) {
            int selected = screen.getSelectedTab();
            selectedTabBeforeReload = selected >= 0 && selected < CreativeModeTab.TABS.length
                    ? CreativeModeTab.TABS[selected]
                    : null;
        } else {
            selectedTabBeforeReload = null;
        }
        loadedTabs = 0;
        skippedTabs = 0;
        failedTabs = 0;
        hiddenStacks.clear();
        disabledTabs.clear();
        reorderedTabs.clear();
        searchableTabs.clear();
        searchDisabledTabs.clear();
        currentTabs.clear();
        if (tabsBefore != null)
            PlatformServices.helper.setNewTabs(tabsBefore);
        customTabs.clear();
        replacedTabs.clear();
        remappedItems.clear();
    }

}
