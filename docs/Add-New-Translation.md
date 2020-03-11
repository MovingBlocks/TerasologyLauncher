[[HOME|Home]] > [[TERASOLOGY LAUNCHER DOCUMENTATION|Documentation]] > [[ADD NEW TRANSLATION|add-new-translation]]

Adding new languages to the launcher's portfolio or helping out with translating single labels or messages is quite easy. We offer two ways of doing so:

1. Using our [Weblate](http://weblate.org) web interface at [translate.terasology.org](http://translate.terasology.org/) (**Recommended**)
2. Manually creating/editing text bundle files and a single Java file.

We strongly encourage you to use the Weblate interface as it allows for easy review of new translations or suggested changes. 

# Using Weblate
Using the Weblate interface is really easy! To start off, you can just log-in with your GitHub Account. By doing so, your changes and translations will be recognized correctly in the Git commits. 

All translations needed for **TerasologyLauncher** can be found in the corresponding Weblate project:
- **[Weblate/TerasologyLauncher](http://translate.terasology.org/projects/terasologylauncher/)**

There are two sub-projects: one for *Labels* (all descriptive labels and button texts of the launcher's UI) and one for *Messages* (update dialog, warnings, and error messages). Those two sub-projects can be found under
- **[Weblate/TerasologyLauncher/Labels](http://translate.terasology.org/projects/terasologylauncher/labels/)** 
- **[Weblate/TerasologyLauncher/Messages](http://translate.terasology.org/projects/terasologylauncher/messages/)**

You can add any new language to the round-up via the web interface. Note that English is the launcher's default language and can thus only be modified using [manual translation](#Manual Translation). For more information on how to use Weblate, see the official [Translator guide](http://weblate.readthedocs.org/en/weblate-1.9/user/index.html).

# Manual Translation
Suppose you have done a translation to German. The necessary steps would be:
  * Copy [`LabelsBundle_en.properties`](https://github.com/MovingBlocks/TerasologyLauncher/blob/develop/src/main/resources/org/terasology/launcher/bundle/LabelsBundle_en.properties) to a new file `LabelsBundle_de.properties` in ` src/main/resources/org/terasology/launcher/bundle/`. Replace the language suffix with the appropriate one (see http://en.wikipedia.org/wiki/ISO_639-1). Translate everything to your target language. The property files need to be valid Java properties files (ISO 8859-1 character encoding). (see http://docs.oracle.com/javase/7/docs/api/java/util/Properties.html).
  * Do the same for `MessageBundle_de.properties`
  * Add a `settings_language_de=...` entry to each `LabelsBundle_*.properties` file for other languages. This is used to display the new translation in the settings. Put only the translated name of the language as value, e.g. `settings_language_de=German` in the `en` file. In the settings menu, the label will be automatically extended to `German (Deutsch)`. 
  * Make the launcher aware that there is a new language: Modify [`src/main/java/org/terasology/launcher/util/Languages.java`](https://github.com/MovingBlocks/TerasologyLauncher/blob/develop/src/main/java/org/terasology/launcher/util/Languages.java) so that you create a new locale, add your language to the supported locales and select the settings label.
  * Add a flag icon to represent the language in the Settings menu. To do this, download an appropriate icon from the [famfamfam.com icon pack](http://www.famfamfam.com/lab/icons/flags/) into [`src/main/resources/org/terasology/launcher/images/flags`](https://github.com/MovingBlocks/TerasologyLauncher/tree/develop/src/main/resources/org/terasology/launcher/images/flags) (or create your own 16x11 icon!), then add a `flag_de=...` entry to the `ImageBundle.properties` file located in the same folder as the `LabelsBundle_*.properties` file you've edited previously.

To keep everything nice and clean, we advise you to use a **feature branch**:
  * `git checkout develop`
  * `git branch germanTranslation`
  * `git checkout germanTranslation`
  * Add/Change files
  * `git commit ....`
  * `git push`
  * Open pull request

If you want, feel free to add yourself to the README.md as a contributor.