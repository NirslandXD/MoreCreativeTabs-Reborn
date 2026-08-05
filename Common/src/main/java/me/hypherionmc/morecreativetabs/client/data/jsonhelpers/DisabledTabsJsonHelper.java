package me.hypherionmc.morecreativetabs.client.data.jsonhelpers;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class DisabledTabsJsonHelper {
    @SerializedName("disabled_tabs")
    public List<String> disabledTabs = new ArrayList<>();
}
