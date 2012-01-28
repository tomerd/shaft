********************************************
			   WORK IN PROGRESS

	this project is still in incubation
********************************************

shaft is an open source web application framework. shaft focuses on:
1. software developer's efficiency, along the lines of ruby on rails.
2. scalability and reliability, utilizing the scala programming language and the JVM
3. freedom, shaft abstracts away common dependencies giving you the freedom of choosing the tools you prefer.


why do we need yet another web application framework?
the truth is we don't. that said, shaft addresses some of the common issues in web application frameworks while keeping things straight forward for the
software developers. shaft recognises and utlizes good concepts found in other frameworks but strikes a different balance to achieve better results. 
i built it because i needed such framework and i believe others can find it useful as well.

shaft foundations:

* shaft based application are developed in scala (or java). using statically typed languages keeps things in order and boosts performance. 
we all love dynamic languages, but they simply do not scale as well as statically types ones.  

* light weight processes, message passing and immutable data. shaft utilizes scala's actors to achieve maximum scalability and reliability.

* convention over configuration.

* shaft is an MVC framework.

* shaft domain model objects are pure. they are not ORM classes and do not deal with the data storage. shaft abstracts data access using a 
data access layer allowing simple transition and mixing of different storage types such as RDBMS (with/without ORM), NoSQL, programatic in-memory, 
file system and and others.
  
* shaft is a pure API server. as such, shaft view layer is light and is strictly used for data encoding. most of it is done automagically for you, 
but shaft includes a view engine for more complex scenarios.
i believe in strong separation of concerns between the client and the server. shaft server does not generate any display code because i believe 
display is not the server's concern, there are plenty client frameworks for this job and shaft offers [optional] adapters for the common ones. 
  
* shaft is web server agnostic. it can be run behind any java web server container or behind a netty server (more to come)

* shaft is data store agnostic (see domain model objects above)

* dependency injection at the controller layer

 shaft utilized the following open source libraries:
...


TODO
----
* scaffolding
* plugins architecture for view engine, rdbms data apater, jetty, other parts
* session facrory or some other, better mechanisim for session managenment
* integrate scalate
* integrate unfiltered?



