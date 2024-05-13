package de.SparkArmy.jda.annotations.events;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JDAEntitySelectInteractionEvent {
    String name() default "";

    String startWith() default "";
}