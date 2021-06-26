---
title: Finding files in Vim
publishdate: 2015-10-11
tags: [vim]
---

One of Vim’s coolest features is how easily it can leverage command-line tools to extend it’s feature set. In this article, I’ll present a walkthrough for creating a UI for the Unix `find` command using Vimscript. The script provides a way to find files from Vim, list the files in a separate Vim window, and allow quick access to the found files.

<!--more-->

## The core script

In order to show the file search results, we need to create a pair of a buffer and a window for them. Instead of creating a new pair for each search result, we’ll instead try to reuse the existing pair when possible. This way the old search results wont keep cluttering the UI.

The `InitFindWindow` initializes the window and buffer for search results. It iterates through the available windows to find an existing buffer showing search results (lines 5-11). The identification is done by jumping to each window one by one (line 6), and querying the `filetype` setting for value `findbuf` (line 7). If the function finds the correct buffer, the function stops in that window (lines 8-9). If the buffer cannot be found, the function jumps back to the first window (line 14), and creates a new window and a buffer below it (line 15). It also sets the file type to `findbuf` for the new buffer (line 16). After calling the function, the search window should be selected.

```vim
function! s:InitFindWindow()
    let startwin = winnr()
    let findwindownr = 0

    for winnumber in range(1, winnr('$'))
        execute winnumber . 'wincmd w'
        if &filetype == 'findbuf'
            let findwindownr = winnumber
            break
        endif
    endfor

    if !findwindownr
        execute startwin . 'wincmd w'
        botright new
        setfiletype findbuf
    endif
endfunction
```

After the search window has been initialized, it needs to be populated with the latest search results. The function `ReplaceContentsWithCommand` first clears the buffer (line 2), and then appends the results from the given shell command (parameter `cmd`) to the buffer (line 3). The command results are placed to the buffer using the combination of Vim’s `read` and `!` commands. The `!` command executes given shell command, while the `read` command appends the output of that command to the buffer. Finally, the function removes all of the empty lines (line 4), and returns the cursor to the first line of the buffer (line 5).

```vim
function! s:ReplaceContentsWithCommand(cmd)
    silent %d
    execute 'silent $read !' . a:cmd
    g/^$/d
    1
endfunction
```

By default, the search result window’s height will be half of the full height of the Vim window. Let’s try adjusting the window height a bit, shall we. In function `MinimalWindowSize`, we’ll use the smallest of these three values as the window height: the number of lines in the search results, half of the full height, or the height value supplied as the parameter. Everyone will obviously have their own preference on the search window height, so adjust accordingly.

```vim
function! s:MinimalWindowSize(maxsize)
    let buflines = line('$')
    let wsize = min([a:maxsize, &lines / 2, l:buflines])
    execute 'resize ' . l:wsize
endfunction
```

To call the file search, we’ll glue these three functions together to a single function, and set up a custom command for it. The function `Find` takes a search pattern as a parameter, and joins it with the command found from `g:findcmd`. By default, the find command shown on line 2 will be used, if no other command is explicitly set. The variable `g:findwinsize` can also be used for adjusting the maximum size of the search window size.

The function initializes the window, updates the contents of the window using the find command, and adjusts the window size if necessary. In order to ensure that the content can be updated, the function enables buffer modification before the update. The function also sets the status line for the window to show the search pattern used.

To expose the function to the user, we set up a command that calls the function with the given parameter. Thus calling the command `:Find *.html` will search all of the HTML files in the current directory.

```vim
function! s:Find(pattern)
    let defaultcmd = 'find . \( -type f -o -type l \) -iname'
    let cmd = exists('g:findcmd') ? g:findcmd : l:defaultcmd
    let fullcmd = l:cmd . " '" . a:pattern . "'"
    let maxwinsize = exists('g:findwinsize') ? g:findwinsize : 10

    call s:InitFindWindow()
    setl modifiable
    call s:ReplaceContentsWithCommand(l:fullcmd)
    setl nomodifiable
    call s:MinimalWindowSize(l:maxwinsize)
    let &l:statusline='Find: ' . a:pattern . ' %= %l/%L'
endfunction

command! -nargs=1 Find call s:Find(<q-args>)
```

## Extending the script

The search window can be further extended by adding features to the `findbuf` file type. This can be done by setting up commands that are automatically run when the buffer is created. For example, adding the line `autocmd FileType findbuf setl number` to your `vimrc` would enable line numbering for the search window.

Since the search buffer is an ephemeral view instead of a real file, so all of its file related features can be disabled:

  - The setting `buftype=nofile` ensures that the buffer is not associated with any file.
  - The settings `bufhidden=wipe` and `nobuflisted` ensure that the buffer is not available in the buffer listing.
  - The setting `noswapfile` ensures that the contents of the buffer is kept in memory instead of swapping to the disk.

```vim
au FileType findbuf setl buftype=nofile bufhidden=wipe nobuflisted noswapfile
```

Additionally, the UI can be further tweaked to appear more "menu-like" by turning off the line wrapping and turning on the line cursor. When the line wrapping is disabled, only one file is shown per line. The line cursor highlights the whole line under the cursor.

```vim
au FileType findbuf setl nowrap cursorline
```

One useful feature is to be able to quickly open files from the search window. Vim already supports this through key mappings `gf` and `Ctrl-W f`. The first one opens the file from under the cursor in the same window while the second one opens the file in a new window. However, it’s missing the ability to open a file in a another existing window. The following listing shows how a file under the cursor can be opened in the previous window. The functionality is mapped to the enter key in the keyboard.

```vim
function! CursorFileInPreviousWindow()
    let cursorfile = expand('<cfile>')
    wincmd p
    execute 'e ' . l:cursorfile
endfunction

au FileType findbuf
    \ nnoremap <buffer> <Return> :call CursorFileInPreviousWindow()<CR>
```

## Alternatives

If the file search solution shown in this article wasn’t for you, there are a few alternatives to choose from.

Vim supports searching files through its built-in command `:e`. When you add wildcards into the parameter and complete the command with tab, the wildcards will be expanded, and Vim will let you choose a file from the selection. For example, typing `:e **/*.html` and pressing tab will list all of the HTML files from the current directory. However, once you select a file, the list of files disappears immediately.

There are also Vim plugins that support finding and opening files. The plugins listed below are few of the many plugins that support these and many other features. They also support a more dynamic kind of search where the search results are updated on each key press.

  - [CtrlP](https://kien.github.io/ctrlp.vim/)
  - [Vim-CtrlSpace](https://github.com/szw/vim-ctrlspace)
  - [Unite](https://github.com/Shougo/unite.vim)
