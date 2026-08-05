# Changelog

All notable changes to MoreCreativeTabs: Reborn are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project follows semantic versioning.

## [Unreleased]

### Changed

- Polished repository metadata, documentation, issue templates, and continuous integration.
- Consolidated project versioning into the `mod_version` Gradle property.

## [1.0.0] - 2026-06-15

### Added

- Minecraft 1.20-1.20.1 support for Fabric and Forge.
- Resource-pack-driven custom, replacement, ordered, searchable, and disabled creative tabs.
- Per-tab item removal rules.
- `/mct reloadTabs` and `/mct showTabNames` client commands.
- Compatibility aliases for legacy creative-tab filenames and selectors.

### Fixed

- KubeJS tab identification and disabling.
- Replacement matching for Natural Blocks, Redstone Blocks, and Food & Drinks.
- Stale selected tabs after disabling or removing a tab.
- Item filtering for vanilla, modded, generated, and replacement tabs.
- Searchable-tab backgrounds and inventory-key handling.

