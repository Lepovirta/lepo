---
title: HTML search and replace in Clojure
publishdate: 2017-03-07
tags: [clojure, html]
---

In Clojure, data structures are mostly built from a handful of [core data structures](https://clojure.org/reference/data_structures) such as lists, vectors, maps, and sets. This means that most data structures can leverage all of the generic data transformation and querying functions built for the core data structures instead of having to rebuild the same functionality for each data structure. This feature in combination with Clojure’s rich standard library makes Clojure very attractive for solving data munching problems from other domains.

In this article, I’m going to demonstrate these capabilities for solving HTML transformations using Clojure. First, I’m going to describe how HTML can be represented in Clojure. With this representation in mind, I’ll demonstrate how we can transform HTML documents in Clojure. Finally, I’ll tie the transformations together with the HTML parsing and formatting to produce a complete solution.

<!--more-->

## Clojure representation for HTML

HTML is a fairly consistent format. HTML documents are mostly composed from elements that have the following properties:

  - They have a name.
  - They have zero or more attributes.
  - They can contain zero or more tag or text elements.

Besides tags and text elements, HTML documents can also contain a DTD (Document Type Declaration) and comments. For the sake of simplicity, we can ignore the DTD and interpret comments as text elements.

With these properties in mind, we can represent HTML tags in many styles using Clojure’s core data structures. [Hiccup](https://github.com/weavejester/hiccup), a Clojure library for converting Clojure data structures to HTML, represents HTML tags as Clojure vectors with the following properties:

1.  The tag name is represented as a keyword in the head of the vector.
2.  The tag attributes are represented as a map in the second value of the vector.
3.  The rest of the vector elements represent the body of the HTML tag.

For example, a list in HTML...

``` html
<ul class="navbar">
  <li>Hello world!</li>
  <li><a href="about.html">About</a></li>
</ul>
```

...can be represented in Hiccup format (with the whitespace omitted) as follows:

``` clojure
[:ul {:class "navbar"}
 [:li {} "Hello world!"]
 [:li {}
  [:a {:href "about.html"} "About"]]]
```

## Mission: adding a root path to absolute paths

Now that we have a language for describing HTML in Clojure data structures, we can build HTML transformations in Clojure.

One example of such transformations is adding a root path to each absolute path appearing in URI attributes. For example, links such as `<a href="/index.html">...</a>` would be transformed to `<a href="/root/index.html">...</a>`. This transformation is common for moving complete websites under a new directory structure.

First, we’ll need to specify what an absolute path is. All absolute paths start with a single `/` character. Note that paths with two `/` characters refer to [protocol-relative URL](https://www.paulirish.com/2010/the-protocol-relative-url/).

``` clojure
(defn absolute-path?
  [^String uri]
  (and (.startsWith uri "/")
       (not (.startsWith uri "//"))))
```

With this in mind, we can write a function that skips all the URIs that are not absolute paths, and transform the ones that are. Adding a root path itself is as easy as string concatenation:

``` clojure
(defn add-root-to-uri
  [root-path uri]
  (if (and (not (empty? root-path))
           (absolute-path? uri))
    (str (string/replace root-path #"^/*" "/") uri)
    uri))
```

Next, we’ll need to specify the attributes that can contain absolute paths. There are many [attributes that accept an URI](https://www.w3.org/TR/REC-html40/index/attributes.html), but for the simplicity of this exercise, we’ll just focus on the two obvious ones: `href` in `a` tags, and `src` in `img` tags. Let’s define a set for the attribute keys.

``` clojure
(def attr-names-with-uris #{:src :href})
```

Now we can apply the transformation to all HTML attributes (key-value pairs). If the attribute key is defined in the set above, we apply the URI transformation to the attribute value. Otherwise, we’ll keep the attribute value the same.

``` clojure
(defn add-root-to-uri-attr
  [root-path [key value]]
  [key (if (attr-names-with-uris key)
         (add-root-to-uri root-path value)
         value)])
```

## Transforming the HTML with recursion

HTML documents can be thought of as trees where HTML elements are either nodes or leaves in the tree. Therefore, in order to apply a transformation, we can use common tree traversing strategies for making the modification across the whole tree. What’s an easy way to traverse a tree? Recursively, of course\!

``` clojure
(defn add-root-to-html-recur
  [root-path html]
  (if (vector? html)                   ; Is it an HTML tag?
    (let [[tag attrs & body] html      ; Unpack the tag
          attrs (->> attrs             ; Update the attributes
                     (map (partial add-root-to-uri-attr root-path))
                     (into {}))
          body (->> body               ; Update the body (recursion)
                    (map (partial add-root-to-html-recur root-path)))]
      (vec (concat [tag attrs] body))) ; Rebuild the tag with the updates intact
    html))
```

In the code listing above, we’ve defined a function for updating the root path to all URI attributes recursively. The function unpacks the given the HTML tag (a vector), updates its attributes with the previously defined attribute transformer, applies the same function the child nodes, and finally rebuilds the node with the updates from the two previous steps. If the given HTML node is not a tag (i.e. it’s text instead), no transformation is necessary. This, in combination with the empty node body, acts as the termination mechanism for the recursion.

## Getting rid of the recursion

Recursive traversal is fairly easy to understand, but it has many of the usual problems of recursive functions:

1.  The function must be called explicitly in order to ensure the whole tree is processed.
2.  The function must have proper recursion termination conditions in place in order to prevent infinite recursion.
3.  Although HTML documents are often shallow, and thus the use of call stack for tracking the traversal progress is rarely a problem, it is something to consider in case of an odd deep document.

Since it’s just a Clojure core data structure we’re traversing, surely there’s a generic way to traverse the tree? And indeed there is a module for that in the Clojure standard library: [clojure.walk](https://clojuredocs.org/clojure.walk). The walk module provides functions for traversing arbitrary Clojure data structures. The functions in the module take a data structure, they call a given function to each substructure, and build a new data structure with the earlier substructure replaced with the result of the given function.

One of the traversal functions in the module is [postwalk](https://clojuredocs.org/clojure.walk/postwalk), which performs a depth-first, post-order traversal on the given data structure. We can use `println` to give us an idea on how it works.

``` clojure
(require 'clojure.walk)

(clojure.walk/postwalk
 (fn [x] (println x) x)
 [:p {}
  [:a {:href "about.html" :title "About"} "See about"]])

;; Output
:p
{}
:a
:href
about.html
[:href about.html]
:title
About
[:title About]
{:href about.html, :title About}
See about
[:a {:href about.html, :title About} See about]
[:p {} [:a {:href about.html, :title About} See about]]
```

As we can see from the output, `postwalk` hits the `href` key-value among other items in the tree. Since we’re now using a more generic traversal method, the transformation passed to `postwalk` is applied to all substructures rather than just the attributes map. Therefore, we need to use a filter to select which structures to apply our transformation on. We can easily achieve this using a predicate that checks if the structure is an attribute, i.e. a key-value pair. Each key-value pair is a vector of two elements, and in this case the first element (the key) is always a keyword.

``` clojure
(defn key-value?
  [x]
  (and (vector? x)
       (= (count x) 2)
       (keyword? (first x))))
```

We can reuse the URI attribute transformer we implemented earlier, and combine it with `postwalk` and the predicate from above. As before, we apply the transformation for all key-value pairs, and simply keep the other structures as they are.

``` clojure
(defn add-root-to-html
  [root-path html]
  (let [edit (fn [x]
               (if (key-value? x) ; only transform attributes
                 (add-root-to-uri-attr root-path x)
                 x))]
    (clojure.walk/postwalk edit html)))
```

## Tying the pieces together

Now that we have a way to perform URI attribute transformation for an HTML document, let’s wrap up the work by handling the conversion between HTML text and the Hiccup format.

We can parse the HTML text into Hiccup using the [Hickory](https://github.com/davidsantiago/hickory) library. The parser will yield a sequence of Hiccup fragments it find from the given text.

``` clojure
(require 'hickory.core)

(defn text->hiccup
  [text]
  (-> text
      hickory.core/parse         ; parse HTML text
      hickory.core/as-hiccup))   ; convert it to Hiccup
```

Once we’ve done our transformations, we can use the [Hiccup](https://github.com/weavejester/hiccup) library to convert the data structures back to text. Note that Hiccup’s `html` is a macro rather than a method, so we’ll need to wrap it with a function in order to use it in function composition.

``` clojure
(require 'hiccup.core)

(defn hiccup->text
  [html]
  (hiccup.core/html html))
```

Finally, we’ll combine these functions with the functions created earlier:

``` clojure
(defn add-root-to-html-text
  [root-path html-str]
  (->> html-str                 ; start with HTML string
       text->hiccup             ; convert it to Hiccup
       (map (comp hiccup->text  ; process each fragment
                  (partial add-root-to-html root-path)))
       clojure.string/join))    ; join the fragments to a single string
```

And there we have it, a function that adds a root path to URIs in HTML text:

1.  The given HTML text is parsed into a sequence of Hiccup fragments.
2.  Root path is added to all the URI attributes in each Hiccup fragment.
3.  Each processed Hiccup fragment is converted back to HTML text.
4.  All the HTML text fragments are joined back into a single string.

We can, of course, further combine this with [file reading](https://clojuredocs.org/clojure.core/slurp) and [writing](https://clojuredocs.org/clojure.core/spit) or an HTTP server library like [Ring](https://github.com/ring-clojure/ring) or [HTTP Kit](http://http-kit.github.io/).

## Conclusions

In this article, I’ve demonstrated how we can represent languages such as HTML in Clojure data structures, and how we can solve HTML transformation problems using Clojure as our tool. We’ve also seen some examples on how Clojure provides nice, out-of-the-box tooling such as traversal for its core data structures.

As always, I’ve included the code examples in a [GitHub Gist](https://gist.github.com/jkpl/e316ef7e975f1ff13205e3877f02d2d7) to play around with. Thanks for reading\!
