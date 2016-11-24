(ns lepo.example-data)

(def page-contents
  ["
{:title \"Hello World!\"
 :author-id :jsmith
 :description \"cool story\"
 :tags [\"mytag\"]}

---

<p>Hello World!</p>"
   "
{:title \"New Year!\"
 :author-id :jdoe
 :description \"yay!\"
 :tags []}

---

<p>Happy New Year!</p>"
   "
{:title \"Summer time!\"
 :author-id :jdoe
 :tags [\"party\"]}

---

<p>so hot</p>"
    "{:description \"my homepage\"}

---

<p>Hi, I'm John Smith!</p>"
    "
{:title \"ZOMBOCOM\"
 :description \"Welcome to ZOMBOCOM\"
 :template \"zombo\"}

---

<p>You can do anything in ZOMBOCOM!</p>"])

(def page-filenames
  ["/posts/2015-12-01-hello-world.html"
   "/posts/2016-01-01-new-year.html"
   "/posts/2016-06-15-summer-time.html"
   "/team/jsmith/index.html"
   "/zombo.html"])

(def pages
  (map vector page-filenames page-contents))
