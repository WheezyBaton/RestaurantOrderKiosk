package com.wheezybaton.kiosk_system.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "com.wheezybaton.kiosk_system", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule services_should_be_in_service_package =
            classes().that().haveSimpleNameEndingWith("Service").should().resideInAPackage("..service..");

    @ArchTest
    static final ArchRule controllers_should_be_in_controller_package =
            classes().that().haveSimpleNameEndingWith("Controller").should().resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule repositories_should_be_in_repository_package =
            classes().that().haveSimpleNameEndingWith("Repository").should().resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule annotated_controllers_should_be_in_controller_package =
            classes()
                    .that().areAnnotatedWith("org.springframework.stereotype.Controller")
                    .or().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .should().resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule layered_architecture_must_be_respected =
            Architectures.layeredArchitecture()
                    .consideringOnlyDependenciesInAnyPackage("com.wheezybaton.kiosk_system..")
                    .layer("Controller").definedBy("..controller..")
                    .layer("Service").definedBy("..service..")
                    .layer("Repository").definedBy("..repository..")
                    .layer("Model").definedBy("..model..") // Np. encje, DTO

                    .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Service")
                    .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
                    .whereLayer("Model").mayOnlyBeAccessedByLayers("Repository", "Service", "Controller");

    @ArchTest
    static final ArchRule no_cycles_in_structure =
            com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices()
                    .matching("com.wheezybaton.kiosk_system.(*)..")
                    .should().beFreeOfCycles();
}