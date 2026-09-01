package com.financialapp.finances.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(
        packages = "com.financialapp.finances",
        importOptions = ImportOption.DoNotIncludeTests.class)
class LayeredArchitectureTest {

    @ArchTest
    static final ArchRule layers_respect_inward_dependency_flow = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("..finances.domain..")
            .layer("Application").definedBy("..finances.application..")
            .layer("Web").definedBy("..finances.web..")
            .layer("Infrastructure").definedBy("..finances.infrastructure..")
            // Domain is the core: accessed by everyone, depends on no other layer.
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Web", "Infrastructure")
            // Application orchestrates domain; only adapters (web/infra) drive it.
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Web", "Infrastructure")
            // Web and Infrastructure are outermost: nothing else imports them.
            .whereLayer("Web").mayNotBeAccessedByAnyLayer()
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer();

    @ArchTest
    static final ArchRule domain_is_free_of_framework_and_outer_layers = noClasses()
            .that().resideInAPackage("..finances.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..finances.web..",
                    "..finances.infrastructure..",
                    "org.springframework..",
                    "jakarta.persistence..")
            .as("Domain must not depend on web, infrastructure, Spring, or JPA");

    @ArchTest
    static final ArchRule application_does_not_depend_on_web = noClasses()
            .that().resideInAPackage("..finances.application..")
            .should().dependOnClassesThat().resideInAPackage("..finances.web..")
            .as("Application must not depend on the web layer");

    @ArchTest
    static final ArchRule domain_events_are_recorded_by_aggregates_not_the_application = noClasses()
            .that().resideInAPackage("..finances.application..")
            .should().dependOnClassesThat().resideInAPackage("..finances.domain.event..")
            .as("Domain events must be recorded by aggregates in the domain, not constructed in the "
                    + "application layer (use cases drain them via the domain event publisher)");

    @ArchTest
    static final ArchRule only_aggregate_roots_have_repositories = classes()
            .that().resideInAPackage("..finances.domain.repository..")
            .and().areInterfaces()
            // package-info is modelled as an interface by ArchUnit; doc, not a repository.
            .and().haveSimpleNameNotContaining("package-info")
            .should().haveSimpleNameEndingWith("Repository")
            .andShould().haveSimpleNameNotContaining("Subcategory")
            .as("Only aggregate roots (Transaction, Category) may have a domain repository; "
                    + "Subcategory is owned by Category and has no repository")
            // Vacuous until the first repository interface is migrated into the empty skeleton.
            .allowEmptyShould(true);
}
