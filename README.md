### csidex
_csidex_ is a Java framework providing networking, persistence, and configuration support for real-time interactive collective action experiments. For concrete examples, see the [foraging](https://github.com/virtualcommons/foraging) and [irrigation](https://github.com/virtualcommons/irrigation) experiments. 


### motivation
The framework was extracted from several years of experience developing interactive collective action experiments with 10-30 participants in a computer lab with support for parameterized configuration, automatic binary and XML-based persistence of experiment data, and basic networking. 


### how to contribute
There are many areas of improvement including:

1. integration with http://netty.io for more robust networking support
2. web-based deployment
3. data export tools
4. better persistence mechanisms (currently XStream and standard Java serialization of PersistableEvents in a time ordered stream).
5. P2P network synchronization to handle larger scale experiments
6. better UI support

### current status
This framework is _not under active development_ at this time as we are more focused on [developing large scale collective action experiments over the web](https://github.com/virtualcommons/vcweb). If you'd like to use csidex to develop new experiments or are interested in customizing any of our existing experiments, please [let us know](http://vcweb.asu.edu/contact). We are also working on 
 [developer documentation](https://github.com/virtualcommons/csidex/wiki/Developer-Documentation) that describe how to implement new experiments.

Development has been supported by the [Center for the Study of Institutional Diversity](http://csid.asu.edu) and the [National Science Foundation](http://nsf.gov).
