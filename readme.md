
			WORK IN PROGRESS
	this project is still in incubation


### about

shaft is a lightweight web application framework written in scala. It focuses on:
* software developer's efficiency. along the lines of ruby on rails.
* scalability and reliability. utilizing the scala programming language and the JVM.
* freedom. shaft abstracts away common dependencies giving you the freedom of choosing the tools you prefer.

do we need yet another web application framework? the truth is we don't. that said, shaft addresses some of the common challenges in 
developing web applications in a different way. mainly, it attempts to balance good architecture, freedom of choice and ease of use. 
i built it because i was scratching an old itch. i needed it and i believe others can find it useful.

### shaft foundations

* shaft based application are developed in scala (or java). using statically typed languages helps keeping things in order and boosts performance. 
we all love dynamic languages, but they simply do not scale as well as their statically typed sisters.

* lightweight processes, message passing and immutable data.

* convention over configuration.

* shaft is an MVC framework.

* shaft domain model objects are pure. they are not ORM classes and do not deal with the data storage. shaft abstracts data storage using the  repository allowing simple transition and mixing of different storage types such as RDBMS (with/without ORM), NoSQL, programatic in-memory, 
file system and others.
  
* strong separation of concerns between the client and the server. shaft is a pure API server. as such, shaft's core view layer is super light and is strictly used for data serialization. that said, shaft plugins include several templating engines for more complex scenarios.
  
* shaft is web server agnostic. 

* shaft is data store agnostic.

* dependency injection at the controller layer

### todo

* scaffolding
* plugins architecture for web server
* better mechanisim for session managenment
* integrate unfiltered?

### license

scala-xmpp, a scala based xmpp framework
Copyright (C) 2012 mishlabs
www.mishlabs.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

***************************************************************************************

This project is work in progress. if you are interested in contributing, have comments
or want to share how you are using it please touch base via github
