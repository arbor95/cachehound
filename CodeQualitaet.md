

# Einführung #
Da CacheHound ein Fork des Projekts CacheWolf ist, wurde natürlich mit dessen Code angefangen. Der CacheWolf Code zeichnet sich leider dadurch aus, dass er vielfach Funktionalität und GUI stark vermischt. Da CacheHound für den Desktop gedacht ist, bietet sich Ewe als Oberfläche eigentlich gar nicht so sehr an. Ein Fernziel ist daher, diesen Code auf ein anderes GUI-Framework (Swing?) umzubauen. Um das zu ermöglichen ist eine hohe Güte des Codes und vorallem die Trennung von Funktionalität und GUI unverzichtbar.

# Paketstruktur #
Alter, direkt aus dem CacheWolf Projekt übernommener Code, soll weiterhin in dessen Paketstruktur behalten werden. Der Grund hierfür ist, Bugfixes oder neue Fähigkeiten für den Cachewolf soweit möglich/nötig direkt übernommen werden können.

Für neuen oder stark überarbeiteten Code (etwa eine Reimplementierung auf Basis der Java-API) soll eine neue Paketstruktur genommen werden. Die Struktur ist nicht endgültig und soll gerade nach oben hin Erweitert werden. Ein Exporter-Paket mit 40 Klassen ist unübersichtlich, daher kann und sollte für jeden Exporter, der aus mehr als einer Klasse besteht, ein Unterpaket angelegt werden.

  * **de.cachehound.main** Alles was das Hauptprogramm ausmacht.
  * **de.cachehound.data** Alle Datenstrukturen (Beans und Listen), die z.B. einen Cache, Koordinaten oder die Verwaltung der Cacheliste ausmachen
  * **de.cachehound.exporter** Alles, mit dem Daten aus dem CacheHound exportiert werden
  * **de.cachehound.importer** Alles, was Daten in den CacheHound importiert
  * **de.cachehound.util** Hilfsklassen, die nicht zugeordnet werden können (wie etwa Algorithmen u.ä.)
  * **de.cachehound.gui.interfaces** Interfaces oder abstrakte Klassen, die GuiElemente darstellen
  * **de.cachehound.gui.ewe** Implementierung von GuiElementen, die eventuell Implementierungen aus _abstract_ darstellen oder aber volle eigenständige Elemente sind.

# Anforderungen an den Quellcode #

## Dateiformate ##

Abweichend zum CacheWolf sollen hier nur UTF-8 kodierte Ascii-Dateiformate genutzt werden. Für Zeilenende die Linux-Endungen, wobei das weniger entscheidend ist als, dass ausversehen CP1250 oder ähnliche Kodierung genutzt werden. Gerade beim Übernehmen von neuen Code aus dem CacheWolf muss daher aufgepasst werden, dass dieses ggf. umcodiert wird.

## Formatierung ##
**Kurz** - 4er Tab, öffnene Klammen hinter Methode/Schleifenkopf, Schließende in eigener Zeile,

Für die Formatierung des Quellcodes wird ersteinmal die von Eclipse als Default Einstellung vorhandene genommen. Das heißt es kann (und soll) reichlich mit dem Eclipse Formatierer (oder einem anderen, der auf den gleichen Einstellungen läuft) Formatiert werden. Des weiteren sollte eine Methode i.d.R. nicht eine Bildschirmhöhe überschreiten (d.h. ca. 30 Zeilen). Bei reinem lineraren Spaghetti-Code (etwa lange Strings, die für eine Ausgabe zusammengebaut werden müssen), kann davon abgewichen werden. Ansonsten sollte auf eine sinnvolle objektorientierte Trennung in Sub-Methoden angestrebt werden.

## Zugreifbarkeit ##
Auf Felder eines Objekts/einer Klasse soll nur über Methoden (vergl. Definition von Beans mit set() und get()-Methoden) zugegriffen werden, d.h. Felder sind private zu deklarieren. Zugriffsmethoden sollten nur hinzugefügt werden, wenn das auch sinnvoll erscheint, d.h. nicht auf Vorrat. Ebenso sollen Methode, welche von außen genutzt werden sollen auch wirklich als public deklariert werden, damit nicht Klassen nur auf Grund von Veachtung von "paket-friendly" in einem Paket landen.

## Kommentare ##
In jeder Klasse sollte min. ein ausführlicher Kommentar verfasst sein, **was** diese Klasse tut und wofür sie steht. Ebenso sollten _ausgefuchste_ Algorithmen kommentiert werden. Auch und gerade public-Methoden sind sind gute Kandidaten kommentiert zu werden.
Wenn in Settern etwa ein Wertebereich überprüft wird, dann sollte dieser Wertebereich auch aus den Kommentaren der Methode ersichtlich sein.
Ansonsten müssen bei einfachen Bean-Klassen nicht alle Variablen, set- und get-Methoden kommentiert werden. Der Name muss aber im Context des Geocachings und der Anwendung unmittelbar beschreibend sein.

# Nutzung von externen APIs #
Wenn eine Funktionalität bereits irgendwo implementiert ist, dann müssen wir das nicht noch einmal bauen. Daher sollen (zumindest halbwegs leichtgewichtige) APIs jeder Zeit im Projekt benutzt werden. Das hält den Code hoffentlich sauber und ermöglicht eine schnelle Entwicklung

# Beispiel #

```
/** 
 * Diese Klasse repräsentiert einen Cache mit seinen Attributen.
 * Im Grunde ist diese Klasse nur eine Sammlung von Feldern, settern und gettern.
 */
Class Cache {
    private String name;
    
    /** 
     * Speichert den Wert im Bereich 0 bis 9, wobei gilt: 
     * GC-Sterne = (difficulty / 2) + 0.5 - 0 bedeutet unbekannt 
    */
    private int difficulty; 

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * gibt die Difficulty als int-Wert zurück. Dabei gilt:
     * GC-Sterne = (difficulty / 2) + 0.5 
     * @return einen Wert zwischen 1 und 9, 0 für unbekannt
     */
    public int getDifficulty() {
        return difficulty;
    }

    /**
     * setzt die Difficulty als int-Wert. Dabei gilt:
     * GC-Sterne = (difficulty / 2) + 0.5
     * @param difficulty es muss gelten: 1 <= difficulty <= 9
     */
    public void setDifficulty(int difficulty) {
        if (difficulty >= 1 && difficulty =< 9) {
            throw new RuntimeException("Difficulty darf nur im Bereich von 1 bis 9 gesetzt werden: " + difficulty);
        this.difficulty = difficulty    
    }
}
```