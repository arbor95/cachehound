#!/bin/sh
java -Dlogback.configurationFile=config/logback.dist.xml -cp CacheHound.jar:ewe.jar:ewe_misc.jar:mail.jar:slf4j-api-1.5.8.jar:slf4j-ext-1.5.8.jar:logback-classic-0.9.16.jar:logback-core-0.9.16.jar:TableLayout.jar ewe.applet.Applet CacheWolf.CacheWolf
