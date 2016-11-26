# lepo

[![Build Status](https://travis-ci.org/Lepovirta/lepo.svg?branch=master)](https://travis-ci.org/Lepovirta/lepo)
[![codecov](https://codecov.io/gh/Lepovirta/lepo/branch/master/graph/badge.svg)](https://codecov.io/gh/Lepovirta/lepo)
[![Dependencies Status](https://jarkeeper.com/Lepovirta/lepo/status.svg)](https://jarkeeper.com/Lepovirta/lepo)

Static website generator for [Lepo.IO](https://lepo.io/).

## Requirements

You need to have JDK installed.
Either Oracle's JDK or OpenJDK is fine.
After installing the JDK, you need to install [Leiningen](http://leiningen.org/).

## Running a live version of the site

Use Leiningen to run a live version of the site.
Go to project root directory and run

    $ lein ring server

This will open the site in your browser automatically.
The first run might take a while to get started.

When you edit the source code or the resources while running the live site,
refreshing the page will reload the latest changes automatically.

## Generating a static site

Use Leiningen to generate a static site.
Go to project root directory and run

    $ lein build-site

This will place the site under directory `target/website/` in the project root directory.
You can also specify a target directory where to place the contents.
WARNING: the `build-site` command deletes EVERYTHING from the target directory before generating a new one.

    $ lein build-site path/to/some/dir

## Editing the site

All of the site resources can be found from `resources` directory.
Here are some of the things it contains:

* **assets/files**: directory of arbitrary files that will be placed to `/files` path.
* **assets/img**: directory of arbitrary images that will be placed to `/img` path
* **assets/styles**: directory containing all of the SASS and CSS files.
  Build output will be placed under `/styles`.
* **assets/favicon.png**: the favicon for the site
* **pages**: directory of all the pages in the site.
  Each page contains the body text in HTML format and some configuration data in EDN format.
  The paths under this directory will be mapped to same path in the output.
* **pages/posts**: directory of all the blog posts.
* **pages/team**: directory of team member pages.
* **templates**: directory of all the templates used in combination with the pages.
  Templates use the [Selmer](https://github.com/yogthos/Selmer) format.
* **config.edn**: the base configuration for the site in EDN format.

### Editing the layouts

Edit one of the templates in the `resources/templates/` directory.
Here's what each template is used for:

* **base**: the base template other templates inherit from
* **archives**: posts archive template
* **tag**: tag posts listing template
* **author**: author page template
* **frontpage**: template for `resources/pages/index.html`
* **post**: blog post template
* **normal**: template for all of the other pages

### Adding a post

Create a new file under directory `resources/pages/posts/`.
The file must begin with a date in format YYYY-MM-DD followed by the post name.
E.g. `resources/pages/posts/2016-01-18-my-cool-post.html`

The file must begin with a EDN map that contains at least a title and a valid author ID.
E.g.

```
{:title "My Cool Post"
 :author-id :someone}
```

After the map, there should be a `---` marker with at least one empty line on both sides.
After the marker, the body of the post can be written in HTML format.

## License

### Software

Copyright © 2016 Jaakko Pallari

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

### Resources

All the resources are licensed under
[Creative Commons Attribution-ShareAlike 4.0](http://creativecommons.org/licenses/by-sa/4.0/).
