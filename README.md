Cooma is a simple microcontainer implementation of Java.

Documentation page: <https://github.com/metaframe/cooma/wiki>
Report problems or suggestions: <https://github.com/metaframe/cooma/issues>
Code Project page: <https://github.com/metaframe/cooma>

Code compile and browse tutorial
==================================

Checkout
--------------

```bash
git clone https://github.com/metaframe/cooma.git
```

Compile
---------------

```bash
mvn install -Dmaven.test.skip
```

Browse
---------------

Eclipse:

```bash
mvn eclipse:eclipse -DdownloadSources

Eclipse -> File -> Import -> Existing Projects into Workspace -> Browse -> Finished
```

Idea:

```bash
mvn idea:idea -DdownloadSources

Idea -> File -> Open -> Browse project directory -> OK
```

Related Product
=========================

- [Spring](http://www.springsource.org/)
- [Google Guice](http://code.google.com/p/google-guice/)
- [microcontainer of JBoss](http://www.jboss.org/jbossmc/) 
- [Excalibur](http://excalibur.apache.org/)
- [Loom](http://loom.codehaus.org/)
- [nanocontainer](http://nanocontainer.codehaus.org/)

Design Principle
=========================

- Tiny and simple. Less than 1000 lines of code(not including code comments).
- Concept independence.
- Integration friendly.