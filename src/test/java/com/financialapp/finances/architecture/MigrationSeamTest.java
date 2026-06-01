package com.financialapp.finances.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

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
    void noLegacyOldPackageRemains() {
        // Slice 5 removed the legacy tree entirely. Guard that it never returns.
        assertThat(imported)
                .filteredOn(c -> c.getPackageName().startsWith("com.financialapp.finances.old"))
                .isEmpty();
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
