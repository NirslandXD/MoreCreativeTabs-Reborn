package me.hypherionmc.morecreativetabs.client.data;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DisabledTabsJsonHelper {

    @SerializedName("disabled_tabs")
    private ArrayList<String> disabledTabs = new ArrayList<>();

}
