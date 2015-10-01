Note from the author:
-----------------
I actually (and it's a shame to admit) had very little time to spend on Ganelon in the last two years, hence it is not being actively developed and I am not very happy with its architecture - web development practices for Clojure have moved on a lot. Still it is fun to use and you can build stuff with it really fast. 

I have also prepared another version which works much better and even supports Enlive (and doesn't bring ton of requirements), but the code isn't public as I need to find time/motivation to document it. With everybody going for Reagent/Om the last part is especially difficult, but if there is need for such framework, I might reconsider going back to that code or releasing it to the community.

Ganelon microframework
-----------------
Ganelon microframework brings rapid development of dynamic web applications to Clojure, by introducing thin JavaScript
layer for AJAX-based server-side management of page's content. To provide maximum flexibility and openness, Ganelon integrates
seamlessly with any other Compojure or Ring-enabled library or server.

More information, documentation, tutorials and live demo are available at [http://ganelon.tomeklipski.com/](http://ganelon.tomeklipski.com/).

API docs are provided as well: [http://ganelon.tomeklipski.com/doc/index.html](http://ganelon.tomeklipski.com/doc/index.html)

Installation
-----------------------
If you are using leiningen, just add following dependency to your project.clj file:

    [ganelon "0.9.0"]

License
-----------------------
Copyright © 2013 Tomek Lipski

Licensed under the EPL (same as Clojure). (See the file LICENSE.txt)
