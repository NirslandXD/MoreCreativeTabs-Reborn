package me.hypherionmc.morecreativetabs.client.tabs;

import lombok.Getter;

@Getter
public class TabReloadResult {

    private final int discoveredTabs;
    private final int loadedTabs;
    private final int skippedTabs;
    private final int failedTabs;
    private final int disabledFiles;
    private final int orderedFiles;
    private final int displayedTabs;

    public TabReloadResult(int discoveredTabs, int loadedTabs, int skippedTabs, int failedTabs, int disabledFiles, int orderedFiles, int displayedTabs) {
        this.discoveredTabs = discoveredTabs;
        this.loadedTabs = loadedTabs;
        this.skippedTabs = skippedTabs;
        this.failedTabs = failedTabs;
        this.disabledFiles = disabledFiles;
        this.orderedFiles = orderedFiles;
        this.displayedTabs = displayedTabs;
    }

    public String summary() {
        return "Loaded %d/%d tab definitions (%d skipped, %d failed), %d disabled files, %d ordered files, %d displayed tabs"
                .formatted(loadedTabs, discoveredTabs, skippedTabs, failedTabs, disabledFiles, orderedFiles, displayedTabs);
    }
}
