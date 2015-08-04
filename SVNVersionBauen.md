# Einleitung #

Neben den Download von "Binär" Releases (sofern man das bei Java so bezeichnen möchte) kann auch die jeweils aktuellste Version direkt selber gebaut werden. Die Wahrscheinlichkeit, dass das Programm Fehler enthält ist natürlich höher, aber dafür hat man auch sofort die aktuellsten Features vorliegen. Daher die Profile Ordner am besten Backupen, dann kann auch eigentlich nichts wirklich schlimmes passieren.

Hier sollen nun kurz die Schritte zum selbstständigen Bauen des CacheHounds beschrieben werden. Eigentlich ist das gar nicht so schwer, sondern auch für _Anfänger_ innerhalb kurzer Zeit inkl. Installation aller Tools zu schaffen (und die müssen nur einmal Installiert werden).

# Software Anforderungen #

Die folgenden 3 Werkzeuge müssen Installiert werden. Ich geh davon aus, dass die Werkzeuge auf der Konsole genutzt werden.

  * JDK Version 6
  * SVN Client
  * ANT

Unter Linux ist dies in der Regel schnell über den Paketmanager erledigt. Beispielsweise gebe ich den Befehl der Installation für Ubuntu an:
```
sudo apt-get install sun-java6-jdk ant subversion
```

## Installation für Anfänger (eher Windows ;-) ) ##
Wer eh regelmäßig mal mit Java entwickelt braucht diese Werkzeuge wahrscheinlich nicht installieren, weil er sie eh hat oder aber auf eine IDE zurückgreift.

### Java JDK 6 ###
Zu finden ist das ganze auf http://java.sun.com/javase/downloads/index.jsp. Dort ist es (momentan) der vierte Link: JDK 6 Update 14 (wenn neuere Updates herauskommen, können die selbstverständlich auch genommen werden).

### SVN Client ###
Hier reicht ein einfacher Consolen Client. Diverse Binaries für verschiedene OS-System findet man hier: http://subversion.tigris.org/getting.html. Ein direkter Link für Windows wäre z.B. dieser hier: http://www.collab.net/downloads/subversion/ Wer lieber einen grafischen Client benutzt, kann etwa http://tortoisesvn.tigris.org/ benutzen. Allerdings kann er dann der Beschreibung nicht ganz folgen, da er für das Auschecken des Codes aus dem SVN-Repositories dann mehr klicken statt tippen muss.

### Ant ###
Ant findet man hier: http://ant.apache.org/bindownload.cgi

### Installation vollständig? ###

Nach der Installation der Tools kann man prüfen, ob die Installation erfolgreich war, in dem er eine Console öffnet und dort 3 kurze Eingaben macht (Start => Ausführen => cmd eingeben und OK drücken (oder so)). In dem erscheinenden Fenster jeweils die erste Zeile ohne > eingeben. Hier die Ausgaben der Linux Varianten, Windows wird ähnlich aussehen :

```
> svn
Geben Sie »svn help« für weitere Hilfe ein.
```
```
> ant
Buildfile: build.xml does not exist!
Build failed
```
```
> java -version
java version "1.6.0_14"
Java(TM) SE Runtime Environment (build 1.6.0_14-b08)
Java HotSpot(TM) 64-Bit Server VM (build 14.0-b16, mixed mode)
```
Hier sollte auch die Versionsnummer mit der herunter geladenen übereinstimmen.
Wenn ein Befehl nicht geklappt hat, muss wahrscheinlich die PATH Variable angepasst werden. [Google hilft bestimmt](http://www.google.de/search?q=Windows+PATH+setzen)

## Jetzt gehts los ##

Als erstes wechselt man in der Console in den Ordner, in den man den Code auschecken möchte (i.d.R. geschieht das mit dem Befehl `cd]`.
Danach gibt man folgende Zeile ein, um den Code vom SVN-Server zu laden (kann ein paar Minuten dauern, wenn der Server belastet ist):
```
> svn checkout http://cachehound.googlecode.com/svn/trunk/ cachehound
```
Wenn man später den Code aktualisieren möchte, weil etwa ein neues Feature implementiert wurde, muss man nicht den kompletten Checkout wiederholen sondern wechseln in das Verzeichnis cachehound und gibt dort folgenden Befehl ein (schaden tut es aber auch direkt nach dem Checkout nicht, wenn man es denn ausprobieren möchte).
```
> cd cachehound
> svn update
```
Jetzt muss nur noch in das cachehound Berzeichnis gewechselt (sofern noch nicht gesehen) werden und ant aufgerufen werden
```
> cd cachehound
> ant
*ziemlich viel Text, die letzten beiden Zeilen lauten hoffentlich*
BUILD SUCCESSFUL
Total time: 34 seconds
```

Jetzt kann endweder in das **work** Verzeichnis gewechselt werden, dort kann dann die cachehound.bat (Windows) bzw. cachehound.sh (Linux) ausgeführt werden.
```
> cd work
> cachehound.bat
bzw. für Linux
> sh cachehound.sh
```
Alternativ liegt im Order **dist** eine Zip-Datei, die genutzt werden kann um den Cachehound auf einem anderen Rechner oder in einem anderen Verzeichnis zu "installieren" (naja, nur entpacken reicht. Denkt daran, dass
auf dem Computer auch Java Installiert sein muss, dort reicht allerdings das [JRE (Java Runtime Environment)](http://java.sun.com/javase/downloads/index.jsp).

_Anmerkung: Die Anleitung ist "aus dem Kopf" geschrieben, d.h. sie kann durchaus noch Fehler oder Ungenauigkeiten enthalten. Wenn Probleme auftreten wäre Feedback toll._