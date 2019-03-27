---
title: Asus UL30A brightness keys on Linux
publishdate: 2012-10-14
tags: [linux]
---

Asus UL30A is a lightweight, 13 inch, budget laptop with a pretty long
battery life. It also has a pretty good compatibility with Linux: pretty
much everything works out-of-the-box. The only things that need tweaking
after a fresh Linux installation are the brightness controls.

<!--more-->

To get the keys working, you need to make sure that Linux boots the
kernel with the these ACPI settings:

    acpi_osi= acpi_vendor=vendor

If you have Debian, Ubuntu, Linux Mint or any other Debian based
distribution installed, all you need to do is follow these steps:

-   Edit file `/etc/default/grub`
-   Make sure that `GRUB_CMDLINE_LINUX` variable includes the mentioned
    settings. For example:

        GRUB_CMDLINE_LINUX="acpi_osi= acpi_vendor=vendor"

-   Run `update-grub`:

        # sudo update-grub
