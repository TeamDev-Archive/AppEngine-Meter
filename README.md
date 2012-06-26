Summary
=======

This [JMeter](http://jmeter.apache.org/) plugin shows [Google App Engine](https://developers.google.com/appengine/) estimated costs by request:

![Screenshot](http://content.screencast.com/users/pavel.kaplin/folders/Jing/media/c8265b57-659e-4695-8320-2327a9234674/00000021.png)

This information could help you to see what requests are most expensive, 
and have realistic and detailed picture about you application cost. 

Install
=======

1. You will need JMeter 2.7 or above (earlier versions will work most probably; however plugin was not tested with them)
2. [Dowonload](https://github.com/TeamDev-Ltd/AppEngine-Meter/downloads) latest version of `appengine-meter.jar`
3. Put it to `<JMETER_HOME>/lib/ext` 
4. Restart JMeter

Use
=======

App Engine Meter is very similar to other [JMeter listeners](http://jmeter.apache.org/usermanual/listeners.html). 
Just add it to your test plan: right-click on your `Thread Group` and select `Add->Listener->App Engine Cost Report`. 
That's it!

Contribute
==========

Set Up Development Environment
------------------------------

1. Create folder for project
2. Checkout JMeter 2.7
> `svn co http://svn.apache.org/repos/asf/jmeter/tags/v2_7 jmeter2.7`

3. Get Appengine-Meter sources
> `git clone https://github.com/TeamDev-Ltd/AppEngine-Meter.git appengine-meter`

4. Build JMeter
> `cd jmeter2.7`<br/>
> `ant download_jars package`

5. Copy `jmeter2.7/lib` folder to `appengine-meter`
6. Start Eclipse and create workspace in folder created in the first step
7. Copy `jmeter2.7/eclipse.classpath` to `jmeter2.7/.classpath`
8. In Eclipse, create Java Project with name `jmeter2.7` and let Eclipse set up all source folders and libs automatically
9. Go to `jmeter2.7` properties, then `Java Build Path`, then `Order and Export`. Click `Select All` and then `OK`
10. Import `appengine-meter` as existing Eclipse project

Run and Debug Locally
---------------------

There is ready-to-use launch configuration within project.

In menu, select `Run->Run Configurations` and choose `JMeter 2.7 with App Engine Meter` 
which is under `Java Application` section.

Build JAR
---------

Double-click on `export-jar.jardesc`, then click `Finish`. Then look for `appengine-meter.jar` \
in the same folder.