---
title: How I learn programming
publishdate: 2021-03-07
---

I've been asked a couple of times how to learn a new programming language or to just generally learn how to program. I haven't had a good answer for this because I've thought my way of learning has been pretty messy. However, after talking to a few people about it, I've realized that there are many ways you can learn programming, and it depends a lot on the person learning programming what works the best for them. Therefore, I might as well share my notes just in case someone finds them useful.

<!-- more -->

## Learning programming from scratch

First, I'll share how I originally learned to program, what I found useful, and what I would do differently.

### Which language to start with?

I've been interested in computers and computing since I was a kid, so it naturally made sense for me to also learn how to program computers as well.

My first introduction to programming was through the [C][] programming language. C is considered a general-purpose language that can produce programs for anything as tiny as the [microcontrollers][] I used in the electronics school courses. It powers a wild variety of software such as operating systems, databases, and desktop applications. It's designed with portability in mind, and it's considered one of the most ubiquitous programming languages out there. Because of its versatility and because it was taught in school, I chose to start with C.

I almost quit programming completely after starting with C. Since C is just a couple of steps from programming machine code, it's not very beginner-friendly. It makes you work hard to get something done with it. Making anything useful with it felt so far out of my reach, that I questioned why anyone would want to program at all. Since this was my first impression of programming, I thought all programming languages would be equally hard to work with. I was ready to focus on other areas in computer science, but fortunately, I later learned other easier languages through my university studies (e.g. Python and Java), which kept me going.

What I learned from the experience is that you want to start with a language that has the following qualities.

1. It's generally considered easy to learn. If something is considered a language for beginners, then it's most likely going to have other beginners learning it, which means that there's bound to be resources on the Internet that you can utilize to your advantage.
2. It's easy to produce something tangible even as a beginner. I found it to be motivating when I was able to see my work gradually build up to something usable.

If I were to learning programming from scratch again, I'd probably go with [JavaScript][]. All you need is a web browser to start producing programs with web pages as your canvas. It's also used in all kinds of places these days:

* The vast majority of websites use JavaScript for handling user interactions.
* Many desktop apps such as [Discord][] and [Visual Studio Code][] are built with JavaScript.
* It can be used for building games for the web browser. [GitHub has compiled a list of game engines that support JavaScript](https://github.com/collections/javascript-game-engines).

There's plenty of resources out there to learn JavaScript, and many pick it up as their first language. I recommend checking out the [Eloquent JavaScript][] book to learn about JavaScript and programming in general.

[c]: https://en.wikipedia.org/wiki/C_%28programming_language%29
[microcontrollers]: https://en.wikipedia.org/wiki/Microcontroller
[javascript]: https://www.javascript.com/
[discord]: https://discordapp.com/
[visual studio code]: https://code.visualstudio.com/
[eloquent javascript]: https://eloquentjavascript.net/

### Exercise

It may sound kind of obvious, but I think the way I got productive in programming was through the act of programming itself. I've learned a lot from reading, listening, and watching videos, but I think the best teacher for me was to sit down in front of the computer and write programs. I think the key bit in learning is the interaction between myself and the code. I write some code, run it, and see how it works.

By coding more and more, I also develop a sort of "muscle memory" for programming. I start to gradually remember the syntax, function names, and just generally how things are supposed to work without having to resort to books and guides that often. This makes me more comfortable with programming, which makes me more confident in solving software problems with my code.

As a learning material, I found programming exercises to be quite effective. I was fortunate enough to go to a university where the majority of the material for the beginner programming courses was based on exercises instead of lectures. You could even skip the course exams if you just did enough course exercises. There was a lot of work to do in these courses, and I think they prepared me quite well to become both comfortable and productive with programming.

I haven't done many programming exercises outside of the university, so I'm not the best person to say what else is out there. However, I've heard that sites like [freeCodeCamp](https://www.freecodecamp.org/), [HackerRank](https://www.hackerrank.com/), and [Project Euler](https://projecteuler.net/) have plenty of great tasks to train with.

### Examples and copying

I think a major part of the learning experience in programming comes from seeing examples of code. In addition to providing a straightforward answer to you on solving a specific problem, snippets of code can help create a context for how to (and why) use certain programming techniques or features.

When I first started learning to program, a lot of the examples were scattered around individual programming tutorial websites and web forums (e.g. Ohjelmointiputka[^1]). Very soon after I started, [StackOverflow][] became the dominant source for answers to code-related questions. StackOverflow is so rich in examples that people often joke you could create rich applications based on the examples alone.

When looking at an example code snippet that solves the problem you're working on, it can be tempting to just copy and paste the code and move forward. I try to avoid direct copy-pasting, and instead, manually type the example myself. While doing so, I might change some of the code formatting (e.g. the whitespace between the syntax), rename the variables, or even re-arrange the code. I feel that this extra bit of effort helps me better understand what exactly the example code does compared to if I had just copied to code directly.

[stackoverflow]: https://stackoverflow.com/

### Hobby projects

After learning the basics of programming, the very next step for me was to start a hobby project to program something I could use myself. I feel that hobby projects have been the greatest motivation for me to keep on programming. While I do sometimes enjoy a good puzzle, I don't feel there's enough motivation to learn to program, if I were to just do it for solving them.

The hobby projects don't have to be large or advanced to be useful. Even small command-line scripts or browser extensions can get you started. As I learn more, I usually revisit my projects to add more features or improve what is there already. For example, I created a browser extension called [YAHE][] to port a browser feature I wanted to use in Chrome. While it's functionally not very different from the original version I made, I've edited its structure a lot over the years to be easier to maintain as I've learned new techniques and practices.

I do have quite a few stale projects. Many of them are pretty basic, janky, outdated, and poorly documented. I think it's a good idea to not let that bother you. After all, they're just personal, hobby projects.

[yahe]: https://github.com/Lepovirta/yahe#readme

## Learning a programming language

Now that I'm comfortable with programming, here's what I typically do when I want to learn a new programming language.

### Finding a modern guide

Usually, the first thing I do to learn a new programming language is to find a modern guide (or guides) for it and skim through it. I use the guide as a way to quickly get familiar with what the language is like. The guide(s) should cover these things:

* The core concepts of how the programming language works. For example, what's the programming paradigm like, what's the syntax like, and how the programming constructs work.
* The tools I should use to start producing runnable programs. For example, compilers and interpreters.
* The bad parts of the language I should generally avoid. For example, language features or patterns that didn't pan out as well as was originally planned.

For example, the [Modern Perl][] book from 2016 covers those points pretty well today. It teaches you how to get started with [Perl][], which was created back in 1987.

The way I usually search for modern guides is by using the search keywords `modern <programming language>` (e.g. "modern java") in Google. It's not a foolproof method but I usually find what I need that way. If the language I'm looking to learn is new, i.e. created in the last 10 years or so, its latest official guide is most likely modern enough to get started with.

[modern perl]: http://modernperlbooks.com/books/modern_perl_2016/index.html
[perl]:https://www.perl.org/

### Finding comparisons to other familiar languages

Many programming languages have a lot of things in common with each other. Languages are often inspired by other languages so many of them copy each others' execution model, structure, and even syntax. For example, a lot of languages are very similar to the C programming language I mentioned earlier, which are often addressed as [C-family programming languages](https://en.wikipedia.org/wiki/List_of_C-family_programming_languages).

Because programming languages have a lot in common and I happen to know a couple of them already, I've found it extremely helpful to compare the language I want to learn to the ones I already know. By mapping the concepts from one language to another, I've found it a great way to cut down the amount of learning I have to do.

To compare the languages, I typically lookup resources from the Internet on how other people compare them together. For example, [Rosetta Code](http://rosettacode.org/wiki/Rosetta_Code) provides example solutions for various programming tasks in all kinds of languages. For a more nuanced comparison, I tend to look for resources that compare language features and how the programs differ at runtime.

### Auto-formatters and linters

Even though programming languages have a lot of flexibility when it comes to code style and how different language constructs are used, there's usually a set of preferred practices that have evolved around the language. These typically come from the programming community and industry after years of experience in using the languages.

A lot of times the practices are encoded into tools that can analyze your code and offer suggestions on how to make your code better. Some of the tools can also automatically change your code to align with the preferred practices. Here are a few examples:

* Auto-formatters: Tools for automatically rearranging the code formatting to follow a consistent style without changing what the code does. For example, [Go][] has [go fmt][] and [Python][] has [black][].
* Linters: Tools for finding and pointing out potentially problematic code, and suggesting how it could be made better. Some of the tools can also automatically fix the problematic code. For example, JavaScript has [ESLint][] and [Shell scripts][] have [ShellCheck][].

These tools are typically used for improving code quality in software projects, but I've found them to be handy in learning new languages, too. When I continuously scan my code and fix it using these tools from the start, I gradually begin to learn how to code according to the preferred practices thus providing better quality code without thinking about it. For example, I extensively used the ShellCheck tool I mentioned earlier to learn out of bad Shell scripting habits (which there are plenty).

The tools I mentioned are just a few examples. There's plenty of more out there for each language, so be sure to check out what's available for the language you're learning. You can usually find them using search keywords `<programming language> lint` (e.g. "java lint") and `<programming language> auto format` (e.g. "java auto format") in Google. Note that not all tools are considered standard in the programming language community, and there may be multiple alternatives for the same purpose.

[go]: https://golang.org/
[go fmt]: https://blog.golang.org/gofmt
[python]: https://www.python.org/
[black]: https://pypi.org/project/black/
[ESLint]: https://eslint.org/
[shell scripts]: https://en.wikipedia.org/wiki/Shell_script
[ShellCheck]: https://www.shellcheck.net/

### Just code

Finally, after I have my tools set up and I've gone through a few examples, I usually just jump into coding something I might find useful. As mentioned earlier, this usually means starting a hobby project.

Of course, I'm not going to be very productive with the language from the start, and I'll be spending most of the development time browsing documentation and code examples rather than typing code. It can feel quite tedious for a long while. However, I'll gradually start to spend more time in the code editor as I get more familiar with the language.

## Stuff I only do occasionally

There are a few ways to learn programming that are often recommended which I only do occasionally. Here's what they are and why I only do them from time to time. If you haven't tried these methods, I do recommend trying them at least once.

### Reading books

While I sometimes read books on software development, I rarely read books that teach how to program. If I do pick up a book on learning programming, it's usually to skim through the main points, to find an example, or to reference other work.

I think there are roughly three reasons why I read few books:

1. I prefer learning through experimentation.
2. I think a lot of the short guides and tutorials on the Internet are easier to navigate and follow than books.
3. I lack the attention span.

### Watch videos

There are hours and hours of video content on programming available for free on sites like YouTube. There's everything from byte size examples to multi-part tutorials and courses.

I rarely watch videos to learn to program because I prefer text-based content over video. This is because I can better control the pace I process text instead of video. For example, re-reading and referencing parts is easier in the text format than finding the right part of the video.

The few times I do watch videos on programming are when they demonstrate a complex topic visually with motion. For example, a video can be a great tool for demonstrating [how sprites and background scrolling work in an old game console](https://www.youtube.com/watch?v=zQE1K074v3s).

### Read other people's code

I've heard a lot of people say that one of the best ways to learn to be better at coding is to learn how other people write code. For example, some suggest browsing through the codebases of popular open-source applications such as [SQLite][] or [etcd][].

I have no doubts that it wouldn't be useful, but I haven't found the motivation to read through a lot of code beyond a few examples. I think much of it is because of the same three reasons why I don't read many books.

That said, there are a couple of contexts in which I do read some of the code produced by other people but only just enough to progress.

* Reviewing and contributing code: Many of the customer projects I work with use code reviews. When a person contributes a code change, another person has to review it before it's accepted. Reviewing and having others review code helps find better solutions and a nice, common code style.
* Debugging: When I'm using an external library or a framework, it's sometimes not enough to use it based on documentation. Occasionally, the libraries and frameworks work in unexpected ways and it's then useful to view the code to get a better understanding of what's going on.

[sqlite]: https://sqlite.org/
[etcd]: https://etcd.io/

## Conclusion

In this post, I shared my experiences on learning to program, how I approach learning a new programming language, and what are the things I do only occasionally when learning.

All of the suggestions are based on my personal experiences and how I like to learn things. I don't expect them to work for everyone, but I some find parts of them useful. My experiences are just a drop in the bucket in the world of programming, so be sure to find out what others do to learn and experiment with what works the best for you.

I wish you the best of luck in learning programming!

[^1]: [Ohjelmointiputka](https://www.ohjelmointiputka.net/) is a Finnish discussion board and a tutorial site for programmers that dates back to 2002.
