package ru.zagvladimir.tgbot;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTest {

    private final ApplicationModules modules = ApplicationModules.of(TgbotApplication.class);

    @Test
    void verifiesModuleBoundaries() {
        modules.verify();
    }

    @Test
    void writesDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}
