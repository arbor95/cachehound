ď»żWie bekomme ich eine brandaktuelle Version des CacheWolf?

Die Sourcen von CacheWolf sind in einem Subversion-Repository bei www.berlios.de (Details siehe https://developer.berlios.de/svn/?group_id=2211) vorhanden, hierauf kann jeder lesend zugreifen. Schreibrechte werden auf Anfrage erteilt. Nachfolgend ist beschrieben, wie man sich die aktuelle Entwicklerversion besorgen kann und zum Laufen benommt.

CacheWolf wurde fĂĽr und mit Ewe (http://www.ewesoft.com/) in Java programmiert. Ewe bietet fĂĽr viele Plattformen sogenannte virtuelle Maschienen an, auĂźerdem kĂ¶nnen fĂĽr Ewe geschriebene Programme auf allen Plattformen laufen, die eine Java-Laufzeitumgebung haben (nicht zu verwechseln mit dem "Handy-Java" J2ME).

BenĂ¶tigte Programme
- JDK von Sun (http://java.sun.com/javase/downloads/index.jsp), enthĂ¤lt auch die Java Laufzeitumgebung
- Ein Subversion (SVN) Client, z.B. Tortoise fĂĽr WinXP (http://tortoisesvn.tigris.org/), kdesvn fĂĽr Linux (KDE) oder das subclipse-Plugin fĂĽr eclipse
- Das Ewe-Developer-SDK (z.B. http://www.ewesoft.com/Downloads/Ewe149-Developer-SDK.zip)

Java-Version
- Checkout der aktuellen Sourcen aus dem Repository (z.B. http://svn.berlios.de/svnroot/repos/cachewolf/trunk)
- Das Verzeichnis kann lokal umbenannt werden, z.B. in CacheWolf
- Es sollte bereits ein Verzeichnis bin/CacheWolf geben, falls nicht, bitte anlegen
- Script compile.bat (WinXP) bzw. ./compile.sh (Linux) ausfĂĽhren. Es gibt etwa 10 Warnings.
- Script getRes.bat bzw. ./getRes.sh ausfĂĽhren. Damit werden u.a. die Image-Dateien in das Work-Verzeichnis kopiert
- Script runwolf.bat bzw. ./runwolf.sh ausfĂĽhren. Damit wird der CacheWolf im Work-Verzeichnis gestartet. Das Datenverzeichnis sollte man irgendwo anders hinlegen, z.B. parallel zum CacheWolf-Verzeichnis.

AusfĂĽhrbare Versionen erzeugen, z.B. fĂĽr WinXP oder PPC
- Parallel zum CacheWolf-Verzeichnis ein Verzeichnis Ewe/programs anlegen, die Dateien finden sich im Ewe-Developer-SDK. Da dieses Verzeichnis von den Scripten relativ (also per ../Ewe/programs) angesprochen wird, auf genaue Einhaltung der Namen achten.
  Der Inhalt des Verziechnisses ist bei mir unter Linux wie folgt:
  -rw-r--r-- 1 kalle kalle      29 2006-07-28 20:43 Ewesoft-Jewel.cfg
  -rw-r--r-- 1 kalle kalle 3830895 2005-12-19 19:27 JavaEwe.zip
  -rw-r--r-- 1 kalle kalle 2748046 2005-12-19 16:52 JewelData.jar
  -rw-r--r-- 1 kalle kalle  254444 2005-11-26 23:53 Jewel.ewe
  -rw-r--r-- 1 kalle kalle      47 2005-01-20 18:44 RunJewel.bat
  -rwxr-xr-x 1 kalle kalle      47 2006-07-24 21:30 runjewel.sh
- Script buildexe.bat bzw. ./buildexe.sh aufrufen, es wird ein Verzeichnis CacheWolf erzeugt mit Unterverzeichnissen fĂĽr die unterschiedlichen Plattformen.
- mit dem Script runjewel kĂ¶nnen Ă„nderungen an der Datei cwberlios.jnf vorgenommen werden.

An weiteren Dateien neben CacheWolf.ewe werden im Programmverzeichnis noch *.def, *.html, *.tpl, *.zip und attributes/*.gif aus dem Verzeichnis â€žresourcesâ€ś benĂ¶tigt. Wahlweise kann man stattdessen auch resources/attributes-big/*.gif in Â«ProgrammverzeichnisÂ»/attributes/ packen.
