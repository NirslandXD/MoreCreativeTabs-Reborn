package me.hypherionmc.morecreativetabs.client.data;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CustomCreativeTabJsonHelper {

    @SerializedName("tab_enabled")
    private Boolean tabEnabled;

    @SerializedName("tab_name")
    private String tabName;

    @SerializedName("tab_stack")
    private TabIcon tabIcon;

<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 79b2a8a (initial commit)
    @SerializedName("tab_background")
    private String tabBackground;

    @SerializedName("search_bar")
    private boolean searchBar;

    @SerializedName("search_bar_width")
    private int searchBarWidth = 89;
<<<<<<< HEAD
=======
=======
    @SerializedName("search_bar")
    private Boolean searchBar;
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)

    private boolean replace;

    @SerializedName("tab_replace")
    private String tabReplace;

    @Setter
    private boolean keepExisting;

    private final Set<String> existingExceptions = new HashSet<>();

    @SerializedName("tab_items")
    private ArrayList<TabItem> tabItems;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class TabItem {
        private String name;
<<<<<<< HEAD
=======
<<<<<<< HEAD
=======
        private String tag;
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)

        private JsonElement except;

        @SerializedName("hide_old_tab")
        private boolean hideOldTab;

        private String nbt;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class TabIcon {
        private String name;
        private String nbt;
    }
}
