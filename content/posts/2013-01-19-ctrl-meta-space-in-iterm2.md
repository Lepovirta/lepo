---
title: Ctrl-Meta-Space in iTerm2
publishdate: 2013-01-19
tags: [emacs, macos]
---

In Emacs, there’s a handy command called `mark-sexp`. It marks the rest of the current s-expression from the cursor’s position. It’s mapped to `C-M-SPC` (ctrl + meta + space) by default, which is compatible with terminal applications.

However, if you have option key mapped to `+Esc` in iTerm2, pressing ctrl + option + space is interpreted as meta + space instead. To fix this, you need to add an additional keybinding to your session’s key bindings.

<!--more-->

1.  In iTerm2, open: Preferences \> Profiles \> some session you wish to edit \> Keys.
2.  Add a new key binding by pressing the “+” button.
3.  Select the “Keyboard Shortcut” field, and press ctrl + option + space.
4.  Select “Send Hex Code” in the “Action” list.
5.  Type “0x1b 0x00” to the field below the list.
6.  Click OK and close the preferences window.

`C-M-SPC` now works in all the new terminal windows that use the edited session. You can also make the same changes for running terminals by editing the running session.
