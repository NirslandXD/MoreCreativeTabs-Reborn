package me.hypherionmc.morecreativetabs.client.data.jsonhelpers;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Configuration shared with newer More Creative Tabs data packs. */
public class CustomCreativeTab {

    @SerializedName("tab_enabled")
    public Boolean tabEnabled;
    @SerializedName("tab_name")
    public String tabName;
    @Deprecated(forRemoval = true)
    @SerializedName("tab_icon")
    public String tabIcon;
    @SerializedName("tab_stack")
    public TabIcon tabStack;
    @SerializedName("tab_background")
    public String tabBackground;
    @SerializedName("search_bar")
    public Boolean searchBar;
    public boolean replace = false;
    @SerializedName("tab_replace")
    public String tabReplace;
    public boolean keepExisting = false;
    public final Set<String> existingExceptions = new HashSet<>();
    @SerializedName("tab_items")
    public List<TabItem> tabItems;

    public static class TabItem {
        public String name;
        public String tag;
        public JsonElement except;
        @SerializedName("hide_old_tab")
        public boolean hideOldTab;
        public String nbt;
    }

    public static class TabIcon {
        public String name;
        public String nbt;
    }
}
