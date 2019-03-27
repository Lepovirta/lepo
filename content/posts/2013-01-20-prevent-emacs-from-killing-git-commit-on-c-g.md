---
title: Prevent Emacs from killing "git commit" on C-g
publishdate: 2013-01-20
tags: [emacs]
---

If you use Emacs and Git, you might have noticed that pressing control +
G (`C-g`) while typing a commit message kills the whole commit process.
Here's how you can work around it.

<!--more-->

`C-g` in Emacs is used for cancelling commands. It also sends a SIGINT
signal to Emacs. The signal is also passed to the parent process, git,
which causes git to end the whole process.

The workaround is to prevent the signal from getting to git. One such
way is to call Emacs within a shell script. Shells typically ignore
SIGINT signals. Here's an example of such script:

    #!/bin/sh -i
    /usr/bin/emacs -nw "$@"

Here shell script starts in an interactive mode, calls `/usr/bin/emacs`,
and passes all the script's parameters to Emacs. Save the script
somewhere in your `$PATH` (for example `$HOME/bin/emacs.sh`), and make
git use the script as its editor:

    $ git config --global core.editor emacs.sh

The same workaround works for Mercurial, too. Change the editor for hg:

    $ hg config --config ui.editor emacs.sh

If you want to use the script as an editor for all the command line
programs, just change the `EDITOR` and `VISUAL` environment variables in
your shell's initialization files:

    export EDITOR="emacs.sh"
    export VISUAL="$EDITOR"

Sources:

-   <http://thread.gmane.org/gmane.comp.version-control.git/158981>
-   <http://www.cons.org/cracauer/sigint.html>
