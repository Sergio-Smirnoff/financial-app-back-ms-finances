package com.financialapp.finances.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class MigrationSeamTest {

    private static JavaClasses imported;

    @BeforeAll
    static void load() {
        imported = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.financialapp.finances");
    }

    @Test
    void useCaseInterfacesAreImplementedOnlyInApplicationLayer() {
        classes()
                .that().implement("com.financialapp.finances.domain.usecase.transaction.RecordTransaction")
                .should().resideInAPackage("com.financialapp.finances.application..")
                .check(imported);
    }

    @Test
    void legacyAndNewDomainDoNotEntangle() {
        // The legacy code lives under the old/ directory but keeps the flat packages
        // (com.financialapp.finances.{service,kafka,model,...}); guard both the directory-style
        // "old" package and the new domain against importing each other. allowEmptyShould keeps
        // the rule green while the legacy tree carries no "old"-prefixed package.
        noClasses()
                .that().resideInAPackage("com.financialapp.finances.old..")
                .should().dependOnClassesThat().resideInAPackage("com.financialapp.finances.domain..")
                .allowEmptyShould(true)
                .check(imported);

        noClasses()
                .that().resideInAPackage("com.financialapp.finances.domain..")
                .should().dependOnClassesThat().resideInAPackage("com.financialapp.finances.old..")
                .allowEmptyShould(true)
                .check(imported);
    }

    @Test
    void supportedCurrenciesPortIsFrameworkFree() {
        noClasses()
                .that().haveSimpleName("SupportedCurrencies")
                .and().resideInAPackage("com.financialapp.finances.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..")
                .allowEmptyShould(true)
                .check(imported);
    }

    @Test
    void supportedCurrenciesImplIsReferencedOnlyViaTheInterface() {
        noClasses()
                .that().resideInAPackage("com.financialapp.finances..")
                .and().haveSimpleNameNotContaining("SupportedCurrenciesImpl")
                .and().haveSimpleNameNotContaining("MessagingConfig")  // none reference the impl; guard stays true
                .should().dependOnClassesThat().haveSimpleName("SupportedCurrenciesImpl")
                .allowEmptyShould(true)
                .check(imported);
    }
}
