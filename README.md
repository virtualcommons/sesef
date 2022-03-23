### sesef
The Social Ecological Systems Experiment Framework (_sesef_) is a Java framework providing networking, persistence, and
configuration support for real-time interactive collective action experiments. For concrete examples, see the
[foraging](https://github.com/virtualcommons/foraging)
and [irrigation](https://github.com/virtualcommons/irrigation) experiments. 

### features and motivation
The framework was developed from several years of experience developing interactive collective action experiments with
10-30 participants in a controlled system / computer lab. A common core was extracted with support for:

- experiment-wide and round-specific parameterizations via [Java properties files](http://docs.oracle.com/javase/7/docs/api/java/util/Properties.html)
- networking, threading, data model, configuration, and user interface scaffolding for clients, facilitators, and
  servers
- event channel abstraction that decouples messaging between different system components (persistence, resource
  generation, client and server side data models and the user interface)
- scaffolding for automatic binary and XML based persistence of user events

### how to contribute
There are many areas of improvement remaining:

1. integration with http://netty.io for better networking support, finish implementing client and server NettyDispatchers
2. easier web-based deployment (perhaps Play or dropwizard?) and a facilitator / experiment webapp
3. standardized data export tools
4. improved persistence mechanisms (currently XStream and standard Java serialization of PersistableEvents in a time ordered stream, brittle with class changes).
5. P2P network synchronization to handle larger scale experiments
6. better UI support and scaffolding

### current status
[![Java CI with Maven](https://github.com/virtualcommons/sesef/actions/workflows/maven.yml/badge.svg)](https://github.com/virtualcommons/sesef/actions/workflows/maven.yml)

Current development is focused on [web-based collective action experiments](https://github.com/virtualcommons/). If you'd like to use sesef to develop new experiments or are interested in customizing any of our existing experiments, please [let us know](http://commons.asu.edu/contact). 

Development supported by the [ASU Center for Behavior, Institutions, and the Environment](https://complexity.asu.edu/cbie) and the [National Science Foundation](http://nsf.gov).
