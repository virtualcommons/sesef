### sesef
The Social Ecological Systems Experiment Framework (_sesef_) is a Java framework providing networking, persistence, and
configuration support for real-time interactive collective action experiments. For concrete examples, see the
[foraging](https://bitbucket.org/virtualcommons/foraging)
and [irrigation](https://bitbucket.org/virtualcommons/irrigation) experiments. 

### features
The framework was developed from several years of experience developing interactive collective action experiments with
10-30 participants in a controlled system / computer lab. 

- support for experiment-wide and round-specific parameterizations via [Java properties files](http://docs.oracle.com/javase/7/docs/api/java/util/Properties.html)
- abstractions and scaffolding for clients, servers, and facilitators
- event channel abstraction that decouples messaging between different system components (persistence, resource
  generation, client and server side data models and the user interface)
- scaffolding for automatic binary and XML based persistence of user events

### motivation

### how to contribute
There are many areas of improvement including:

1. integrate with http://netty.io, finish implementing client and server NettyDispatcherS
2. web-based deployment (perhaps Play or dropwizard?)
3. standardized data export tools
4. improved persistence mechanisms (currently XStream and standard Java serialization of PersistableEvents in a time ordered stream, brittle with class changes).
5. P2P network synchronization to handle larger scale experiments
6. better UI support and scaffolding

### current status
Current development is focused on [web-based collective action experiments](https://bitbucket.org/virtualcommons/vcweb). If you'd like to use sesef to develop new experiments or are interested in customizing any of our existing experiments, please [let us know](http://vcweb.asu.edu/contact). 
We are also working on [developer documentation](https://bitbucket.org/virtualcommons/sesef/wiki/Home) that describe how to implement new experiments.

Development supported by the [Center for the Study of Institutional Diversity](http://csid.asu.edu) and the [National Science Foundation](http://nsf.gov).
