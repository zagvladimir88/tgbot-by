package ru.zagvladimir.tgbot;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

class HexagonalBoundariesTest {

    private static final String BASE = "ru.zagvladimir.tgbot";

    private final com.tngtech.archunit.core.domain.JavaClasses classes = new ClassFileImporter().importPackages(BASE);

    @Test
    void domainKnowsNothingAboutTelegram() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(BASE + ".domain..")
                .and()
                .haveSimpleNameNotEndingWith("Test")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(BASE + ".telegram..", "org.telegram..")
                .check(classes);
    }

    @Test
    void domainKnowsNothingAboutIntegrationsAndInfrastructure() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(BASE + ".domain..")
                .and()
                .haveSimpleNameNotEndingWith("Test")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(BASE + ".integration..", BASE + ".infra..")
                .check(classes);
    }

    @Test
    void domainDoesNotSpeakHttpOrJdbcDirectly() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(BASE + ".domain..")
                .and()
                .haveSimpleNameNotEndingWith("Test")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.springframework.web..", "org.springframework.jdbc..", "java.sql..")
                .check(classes);
    }

    @Test
    void integrationsDependOnDomainOnlyThroughItsOwnTypes() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(BASE + ".integration..")
                .and()
                .haveSimpleNameNotEndingWith("Test")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(BASE + ".telegram..", BASE + ".subscription..")
                .check(classes);
    }
}
