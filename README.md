File Upload Examples/Experiments
================================

This repo just contains some experiments I've been doing with adding a file upload
facility to a Reagent based SPA. I'm currently working on a Reagent based SPA which
requires the ability for the user to upload files. Once uploaded, the files are
processed by the server and inserted into a database.

Of course the easy way to do this would be to just have a standard form embedded in
my page and let the browser pretty much handle the whole process. However, the files
I am working with can be quite large and can take a while to process. Therefore, I
wanted a little bit more control and want to make as much of this happen in the
*background* as possible. I also want to work out how to best provide feedback to the
user so that they know what stage of processing things are up to and so that they can
continue doing other *stuff* which does not depend on the data currently being
processed. For all these reasons, I figured it was better to upload the files using an
*Ajax* style approach.

So far, I have identified 3 different solutions which look promising. As usual, they
all have their own pros and cons. I'm not sure which approach I'll settle on
yet. However, I thought it might be worthwhile making what I've done available just
in case someone else might find it useful, might have some improvement suggestions or
might be able to identify yet another solution I've not tried.

So far, the three techniques I've looked at and will attempt are

	- cljs-ajax and js/FormData. I already use the cljs-ajax library to amke calls to
      the server, so this method seems like a good starting point. The limitation of
      using js/FormData is that not all browsers support it. In particular, Internet
      Exploader does not.

	- goog.net.IframIo. This looks like a good cross-browser solution which should
      work regardless of the browser. The only drawback is you have to drag in
      additional Google Closure stuff.

	- goog.net.XhrIo. This seems like the +recommended+ solution found in all the
      documentation I was able to locate for Google's Closure library. Not sure what
      the advantages/disadvantages are over IframIo. 


**WARNING** This repo is my playground and a rough experiment and PoC. It will almost
certainly have errors, will constantly be in need of re-factoring and will lack any
clear design. I will try to re-factor and clean things up once I feel a certain level
of stability has been reached. This is not a repo with clear canned recipes you can
just plug into your own project. However, it may just have enough information to get
you started or give you the inspiration for a better solution.  

I probably should mention, I'm not much of a web programmer. The last time I did any
serous web programming was in the late 90's using Java and Perl. Ajax had not yet
been proposed, Java was at version 1.0 and Perl was King/Queen. Mobile devices
consisted of dumb phones which were so large you had to hand them on your belt,
tablets were things prescribed by doctors and laptops were more luggable than
portable. At that time, Javascript was more a curiosity than a central tool for web
development. 

Recently, I've been inspired to get back up to speed. This has mainly been due to the
re-invigorating properties of programming in Clojure. For me, Clojure and more
recently ClojureScript have brought back an enjoyment of programming I'd nearly
forgotten. While my progress has been OK (after some years of working with various
Lisp dialects back in the 80's, I'm pretty comfortable with the basic concepts of
Clojure), perhaps my weakest area is still JavaScript. This can mean that some of my
code, especially where I need to interact at a lower level in my ClojureScript, is a
bit clunky, even less elegant than normal and probably has lots of mistakes. I am
trying to work on my Javascript skills, but to be honest, whenever I start looking at
Javascript code, my eyes begin to hurt, I get an ache in my stomach and spend more
time wondering how such an ugly child managed to become so popular. I guess beauty
really is in the eye of the beholder. Regardless of my irrelevant assessment of
Javascript, there is no denying that it has significantly enhanced web applications
and inspired solutions which simply were not possible without it. 

