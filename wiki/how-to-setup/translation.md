---
description: null
---

# Translation

## Editing the plugin's messages

### Requirements:

* A ZIP explorer program \(such as [WinRAR](https://www.win-rar.com/download.html?&L=0) or [7-Zip](https://www.7-zip.org/download.html)\)
* A Text editor like [NotePad++](https://notepad-plus-plus.org/downloads/), [Visual Studio Code](https://code.visualstudio.com/) or [Sublime Text](https://www.sublimetext.com/).

### Step by step

1. Right click the plugin jar, click on "Open as" and choose the ZIP program;
2. Copy the messages file corresponding to your language to the plugin's folder;
   1. If there isn't one for your language, copy "messages.properties" instead and rename it with your language code appended. For example, if your language is Russian: `messages_ru_RU.properties`
3. Edit the messages and save;
4. Change `language` in config.yml to your language code;
5. Reload the plugin using `/union admin reload`.

### A little trick

If you like the current translation, but want to change only a few lines: 1. Copy the file to the plugin's folder; 2. Delete all messages except the ones you would like to edit; 3. Edit them and save.

> **Note**: Please note that your custom `messages.properties` does not automatically update when new messages are added.

## Sharing translations

Unions-OG maintains its own message bundles, which have diverged from upstream
SimpleClans: this fork adds keys of its own \(proposals and voting, for
example\) and renames the `clan.*` keys to `union.*`. Upstream translations
therefore no longer drop in unchanged.

If you have translated a bundle for this fork, open a pull request on the
[Unions-OG repository](https://github.com/true-og/Unions-OG).
