package com.aias;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Phase 0 Architecture Rules (ArchUnit 1.5.0).
 * Enforces strictly established modular-monolith boundaries.
 */
@AnalyzeClasses(packages = "com.aias", importOptions = {ImportOption.DoNotIncludeTests.class})
public class ArchitectureTest {

    @ArchTest
    static final ArchRule no_circular_dependencies =
        slices().matching("com.aias.(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule scoring_cannot_depend_on_ai_or_evaluation =
        noClasses().that().resideInAPackage("..scoring..")
            .should().dependOnClassesThat().resideInAnyPackage("..ai..", "..evaluation..");

    @ArchTest
    static final ArchRule common_cannot_depend_on_feature_modules =
        noClasses().that().resideInAPackage("..common..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..framework..", "..assessment..", "..evidence..", "..scoring..",
                "..evaluation..", "..ai..", "..review..", "..penpicture..",
                "..academic..", "..audit..", "..security.."
            );

    @ArchTest
    static final ArchRule evaluation_cannot_depend_on_review =
        noClasses().that().resideInAPackage("..evaluation..")
            .should().dependOnClassesThat().resideInAPackage("..review..");
}
