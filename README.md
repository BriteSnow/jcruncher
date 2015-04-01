## Intro 

Simple Java 8 library as well as command line (i.e. executable jar) to compile lesscss and handlebars using Java 8 Nashorn JS VM.

## As Executable Jar (i.e. jcruncherEx)

[jcruncherEx download &amp; documentation](http://jcruncher.org/)

#### Building the executable jar

To build this library as a exectable jar (i.e. jcruncherEx.jar), use the following maven command:

```
mvn clean compile assembly:single
```

## Support and License
Currently support 0.9.3: 
- **lesscss 1.7.5** Unfortunately lesscss 2.x major refactoring seems to make nashorn support harder, or at least different from previous 1.x versions. Consequently, quite a bit of work is needed, and not clear when we can get to it.
- **handlebars 2.0** Currently working on supporting handlbars 3.0.0 (already on master branch, 0.9.4-SNAPSHOT)

Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0

Lesscss resource loading and exception handling java code come mostly from https://github.com/asual/lesscss-engine



## As Library

jcruncher as an library (i.e. API) is not finalized yet, but feel free to use the ```LessProcessor``` and ```HbsProcessor``` classes to compile lesscss or handlebars respectively. Make sure to reuse the Processor as they create their own Nashorn script environments. 

#### Warning

As of now, the LessProcess or HbsProcessor should be threadsafe, but more verification needs to be made.
