package com.intelygenz.android;

import java.lang.annotation.Annotation;

class AnnotationDeps {


    String getAnnotationImport() {
        return String.format("import %s;", Feat.class.getCanonicalName());
    }

    String getAnnotation(String feature, String scenario) {
        String className = Feat.class.getSimpleName();
        return String.format("@%s(feature = \"%s\", scenario = \"%s\")", className, feature, scenario);
    }

    boolean isCoincident(Annotation annotation, String feature, String scenario) {
        if (annotation instanceof Feat) {
            Feat feat = (Feat) annotation;
            return feat.feature().equalsIgnoreCase(feature)
                    && feat.scenario().equalsIgnoreCase(scenario);
        }
        return false;
    }

    Class getAnnotationClass() {
        return Feat.class;
    }
}
