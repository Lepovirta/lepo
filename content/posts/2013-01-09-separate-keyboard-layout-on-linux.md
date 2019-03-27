---
title: Separate keyboard layout on Linux
publishdate: 2013-01-09
tags: [linux]
---

I've been doing some pair programming where there are two sets of
keyboards and mice connected to one computer. I prefer using a different
keyboard layout than what my coworkers use. Fortunately, there is way to
set a separate layout for each keyboard on Linux.

<!--more-->

For this setup you need `xinput` and `setxkbmap` commands.
First, list all your input devices using command `xinput -list`.
You should get a list that looks something like this:

    ⎡ Virtual core pointer                          id=2    [master pointer  (3)]
    ⎜   ↳ Virtual core XTEST pointer                id=4    [slave  pointer  (2)]
    ⎜   ↳ ETPS/2 Elantech Touchpad                  id=12   [slave  pointer  (2)]
    ⎜   ↳ Logitech USB Receiver                     id=14   [slave  pointer  (2)]
    ⎜   ↳ Logitech USB Receiver                     id=15   [slave  pointer  (2)]
    ⎣ Virtual core keyboard                         id=3    [master keyboard (2)]
        ↳ Virtual core XTEST keyboard               id=5    [slave  keyboard (3)]
        ↳ Power Button                              id=6    [slave  keyboard (3)]
        ↳ Video Bus                                 id=7    [slave  keyboard (3)]
        ↳ Sleep Button                              id=8    [slave  keyboard (3)]
        ↳ USB2.0 0.3M UVC WebCam                    id=9    [slave  keyboard (3)]
        ↳ Asus Laptop extra buttons                 id=10   [slave  keyboard (3)]
        ↳ AT Translated Set 2 keyboard              id=11   [slave  keyboard (3)]
        ↳ ACPI Virtual Keyboard Device              id=13   [slave  keyboard (3)]

Next, look for the keyboard that you wish to change the layout for. In
my example, I only have one keyboard device listed: AT Translated Set 2
keyboard. The list also tells us the ID the device is assigned to, which
is 11 in my case. You need to pass the ID to the command `setxkbmap`
using option `-device`. For example, if I wanted to switch my keyboard
layout to a finnish layout, the command would be:

    setxkbmap -device 11 us
