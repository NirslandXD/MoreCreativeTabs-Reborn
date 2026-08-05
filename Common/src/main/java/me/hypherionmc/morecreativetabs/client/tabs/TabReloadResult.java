package me.hypherionmc.morecreativetabs.client.tabs;

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

    public int getDiscoveredTabs() { return discoveredTabs; }
    public int getLoadedTabs() { return loadedTabs; }
    public int getSkippedTabs() { return skippedTabs; }
    public int getFailedTabs() { return failedTabs; }
    public int getDisabledFiles() { return disabledFiles; }
    public int getOrderedFiles() { return orderedFiles; }
    public int getDisplayedTabs() { return displayedTabs; }

    public String summary() {
        return String.format("Loaded %d/%d tab definitions (%d skipped, %d failed), %d disabled files, %d ordered files, %d displayed tabs",
                loadedTabs, discoveredTabs, skippedTabs, failedTabs, disabledFiles, orderedFiles, displayedTabs);
    }
}
