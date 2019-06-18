---
title: Commenting lines in Vim
publishdate: 2015-05-20
tags: [vim]
---

Emacs has a nice command called `comment-region`, which can be used for
commenting lines of code. The command adds comment symbols to the
beginning of each line in the selected region. Unfortunately, there is no
equivalent command in Vim, but a similar command can be easily created
with just a few lines of Vimscript.

<!--more-->

```
fu! CommentLines() range
    let commentsymbol = exists('b:commentsymbol') ? b:commentsymbol : '//'
    let beginsWithComment = getline(a:firstline) =~ ('\M^' . l:commentsymbol)
    for linenum in range(a:firstline, a:lastline)
        let line = getline(linenum)
        let replacement = l:beginsWithComment
            \ ? substitute(line, '\M^' . l:commentsymbol . '\s\?', '', '')
            \ : l:commentsymbol . ' ' . line
        if exists('b:commentsymbolend')
            let l:replacement = l:beginsWithComment
                \ ? substitute(l:replacement, '\M\s\?' . b:commentsymbolend . '$', '', '')
                \ : l:replacement . ' ' . b:commentsymbolend
        endif
        call setline(linenum, replacement)
    endfor
    call cursor(a:lastline + 1, 1)
endfunction

fu! CommentSymbol(start, ...)
    let b:commentsymbol = a:start
    if a:0 >= 1
        let b:commentsymbolend = a:1
    elseif exists('b:commentsymbolend')
        unlet b:commentsymbolend
    endif
endfunction
command! -nargs=0 -range Comment <line1>,<line2>call CommentLines()
command! -nargs=+ CommentSymbol call CommentSymbol(<f-args>)
```

In the code listing above, the function `CommentLines` is used for
commenting the selected range. The command `Comment` is a short hand for
the function, which can be invoked like any other range command. For
example, invoking `:5,10Comment` will (un)comment the lines between 5
and 10. Invoking the command without a range will (un)comment the line
under the cursor. Here's how the command can be bound to a key
combination such as `<Leader>c`:

    nnoremap <silent> <Leader>c :Comment<cr>
    vnoremap <silent> <Leader>c :Comment<cr>

The comment symbols can be configured using two buffer local variables.
The function uses variable `b:commentsymbol` as the comment symbol. By
default, `//` is used as the comment symbol, if the variable is not set.
An optional variable `b:commentsymbolend` can also be specified, which
will be appended to the commented lines. The comment symbols can be set
directly to the variables using `let`:

    " Python comments
    let b:commentsymbol = '#'

    " C style comments
    let b:commentsymbol = '/*'
    let b:commentsymbolend = '*/'

Alternatively, the command `CommentSymbol` can be used:

    " Python comments
    CommentSymbol #

    " C style comments
    CommentSymbol /* */

If the comment function shown above isn't your cup of tea, check out
these plugins instead:

- [commentary.vim](https://github.com/tpope/vim-commentary)
- [NERD Commenter](https://github.com/scrooloose/nerdcommenter)
- [tComment](http://www.vim.org/scripts/script.php?script_id=1173)
