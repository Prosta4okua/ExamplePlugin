# Плагин-пример. 
Чтобы увидеть, как работает плагин, заходи в папку src/darkdustry и открывай файл Example.java
Там все расписано с пояснениями.
## Примечания:
- Плагин может не работать, информация может быть неправильной, я не несу никакой ответственности за нее.
- Плагин написан для 129.1 версии mindustry и не будет работать на версиях ниже.
- Если используете этот плагин, или код из него, пожалуйста, оставляйте ссылку на оригинал.
- Не рекомендую использовать плагин на публичных серверах.

## Building
First, make sure you have JDK 14 installed. Then, setup [plugin.json](src/main/resources/plugin.json) and run the following commands:

* Windows: `gradlew jar`
* *nix/Mac OS: `./gradlew jar`

### Troubleshooting

* If the terminal returns `Permission denied` or `Command not found`, run `chmod +x ./gradlew`.
