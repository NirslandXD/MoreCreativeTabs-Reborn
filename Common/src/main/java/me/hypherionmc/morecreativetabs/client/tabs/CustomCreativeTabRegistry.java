package me.hypherionmc.morecreativetabs.client.tabs;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.hypherionmc.morecreativetabs.ModConstants;
import me.hypherionmc.morecreativetabs.client.data.CustomCreativeTabJsonHelper;
import me.hypherionmc.morecreativetabs.client.data.DisabledTabsJsonHelper;
import me.hypherionmc.morecreativetabs.client.data.OrderedTabsJsonHelper;
import me.hypherionmc.morecreativetabs.mixin.accessor.CreativeModeTabAccessor;
import me.hypherionmc.morecreativetabs.mixin.accessor.CreativeModeTabsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static me.hypherionmc.morecreativetabs.utils.CreativeTabUtils.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CustomCreativeTabRegistry {

    public static final CustomCreativeTabRegistry INSTANCE = new CustomCreativeTabRegistry();
    private final Gson gson = new Gson();

    private final List<CreativeModeTab> vanillaTabs = new ArrayList<>();
    private final LinkedHashSet<CreativeModeTab> customTabs = new LinkedHashSet<>();
    private final Set<String> disabledTabs = new HashSet<>();
    private final LinkedHashSet<String> tabOrder = new LinkedHashSet<>();
    private final LinkedList<CreativeModeTab> currentTabs = new LinkedList<>();
    private final LinkedHashMap<String, Pair<CustomCreativeTabJsonHelper, List<ItemStack>>> replacedTabs = new LinkedHashMap<>();

    private final HashMap<CreativeModeTab, List<ItemStack>> tabItems = new HashMap<>();
    private final Map<CreativeModeTab, ResourceLocation> tabRegistryIds = new IdentityHashMap<>();
    private final Set<Item> hiddenItems = new HashSet<>();
    private final Set<String> searchableTabs = new HashSet<>();
    private final Set<String> searchDisabledTabs = new HashSet<>();
    private static final Set<String> PROTECTED_TABS = Set.of(
            "itemgroup.search", "itemgroup_search", "search", "minecraft:search", "minecraft.search", "minecraft_search",
            "itemgroup.hotbar", "itemgroup_hotbar", "hotbar", "minecraft:hotbar", "minecraft.hotbar", "minecraft_hotbar",
            "itemgroup.inventory", "itemgroup_inventory", "inventory", "minecraft:inventory", "minecraft.inventory", "minecraft_inventory"
    );
    private int loadedTabs;
    private int skippedTabs;
    private int failedTabs;

    @Setter
    private boolean showTabNames = false;

    private boolean wasReloaded = false;

    private final CreativeModeTab operatorTab = CreativeModeTabsAccessor.getOpBlockTab();

    public void processEntries(Map<ResourceLocation, Resource> entries) {
        for (Map.Entry<ResourceLocation, Resource> entry : sortedEntries(entries)) {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();

            ModConstants.logger.info("Processing {}", location.toString());

            try (InputStream stream = resource.open()) {
                CustomCreativeTabJsonHelper json = gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), CustomCreativeTabJsonHelper.class);
                if (json == null)
                    throw new IllegalArgumentException("JSON is empty");
                boolean replacementTab = hasTabReplace(json) || json.isReplace();
                if (json.getTabEnabled() == null)
                    throw new IllegalArgumentException("Missing tab_enabled");
                if (!json.getTabEnabled()) {
                    skippedTabs++;
                    continue;
                }
                if (!replacementTab) {
                    if (json.getTabName() == null || json.getTabName().isBlank())
                        throw new IllegalArgumentException("Missing tab_name");
                    if (json.getTabItems() == null)
                        throw new IllegalArgumentException("Missing tab_items");
                    if (json.getTabIcon() == null || json.getTabIcon().getName() == null || json.getTabIcon().getName().isBlank())
                        throw new IllegalArgumentException("Missing tab_stack.name");
                }

                ArrayList<ItemStack> stacks = parseTabItems(location, json);

                if (replacementTab) {
                    registerReplacement(location, json, stacks);
                } else {
                    CreativeModeTab.Builder builder = new CreativeModeTab.Builder(null, -1);
                    builder.title(Component.translatable(prefix(json.getTabName())));
                    builder.icon(makeTabIcon(json));

                    if (Boolean.TRUE.equals(json.getSearchBar()))
                        builder.backgroundSuffix("item_search.png");

                    if (Boolean.TRUE.equals(json.getSearchBar())) {
                        try {
                            builder.getClass().getMethod("withSearchBar").invoke(builder);
                        } catch (ReflectiveOperationException ignored) {
                        }
                    }

                    CreativeModeTab tab = builder.build();
                    customTabs.add(tab);
                    tabItems.put(tab, stacks);
                    if (Boolean.TRUE.equals(json.getSearchBar()))
                        searchableTabs.add(getTabIdentifier(tab));
                }
                loadedTabs++;
            } catch (Exception e) {
                failedTabs++;
                ModConstants.logger.error("Failed to process creative tab {}: {}", location, e.getMessage(), e);
            }
        }

        reorderTabs();
    }

    private ArrayList<ItemStack> parseTabItems(ResourceLocation location, CustomCreativeTabJsonHelper json) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        List<CustomCreativeTabJsonHelper.TabItem> jsonItems = json.getTabItems() == null ? Collections.emptyList() : json.getTabItems();
        for (CustomCreativeTabJsonHelper.TabItem item : jsonItems) {
            if (item == null) {
                continue;
            }

            if (item.getTag() != null && !item.getTag().isBlank()) {
                ResourceLocation tagId = ResourceLocation.tryParse(item.getTag());
                if (tagId == null) {
                    ModConstants.logger.warn("Skipping invalid tag {} in {}", item.getTag(), location);
                    continue;
                }
                net.minecraft.tags.TagKey<Item> tagKey = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, tagId);
                for (net.minecraft.core.Holder<Item> holder : net.minecraft.core.registries.BuiltInRegistries.ITEM.getTagOrEmpty(tagKey)) {
                    ItemStack stack = new ItemStack(holder.value());
                    if (item.isHideOldTab())
                        hiddenItems.add(stack.getItem());
                    applyItemNbt(item, stack);
                    stacks.add(stack);
                }
                continue;
            }

            if (item.getName() == null || item.getName().isBlank()) {
                ModConstants.logger.warn("Skipping item without a name or tag in {}", location);
                continue;
            }
            if (item.getName().equalsIgnoreCase("existing")) {
                json.setKeepExisting(true);
                addExistingExceptions(location, json, item);

                ItemStack sentinel = new ItemStack(net.minecraft.world.item.Items.COMMAND_BLOCK);
                CompoundTag sentinelTag = new CompoundTag();
                sentinelTag.putBoolean("MCT_EXISTING", true);
                sentinel.setTag(sentinelTag);
                sentinel.setHoverName(Component.literal("MCT_EXISTING"));
                stacks.add(sentinel);
                continue;
            }

            ItemStack stack = getItemStack(item.getName());
            if (stack.isEmpty())
                continue;

            if (item.isHideOldTab())
                hiddenItems.add(stack.getItem());

            applyItemNbt(item, stack);
            stacks.add(stack);
        }
        return stacks;
    }

    private void applyItemNbt(CustomCreativeTabJsonHelper.TabItem item, ItemStack stack) {
        if (item.getNbt() == null || item.getNbt().isEmpty())
            return;

        try {
            CompoundTag tag = TagParser.parseTag(item.getNbt());
            stack.setTag(tag);

            if (tag.contains("customName"))
                stack.setHoverName(Component.literal(tag.getString("customName")));
        } catch (CommandSyntaxException e) {
            ModConstants.logger.error("Failed to Process NBT for Item {}", item.getName(), e);
        }
    }

    private void addExistingExceptions(ResourceLocation location, CustomCreativeTabJsonHelper json, CustomCreativeTabJsonHelper.TabItem item) {
        JsonElement exceptions = item.getExcept();
        if (exceptions == null || exceptions.isJsonNull())
            return;

        if (exceptions.isJsonArray()) {
            for (JsonElement exception : exceptions.getAsJsonArray()) {
                addExistingException(location, json, exception);
            }
            return;
        }

        addExistingException(location, json, exceptions);
    }

    private void addExistingException(ResourceLocation location, CustomCreativeTabJsonHelper json, JsonElement exception) {
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

        ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        json.getExistingExceptions().add(itemId.toString());
    }

    public void loadDisabledTabs(Map<ResourceLocation, Resource> entries) {
        for (Map.Entry<ResourceLocation, Resource> entry : sortedEntries(entries)) {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();
            ModConstants.logger.info("Processing {}", location);
            try (InputStream stream = resource.open()) {
                DisabledTabsJsonHelper json = gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), DisabledTabsJsonHelper.class);
                applyDisabledTabs(location, json);
            } catch (Exception e) {
                ModConstants.logger.error("Failed to process disabled tabs for {}", location, e);
            }
        }
    }

    public PolicyFileResult processPolicyEntry(ResourceLocation location, Resource resource) {
        try (InputStream stream = resource.open()) {
            JsonElement parsed = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            if (!parsed.isJsonObject())
                return PolicyFileResult.NON_POLICY;

            JsonObject json = parsed.getAsJsonObject();
            boolean disabledFile = json.has("disabled_tabs");
            boolean orderedFile = json.has("tabs");
            if (!disabledFile && !orderedFile)
                return PolicyFileResult.NON_POLICY;

            ModConstants.logger.info("Processing {}", location);
            boolean disabledApplied = false;
            boolean orderedApplied = false;
            boolean failed = false;
            if (disabledFile) {
                try {
                    applyDisabledTabs(location, gson.fromJson(json, DisabledTabsJsonHelper.class));
                    disabledApplied = true;
                } catch (Exception e) {
                    failed = true;
                    ModConstants.logger.error("Failed to process disabled tabs for {}", location, e);
                }
            }
            if (orderedFile) {
                try {
                    applyOrderedTabs(gson.fromJson(json, OrderedTabsJsonHelper.class));
                    orderedApplied = true;
                } catch (Exception e) {
                    failed = true;
                    ModConstants.logger.error("Failed to process ordered tabs for {}", location, e);
                }
            }
            if (failed)
                failedTabs++;
            return new PolicyFileResult(true, false, disabledApplied, orderedApplied, failed);
        } catch (Exception e) {
            failedTabs++;
            ModConstants.logger.error("Failed to inspect creative tab policy {}", location, e);
            return PolicyFileResult.MALFORMED;
        }
    }

    private void applyDisabledTabs(ResourceLocation location, DisabledTabsJsonHelper json) {
        if (json == null)
            throw new IllegalArgumentException("JSON is empty");
        if (json.getDisabledTabs() == null)
            throw new IllegalArgumentException("Missing disabled_tabs array");
        for (String selector : json.getDisabledTabs()) {
            if (selector != null && selector.equalsIgnoreCase("existing")) {
                for (CreativeModeTab tab : vanillaTabs) {
                    if (!isProtectedTab(tab))
                        disabledTabs.addAll(tabAliases(tab));
                }
                continue;
            }

            Set<String> aliases = selectorAliases(selector);
            if (aliases.isEmpty())
                continue;
            if (isProtectedSelector(aliases)) {
                ModConstants.logger.warn("Ignoring attempt to disable protected creative tab {}", selector);
                continue;
            }
            disabledTabs.addAll(aliases);
        }
    }

    public void loadOrderedTabs(Map<ResourceLocation, Resource> resourceMap) {
        for (Map.Entry<ResourceLocation, Resource> entry : sortedEntries(resourceMap)) {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();
            ModConstants.logger.info("Processing {}", location);
            try (InputStream stream = resource.open()) {
                applyOrderedTabs(gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), OrderedTabsJsonHelper.class));
            } catch (Exception e) {
                ModConstants.logger.error("Failed to process ordered tabs for {}", location, e);
            }
        }
    }

    private void applyOrderedTabs(OrderedTabsJsonHelper tabs) {
        if (tabs == null || tabs.getTabs() == null)
            throw new IllegalArgumentException("Missing tabs array");
        for (String selector : tabs.getTabs()) {
            if (selector != null && selector.equalsIgnoreCase("existing")) {
                tabOrder.add("existing");
                continue;
            }
            tabOrder.addAll(selectorAliases(selector));
        }
    }

    private void reorderTabs() {
        List<CreativeModeTab> oldTabs = new ArrayList<>();
        oldTabs.addAll(vanillaTabs);
        oldTabs.addAll(customTabs);

        for (Pair<CustomCreativeTabJsonHelper, List<ItemStack>> replacement : replacementEntries()) {
            if (oldTabs.stream().noneMatch(tab -> replacementTab(tab)
                    .map(candidate -> candidate.equals(replacement))
                    .orElse(false))) {
                ModConstants.logger.warn("Creative tab replacement target {} did not match any loaded tab",
                        replacement.getLeft().getTabReplace());
            }
        }

        Set<String> knownTabs = oldTabs.stream().flatMap(tab -> tabAliases(tab).stream()).collect(java.util.stream.Collectors.toSet());
        disabledTabs.stream().sorted().filter(selector -> !knownTabs.contains(selector))
                .forEach(selector -> ModConstants.logger.warn("Disabled tab selector {} did not match any creative tab", selector));

        LinkedHashSet<CreativeModeTab> filteredTabs = new LinkedHashSet<>();

        if (!tabOrder.isEmpty()) {
            Set<CreativeModeTab> explicitlyOrderedTabs = tabOrder.stream()
                    .filter(selector -> !selector.equalsIgnoreCase("existing"))
                    .flatMap(selector -> oldTabs.stream().filter(tab -> tabAliases(tab).contains(selector)))
                    .collect(java.util.stream.Collectors.toSet());

            for (String orderedTab : tabOrder) {
                if (!orderedTab.equalsIgnoreCase("existing")) {
                    Optional<CreativeModeTab> tab = oldTabs.stream()
                            .filter(candidate -> tabAliases(candidate).contains(orderedTab))
                            .findFirst();
                    if (tab.isPresent()) {
                        processTab(tab.get(), filteredTabs);
                    } else {
                        ModConstants.logger.warn("Ordered tab selector {} did not match any creative tab", orderedTab);
                    }
                } else {
                    for (CreativeModeTab tab : oldTabs) {
                        if (!explicitlyOrderedTabs.contains(tab))
                            processTab(tab, filteredTabs);
                    }
                }
            }
        } else {
            for (CreativeModeTab tab : oldTabs) {
                processTab(tab, filteredTabs);
            }
        }

        // Don't disable the Survival Inventory, Search and Hotbar
        filteredTabs.add(CreativeModeTabsAccessor.getSearchTab());
        filteredTabs.add(CreativeModeTabsAccessor.getHotbarTab());
        filteredTabs.add(CreativeModeTabsAccessor.getInventoryTab());

        for (CreativeModeTab customTab : customTabs) {
            if (!isDisabled(customTab))
                filteredTabs.add(customTab);
        }

        currentTabs.clear();
        currentTabs.addAll(filteredTabs.stream().filter(Objects::nonNull).toList());
        if (currentTabs.isEmpty())
            currentTabs.addAll(vanillaTabs.stream().filter(Objects::nonNull).toList());

        CreativeTabSearchHelper.configure(CreativeModeTabsAccessor.getSearchTab());

    }

    private void processTab(CreativeModeTab tab, LinkedHashSet<CreativeModeTab> filteredTabs) {
        if (!isDisabled(tab))
            filteredTabs.add(tab);
    }

    private boolean hasTabReplace(CustomCreativeTabJsonHelper json) {
        return json.getTabReplace() != null && !json.getTabReplace().isBlank();
    }

    private String legacyFileSelector(ResourceLocation location) {
        String selector = fileToTab(location.getPath()).toLowerCase(Locale.ROOT);
        if (!selector.startsWith("itemgroup_") && !selector.startsWith("itemgroup."))
            throw new IllegalArgumentException("Legacy replacement filenames must use a translation key, such as itemgroup_buildingblocks.json");
        return selector;
    }

    private void registerReplacement(ResourceLocation location, CustomCreativeTabJsonHelper json, List<ItemStack> stacks) {
        String selector = hasTabReplace(json)
                ? json.getTabReplace()
                : legacyFileSelector(location);
        Set<String> aliases = selectorAliases(selector);
        if (aliases.isEmpty())
            throw new IllegalArgumentException("Missing replacement target");

        Pair<CustomCreativeTabJsonHelper, List<ItemStack>> replacement = Pair.of(json, stacks);
        aliases.forEach(alias -> replacedTabs.put(alias, replacement));
        if (json.getSearchBar() != null
                && !aliases.contains("hotbar")
                && !aliases.contains("itemgroup.hotbar")
                && !aliases.contains("itemgroup_hotbar")) {
            if (json.getSearchBar())
                searchableTabs.addAll(aliases);
            else
                searchDisabledTabs.addAll(aliases);
        }
    }

    public Optional<Pair<CustomCreativeTabJsonHelper, List<ItemStack>>> replacementTab(CreativeModeTab tab) {
        for (String alias : tabAliases(tab)) {
            Pair<CustomCreativeTabJsonHelper, List<ItemStack>> replacement = replacedTabs.get(alias);
            if (replacement != null)
                return Optional.of(replacement);
        }
        return Optional.empty();
    }

    public String getTabIdentifier(CreativeModeTab tab) {
        return getTabKey(((CreativeModeTabAccessor) tab).getInternalDisplayName()).toLowerCase(Locale.ROOT);
    }

    public boolean isDisabled(CreativeModeTab tab) {
        return matchesTab(disabledTabs, tab);
    }

    public boolean isSearchable(CreativeModeTab tab) {
        return tab != null && !isVanillaSearch(tab) && matchesTab(searchableTabs, tab);
    }

    public boolean isSearchBarDisabled(CreativeModeTab tab) {
        return tab != null && matchesTab(searchDisabledTabs, tab);
    }

    public boolean shouldHandleSearch(CreativeModeTab tab) {
        return tab != null
                && !isProtectedSearchTab(tab)
                && !isSearchBarDisabled(tab)
                && matchesTab(searchableTabs, tab);
    }

    private boolean isProtectedSearchTab(CreativeModeTab tab) {
        return tab == CreativeModeTabsAccessor.getSearchTab()
                || tab == CreativeModeTabsAccessor.getHotbarTab()
                || tab == CreativeModeTabsAccessor.getInventoryTab();
    }

    public boolean isVanillaSearch(CreativeModeTab tab) {
        return tab == CreativeModeTabsAccessor.getSearchTab();
    }

    public boolean isHidden(ItemStack stack) {
        return hiddenItems.contains(stack.getItem());
    }

    public boolean shouldKeepExistingItem(CustomCreativeTabJsonHelper json, ItemStack stack) {
        ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return itemId == null || !json.getExistingExceptions().contains(itemId.toString());
    }

    public boolean isExistingSentinel(ItemStack stack) {
        return stack.is(Items.COMMAND_BLOCK) && stack.hasTag() && stack.getTag().getBoolean("MCT_EXISTING");
    }

    private boolean matchesTab(Set<String> selectors, CreativeModeTab tab) {
        return tabAliases(tab).stream().anyMatch(selectors::contains);
    }

    private boolean isProtectedSelector(Set<String> aliases) {
        return aliases.stream().anyMatch(PROTECTED_TABS::contains)
                || matchesTab(aliases, CreativeModeTabsAccessor.getSearchTab())
                || matchesTab(aliases, CreativeModeTabsAccessor.getHotbarTab())
                || matchesTab(aliases, CreativeModeTabsAccessor.getInventoryTab());
    }

    private boolean isProtectedTab(CreativeModeTab tab) {
        return tab == CreativeModeTabsAccessor.getSearchTab()
                || tab == CreativeModeTabsAccessor.getHotbarTab()
                || tab == CreativeModeTabsAccessor.getInventoryTab();
    }

    private Set<String> tabAliases(CreativeModeTab tab) {
        LinkedHashSet<String> aliases = new LinkedHashSet<>();
        String translationKey = getTabKey(((CreativeModeTabAccessor) tab).getInternalDisplayName()).toLowerCase(Locale.ROOT);
        aliases.add(translationKey);
        if (translationKey.startsWith("itemgroup."))
            aliases.add(translationKey.substring("itemgroup.".length()));
        aliases.add(translationKey.replace(".", "_"));
        aliases.add(normalizeLegacyReplacementKey(translationKey));
        aliases.add(normalizeLegacyReplacementKey(translationKey.replace(".", "_")));

        ResourceLocation registryId = tabRegistryIds.get(tab);
        if (registryId != null) {
            String namespace = registryId.getNamespace().toLowerCase(Locale.ROOT);
            String path = registryId.getPath().toLowerCase(Locale.ROOT);
            aliases.add(registryId.toString().toLowerCase(Locale.ROOT));
            aliases.add(path);
            aliases.add(namespace + "." + path);
            aliases.add(namespace + "_" + path);
            aliases.add("itemgroup." + namespace + "." + path);
            aliases.add("itemgroup_" + namespace + "_" + path);
        }

        aliases.remove(null);
        return aliases;
    }

    private Set<String> selectorAliases(String selector) {
        LinkedHashSet<String> aliases = new LinkedHashSet<>();
        String normalized = normalizeTabSelector(selector);
        if (normalized != null)
            aliases.add(normalized);
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

    private String normalizeTabSelector(String selector) {
        if (selector == null || selector.isBlank())
            return null;

        return selector.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeLegacyReplacementKey(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        normalized = switch (normalized) {
            case "itemgroup_buildingblocks", "itemgroup.buildingblocks" -> "itemgroup_building_blocks";
            case "itemgroup_coloredblocks", "itemgroup.coloredblocks" -> "itemgroup_colored_blocks";
            case "itemgroup_natural", "itemgroup.natural" -> "itemgroup_natural_blocks";
            case "itemgroup_functional", "itemgroup.functional" -> "itemgroup_functional_blocks";
            case "itemgroup_redstone", "itemgroup.redstone" -> "itemgroup_redstone_blocks";
            case "itemgroup_tools", "itemgroup.tools", "itemgroup_toolsandutilities", "itemgroup.toolsandutilities" -> "itemgroup_tools_and_utilities";
            case "itemgroup_foodanddrink", "itemgroup.foodanddrink" -> "itemgroup_food_and_drinks";
            case "itemgroup_spawneggs", "itemgroup.spawneggs" -> "itemgroup_spawn_eggs";
            case "itemgroup.kubejs.tab", "itemgroup_kubejs_tab", "kubejs.tab", "kubejs_tab" -> "itemgroup_kubejs_tab";
            default -> normalized;
        };
        return normalized.replace(".", "_");
    }

    public void clearTabs() {
        wasReloaded = true;
        loadedTabs = 0;
        skippedTabs = 0;
        failedTabs = 0;

        customTabs.clear();
        hiddenItems.clear();
        disabledTabs.clear();
        searchableTabs.clear();
        searchDisabledTabs.clear();
        tabItems.clear();
        tabRegistryIds.clear();
        tabOrder.clear();
        currentTabs.clear();
        replacedTabs.clear();
    }

    public boolean consumeWasReloaded() {
        boolean reloaded = wasReloaded;
        wasReloaded = false;
        return reloaded;
    }

    private static List<Map.Entry<ResourceLocation, Resource>> sortedEntries(Map<ResourceLocation, Resource> entries) {
        return entries.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
    }

    public TabReloadResult createReloadResult(int discoveredTabs, int disabledFiles, int orderedFiles) {
        return new TabReloadResult(discoveredTabs, loadedTabs, skippedTabs, failedTabs, disabledFiles, orderedFiles, displayedTabs().size());
    }

    public void refreshOpenScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof CreativeModeInventoryScreen screen)
            screen.resize(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    }

    public List<CreativeModeTab> sortedTabs() {
        return this.currentTabs;
    }

    public List<CreativeModeTab> displayedTabs() {
        return this.currentTabs.stream().filter(tab -> {
            if (tab == operatorTab && !Minecraft.getInstance().options.operatorItemsTab().get()) {
                return false;
            }

            return tab.shouldDisplay();
        }).toList();
    }

    public Collection<Pair<CustomCreativeTabJsonHelper, List<ItemStack>>> replacementEntries() {
        return new LinkedHashSet<>(replacedTabs.values());
    }

    public void setVanillaTabs(List<CreativeModeTab> tabs) {
        this.vanillaTabs.clear();
        this.vanillaTabs.addAll(tabs);
    }

    public void setTabRegistryIds(Map<CreativeModeTab, ResourceLocation> registryIds) {
        this.tabRegistryIds.clear();
        this.tabRegistryIds.putAll(registryIds);
    }

    public void registerTabRegistryId(CreativeModeTab tab, ResourceLocation registryId) {
        if (tab != null && registryId != null)
            this.tabRegistryIds.put(tab, registryId);
    }

    public record PolicyFileResult(boolean policyFile, boolean malformed, boolean disabledFile, boolean orderedFile, boolean failed) {

        private static final PolicyFileResult NON_POLICY = new PolicyFileResult(false, false, false, false, false);
        private static final PolicyFileResult MALFORMED = new PolicyFileResult(false, true, false, false, true);
    }

}
