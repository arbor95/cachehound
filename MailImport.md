# Einführung #
_Achtung, dieses Feature richtet sich nur an Premium Mitglieder bei Geocaching.com_

Als erstes großes neues Feature gegenüber dem CacheWolf hat der CacheHound die Möglichkeit Mails von Groundspeak direkt aus einer Mailbox zu lesen. Momentan kann er sowohl PocketQuerys als auch Notify-Mails parsen und verarbeiten. Weil das Feature noch als experimental einzuschätzen ist, wird der CacheHound keine Mails löschen. Am besten eignet sich ein eigener Mailaccount, der über Imap abgeholt wird. Ich empfehle hier einen Mailaccount bei http://www.googlemail.com anzulegen, da dieser ausreichend Platz hat und auch per Imap abholbar ist (muss erst in den Einstellungen aktiviert werden).

# Vorgehen #

## Anlegen der PQ ##
Als erstes sollte man sich ein (oder mehrere) Gebiet(e) aussuchen, die man aktuell halten möchte. Damit sowohl als PQs als auch Notifys runde Gebiete abdecken, empfiehlt es sich, das Gebiet (etwa 20 km um eine Koordinate) zeitlich in mehrere PQs zu zerteilen. Hierzu wird eine PQ angelegt, die den Suchkriterien entspricht (ich lass mir einfach alle Caches liefern, filtern kann ich später im CacheHound). Diese kann man nun kopieren und jeweils den Zeitraum anpassen, so dass man mehrere PQs erhält, die zeitlich nacheinander das Gebiet abdecken: Ein anschauliches vereinfachtes Beispiel:
  * PQ-1: 01.01.1998 bis 31.12.2003
  * PQ-2: 01.01.2004 bis 31.12.2005
  * PQ-3: 01.01.2005 bis 31.12.2007
  * PQ-4: 01.01.2007 bis 05.07.2008
  * PQ-5: 06.07.2008 bis 01.08.2009
  * PQ-6: 02.08.2009 bis 31.12.2010
Es empfielt sich, gleich ein paar PQs für die Zukunft anzulegen, damit man nicht monatlich nachschauen muss, ob die PQs überlaufen, weil ja in eine PQ nur 500 Caches passen. Später empfielt es sich dann, alle halbe Jahr die PQs zu überarbeiten (Zeiten anpassen, so dass wieder knapp 500 Caches in einer PQ sind) und neue anzulegen, die das nächste halbe Jahr großzügig abdecken.

## Anlegen der Notifys ##
Soweit so gut, aber eigentlich alles ziemlich langweilig ... bis auf das automatische Abrufen hat sich noch nicht viel zum vorherigen Zustand getan. Aber es wäre ja schön, neue Caches sofort in der Datenbank zu haben, oder aber Caches die archiviert werden, auch sofort aus der Datenbank zu entfernen (bzw. zu markieren). Dafür sind die Notifys sehr praktisch. Leider muss man für jeden Cachetype, den Groundspeak unterstützt, eine eigene Notify-Benachrichtigung einrichten (ja, ist 'ne verfluchte Arbeit, wenn man alle oder fast alle Cachetypen berücksichten möchte). Es können alle Notifications gelesen werden und die empfangen Logs werden auch dem Cache hinzugefügt und ggf. der Status geändert.  Auch wenn Found- und DNF-Logs verarbeitet werden können, sollte man diese wohl lieber nicht den Notification Mails hinzufügen, da diese sehr viele Mails erzeugen würden. Die anderen Logtypen können aber eigentlich genommen werden, die Anzahl der Mails hält sich in vernünftigen Grenzen)

## Einstellungen im CacheHound ##
Vor dem Aufruf 'Anwendung' -> 'Import' -> 'Import Mails' müssen unter 'Anwendung' -> 'Einstellungen' -> 'Mail' noch einige Einstellungen gemacht werden.
Während Longin-Name, Passwort, Server sowie die Häkchen wohl noch selbsterklärend sind, ist das Protokol noch wichtig. Folgende Werte werden unterstützt:
| IMAP | Abholung per IMAP-Protokol, unverschlüsselt | Mails werden nach erfolgreicher Verarbeitung entsprechend den Einstellungen behandeln |
|:-----|:--------------------------------------------|:--------------------------------------------------------------------------------------|
| IMAPS | Abholung per IMAP-Protokol + SSL, verschlüsselt | Mails werden nach erfolgreicher entsprechend den Einstellungen behandeln              |
| POP3 | Abholung per POP3-Protokol, unverschlüsselt | Mails können nicht markiert werden, bleiben daher neu und müssen etwa mit einem EMailProgramm abgerufen/gelöscht werden. |
| POP3S | Abholung per POP3-Protokol + SSL, verschlüsselt | Mails können nicht markiert werden, bleiben daher neu und müssen etwa mit einem EMailProgramm abgerufen/gelöscht werden. |
**Bemerkung:** Bevor ich das Löschen auch für das POP3 Protokoll freischalte, möchte ich erst ein wenig Erfahrung mit der EMail Verarbeitung gesammelt haben. Feedback von Nutzern wäre dazu gut. _(TweetyHH)_

# Bekannte Probleme #
~~Wenn die Verarbeitung einer Mail zu lange dauert (Zeit noch unbekannt, tritt bei mir auf, wenn ich eine PQ einlese und deren Bilder nachlade), bricht die Mailverarbeitung auf Grund eines Timeouts seitens des Servers ab. Dabei werden die verarbeiteten Mails auch nicht als gelesen markiert. Dieses müssen dann entweder per Hand als bereits gelesen markiert werden oder aber bei nächsten aufruf der Funktion werden die Daten nochmals runtergeladen und verarbeitet (Da bei bereits importieren Caches die Bilder nicht neu gespidert werden, geht das entsprechend schnell, aber je nach Internetverbindung kann das Runterladen der PQ-Datei selber ziemlich zeitraubend sein).~~

Folgendes Verhalten kann auftreten wenn sowohl Notifys wie auch PQs genutzt werden: Wenn ein Log nachträglich verändert wird, kann es passieren, dass dieses zwei mal in der Datenbank gespeichert wird. Einmal in der Form wie es per Mail zugestellt wurde. Das andere mal in der Form, wie es per PQ zugestellt wurde. Verhindern liese sich das wohl nur, wenn man pro User nur ein Log pro Typ und Tag zulässt. Da aber nicht entschieden werden kann, welches das "informativere" Log ist, wird bewusst darauf verzichtet hier eins von beiden zu löschen.

# Liste der Aktionen bei eine Notify-Mail #
| Found | Log wird der Datenbank hinzugefügt |
|:------|:-----------------------------------|
| DNF   | Log wird der Datenbank hinzugefügt |
| publish | Cache wird nachgeladen             |
| archive | Cache wird als archiviert markiert |
| Owner Maintenance | Cache wird nachgeladen, es könnte sich die Beschreibung geändert haben |
| Needs Maintenance | Wartungs-Symbol wird gesetzt       |
| retract | Cache wird als archiviert markiert |
| update coordinates | Cache wird nachgeladen, es könnte sich die Beschreibung geändert haben |
| enable Listing | Cache wird auf aktiv gesetzt       |
| disable Listing | Cache wird auf inaktiv gesetzt     |

# Zukünftig geplante Änderungen #
  * ~~Verhindern, dass ein Timeout entsteht.~~ **done**
  * Verarbeiten von Watchlist-Mails. _(Doch nicht so einfach => keine GCNummer sondern nur die ID.)_
  * ~~Unterstützung von Ordnern bei IMAP inkl. Verschieben.~~ **done**
  * Verbesserte Ausgaben bei Fehlern