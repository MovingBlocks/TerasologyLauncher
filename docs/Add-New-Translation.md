# Add new translation

Adding new languages to the launcher's portfolio or helping out with translating single labels or messages is quite easy. You only need to decide on the target language you want to translate to and find the appropriate [language code](http://en.wikipedia.org/wiki/ISO_639-1) for your target language. In the following steps, `<code>` will represent the associated language code for your target language `<language>`.

1. Copy the English **labels** [`LabelsBundle_en.properties`](https://github.com/MovingBlocks/TerasologyLauncher/blob/develop/src/main/resources/org/terasology/launcher/bundle/LabelsBundle_en.properties) to a new file `LabelsBundle_<code>.properties` in 
    ```
    src/main/resources/org/terasology/launcher/bundle/
    ``` 
1. Translate everything to your target language. The property files need to be valid Java properties files ([ISO 8859-1 character encoding](http://docs.oracle.com/javase/7/docs/api/java/util/Properties.html)).
1. Repeat (1) and (2) for the **messages** in `MessageBundle_en.properties`.
1. Add a `settings_language_<code>=...` entry to each `LabelsBundle_*.properties` file for other languages. This is used to display the new translation in the settings. The value of this entry is the translated name of your target language (e.g., in when translating to German, the entry in `LabelsBundle_en.properties` would be `settings_language_de=German`, while in `LabelsBundle_de.properties` it would be `settings_language_de=Deutsch`.
1. Modify [`src/main/java/org/terasology/launcher/util/Languages.java`](https://github.com/MovingBlocks/TerasologyLauncher/blob/master/src/main/java/org/terasology/launcher/util/Languages.java): create a new locale, add your target language to the supported locales and select the settings label.
1. Add a flag icon to represent your target language in the settings menu. To do this, download the appropriate icon from the [famfamfam.com icon pack](http://www.famfamfam.com/lab/icons/flags/) into [`src/main/resources/org/terasology/launcher/images/flags`](https://github.com/MovingBlocks/TerasologyLauncher/tree/master/src/main/resources/org/terasology/launcher/images/flags) (or create your own 16x11 icon!)
1. Add a `flag_<code>=...` entry to the `ImageBundle.properties` file located in the same folder as the `LabelsBundle_*.properties` file you've edited previously.

To keep everything nice and clean, we advise you to use a **feature branch**:

- `git checkout master`
- `git checkout -b <language>-translation`
- Add/Change files
- `git commit ....`
- `git push`
- Open pull request
